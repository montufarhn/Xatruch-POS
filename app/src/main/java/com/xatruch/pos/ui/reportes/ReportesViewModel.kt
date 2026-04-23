package com.xatruch.pos.ui.reportes

import android.app.Application
import androidx.lifecycle.*
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.data.entity.ProductSales
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReportesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val invoiceDao = db.invoiceDao()
    private val businessDao = db.businessDao()

    val businessData: LiveData<com.xatruch.pos.data.entity.BusinessData?> = businessDao.getBusinessData().asLiveData()

    private val _dateRange = MutableStateFlow(Pair(getStartOfDay(), getEndOfDay()))
    val dateRange: LiveData<Pair<Long, Long>> = _dateRange.asLiveData()

    val invoicesInRange: LiveData<List<InvoiceWithItems>> = _dateRange.flatMapLatest { range ->
        invoiceDao.getInvoicesInDateRange(range.first, range.second)
    }.asLiveData()

    val bestSellingProducts: LiveData<List<ProductSales>> = _dateRange.flatMapLatest { range ->
        invoiceDao.getBestSellingProducts(range.first, range.second)
    }.asLiveData()

    val totalSales: LiveData<Double> = invoicesInRange.map { invoices ->
        invoices.sumOf { it.invoice.totalAmount }
    }

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = Pair(start, end)
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
