package com.xatruch.pos.data.repository

import android.content.Context
import com.xatruch.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncManager(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val firestoreRepository = FirestoreRepository()

    fun startRealtimeSync(scope: kotlinx.coroutines.CoroutineScope) {
        firestoreRepository.listenToBusinessData { cloudData ->
            cloudData?.let {
                scope.launch(Dispatchers.IO) {
                    try {
                        db.businessDao().insertOrUpdate(it)
                        android.util.Log.d("SyncManager", "Business data synced from cloud")
                    } catch (e: Exception) {
                        android.util.Log.e("SyncManager", "Error syncing business data", e)
                    }
                }
            }
        }

        firestoreRepository.listenToProducts { cloudProducts ->
            scope.launch(Dispatchers.IO) {
                try {
                    cloudProducts.forEach { product ->
                        db.productDao().insert(product)
                    }
                    android.util.Log.d("SyncManager", "Products synced from cloud: ${cloudProducts.size}")
                } catch (e: Exception) {
                    android.util.Log.e("SyncManager", "Error syncing products", e)
                }
            }
        }

        firestoreRepository.listenToInvoices { cloudInvoices ->
            scope.launch(Dispatchers.IO) {
                try {
                    // Obtenemos las facturas locales actuales para comparar
                    val localInvoices = db.invoiceDao().getAllInvoicesSync() 
                    
                    cloudInvoices.forEach { (invoice, items) ->
                        val localInvoice = localInvoices.find { it.invoice.id == invoice.id }
                        
                        // Solo insertamos si no existe o si el estado ha cambiado
                        if (localInvoice == null || localInvoice.invoice.status != invoice.status) {
                            db.invoiceDao().insertInvoiceWithItems(invoice, items)
                        }
                    }
                    android.util.Log.d("SyncManager", "Invoices synced from cloud: ${cloudInvoices.size}")
                } catch (e: Exception) {
                    android.util.Log.e("SyncManager", "Error syncing invoices", e)
                }
            }
        }

        firestoreRepository.listenToInventory { cloudItems ->
            scope.launch(Dispatchers.IO) {
                try {
                    db.inventoryDao().insertAll(cloudItems)
                    android.util.Log.d("SyncManager", "Inventory synced from cloud: ${cloudItems.size}")
                } catch (e: Exception) {
                    android.util.Log.e("SyncManager", "Error syncing inventory", e)
                }
            }
        }

        firestoreRepository.listenToIngredients { cloudIngredients ->
            scope.launch(Dispatchers.IO) {
                try {
                    // Esperar un momento para asegurar que productos e inventario se procesen
                    cloudIngredients.forEach { ingredient ->
                        try {
                            db.ingredientDao().insert(ingredient)
                        } catch (e: Exception) {
                            android.util.Log.w("SyncManager", "Ingredient FK fail (likely parent missing), retrying later: ${ingredient.productId}")
                        }
                    }
                    android.util.Log.d("SyncManager", "Ingredients synced from cloud: ${cloudIngredients.size}")
                } catch (e: Exception) {
                    android.util.Log.e("SyncManager", "Error syncing ingredients", e)
                }
            }
        }
    }

    suspend fun syncFromCloud() = withContext(Dispatchers.IO) {
        // 1. Sync Business Data
        firestoreRepository.getBusinessData()?.let {
            db.businessDao().insertOrUpdate(it)
        }

        // 2. Sync Products
        val cloudProducts = firestoreRepository.getAllProducts()
        if (cloudProducts.isNotEmpty()) {
            cloudProducts.forEach { product ->
                db.productDao().insert(product)
            }
        }

        // 3. Sync Invoices
        val cloudInvoices = firestoreRepository.getAllInvoices()
        if (cloudInvoices.isNotEmpty()) {
            cloudInvoices.forEach { (invoice, items) ->
                db.invoiceDao().insertInvoiceWithItems(invoice, items)
            }
        }
    }
}
