package com.xatruch.pos.data.repository

import android.content.Context
import com.xatruch.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val firestoreRepository = FirestoreRepository()

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
