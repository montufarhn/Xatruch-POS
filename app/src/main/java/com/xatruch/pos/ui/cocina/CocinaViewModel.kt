package com.xatruch.pos.ui.cocina

import android.app.Application
import androidx.lifecycle.*
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CocinaViewModel(application: Application) : AndroidViewModel(application) {

    private val invoiceDao = AppDatabase.getDatabase(application).invoiceDao()
    private val firestoreRepository = FirestoreRepository()

    val pendingOrders: LiveData<List<InvoiceWithItems>> = invoiceDao.getPendingInvoices().asLiveData()

    fun markOrderAsReady(invoiceId: Long) {
        viewModelScope.launch {
            invoiceDao.updateInvoiceStatus(invoiceId, "LISTO")
            firestoreRepository.updateInvoiceStatus(invoiceId, "LISTO")
        }
    }
}