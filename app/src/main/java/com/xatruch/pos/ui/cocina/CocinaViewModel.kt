package com.xatruch.pos.ui.cocina

import android.app.Application
import androidx.lifecycle.*
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CocinaViewModel(application: Application) : AndroidViewModel(application) {

    private val invoiceDao = AppDatabase.getDatabase(application).invoiceDao()

    val pendingOrders: LiveData<List<Invoice>> = invoiceDao.getPendingInvoices().asLiveData()

    fun getItemsForOrder(invoiceId: Long): LiveData<List<InvoiceItem>> {
        return invoiceDao.getItemsForInvoice(invoiceId).asLiveData()
    }

    fun markOrderAsReady(invoiceId: Long) {
        viewModelScope.launch {
            invoiceDao.updateInvoiceStatus(invoiceId, "LISTO")
        }
    }
}
