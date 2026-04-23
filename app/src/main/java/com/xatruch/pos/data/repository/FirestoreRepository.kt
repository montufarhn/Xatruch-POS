package com.xatruch.pos.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.Product

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
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllProducts(): List<Product> {
        val uid = userId ?: return emptyList()
        return try {
            db.collection("users").document(uid)
                .collection("products")
                .get().await().toObjects(Product::class.java)
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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

    fun saveProduct(product: Product) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("products").document(product.id.toString())
                .set(product)
        }
    }

    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        userId?.let { uid ->
            val invoiceRef = db.collection("users").document(uid)
                .collection("invoices").document(invoice.id.toString())
            
            db.runTransaction { transaction ->
                transaction.set(invoiceRef, invoice)
                items.forEach { item ->
                    val itemRef = invoiceRef.collection("items").document(item.id.toString())
                    transaction.set(itemRef, item)
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
}
