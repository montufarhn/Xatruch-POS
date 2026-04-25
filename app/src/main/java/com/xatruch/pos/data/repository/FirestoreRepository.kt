package com.xatruch.pos.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.InventoryItem
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.Product
import com.xatruch.pos.data.entity.ProductIngredient

import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    suspend fun getBusinessData(): BusinessData? {
        val uid = userId ?: return null
        return try {
            db.collection("users").document(uid)
                .collection("config").document("business")
                .get().await().toObject(BusinessData::class.java)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAllProducts(): List<Product> {
        val uid = userId ?: return emptyList()
        return try {
            db.collection("users").document(uid)
                .collection("products")
                .get().await().toObjects(Product::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getAllInvoices(): List<Pair<Invoice, List<InvoiceItem>>> {
        val uid = userId ?: return emptyList()
        return try {
            val invoiceDocs = db.collection("users").document(uid)
                .collection("invoices")
                .get().await()
            
            val result = mutableListOf<Pair<Invoice, List<InvoiceItem>>>()
            for (doc in invoiceDocs) {
                val invoice = doc.toObject(Invoice::class.java)
                val items = doc.reference.collection("items")
                    .get().await().toObjects(InvoiceItem::class.java)
                result.add(Pair(invoice, items))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveBusinessData(data: BusinessData) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("config").document("business")
                .set(data)
        }
    }

    fun listenToBusinessData(onDataChanged: (BusinessData?) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("config").document("business")
            .addSnapshotListener { snapshot, _ ->
                onDataChanged(snapshot?.toObject(BusinessData::class.java))
            }
    }

    fun listenToProducts(onDataChanged: (List<Product>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("products")
            .addSnapshotListener { snapshot, _ ->
                onDataChanged(snapshot?.toObjects(Product::class.java) ?: emptyList())
            }
    }

    fun listenToInvoices(onDataChanged: (List<Pair<Invoice, List<InvoiceItem>>>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("invoices")
            .addSnapshotListener { snapshot, _ ->
                val result = mutableListOf<Pair<Invoice, List<InvoiceItem>>>()
                val docs = snapshot?.documents ?: emptyList()
                
                if (docs.isEmpty()) {
                    onDataChanged(emptyList())
                    return@addSnapshotListener
                }

                var processedCount = 0
                val totalDocs = docs.size
                for (doc in docs) {
                    val invoice = doc.toObject(Invoice::class.java)
                    if (invoice == null) {
                        processedCount++
                        if (processedCount == totalDocs) onDataChanged(result)
                        continue
                    }
                    
                    doc.reference.collection("items").get().addOnSuccessListener { itemSnapshot ->
                        val items = itemSnapshot.toObjects(InvoiceItem::class.java)
                        result.add(Pair(invoice, items))
                        processedCount++
                        if (processedCount == totalDocs) {
                            onDataChanged(result)
                        }
                    }.addOnFailureListener {
                        processedCount++
                        if (processedCount == totalDocs) onDataChanged(result)
                    }
                }
            }
    }

    fun saveProduct(product: Product) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("products").document(product.id.toString())
                .set(product)
        }
    }

    fun saveUserProfile(profile: com.xatruch.pos.data.entity.UserProfile) {
        db.collection("users").document(profile.uid)
            .collection("config").document("profile")
            .set(profile)
    }

    fun deleteProduct(product: Product) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("products").document(product.id.toString())
                .delete()
        }
    }

    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        userId?.let { uid ->
            val invoiceRef = db.collection("users").document(uid)
                .collection("invoices").document(invoice.id.toString())
            
            db.runTransaction { transaction ->
                transaction[invoiceRef] = invoice
                items.forEachIndexed { index, item ->
                    val itemId = "${item.productId}_$index"
                    val itemRef = invoiceRef.collection("items").document(itemId)
                    transaction[itemRef] = item
                }
            }
        }
    }

    fun updateInvoiceStatus(invoiceId: Long, status: String) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("invoices").document(invoiceId.toString())
                .update("status", status)
        }
    }

    fun saveInventoryItem(item: InventoryItem) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("inventory").document(item.id.toString())
                .set(item)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("inventory").document(item.id.toString())
                .delete()
        }
    }

    fun listenToInventory(onDataChanged: (List<InventoryItem>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("inventory")
            .addSnapshotListener { snapshot, _ ->
                onDataChanged(snapshot?.toObjects(InventoryItem::class.java) ?: emptyList())
            }
    }

    fun saveIngredient(ingredient: ProductIngredient) {
        userId?.let { uid ->
            val id = "${ingredient.productId}_${ingredient.inventoryItemId}"
            db.collection("users").document(uid)
                .collection("ingredients").document(id)
                .set(ingredient)
        }
    }

    fun listenToIngredients(onDataChanged: (List<ProductIngredient>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("ingredients")
            .addSnapshotListener { snapshot, _ ->
                onDataChanged(snapshot?.toObjects(ProductIngredient::class.java) ?: emptyList())
            }
    }
}
