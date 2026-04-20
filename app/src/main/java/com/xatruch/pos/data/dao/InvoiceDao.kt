package com.xatruch.pos.data.dao

import androidx.room.*
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE status = 'PENDIENTE' ORDER BY date ASC")
    fun getPendingInvoices(): Flow<List<Invoice>>

    @Query("UPDATE invoices SET status = :status WHERE id = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: Long, status: String)

    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Transaction
    suspend fun insertInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItem>>
}
