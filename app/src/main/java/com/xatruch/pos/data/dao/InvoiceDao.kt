package com.xatruch.pos.data.dao

import androidx.room.*
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices WHERE status = 'PENDIENTE' ORDER BY date ASC")
    fun getPendingInvoices(): Flow<List<InvoiceWithItems>>

    @Query("UPDATE invoices SET status = :status WHERE id = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: Long, status: String)

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoicesSync(): List<InvoiceWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Transaction
    suspend fun insertInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = insertInvoice(invoice)
        // Eliminamos los items existentes para evitar duplicados si es una actualización
        deleteItemsForInvoice(invoiceId)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItem>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getInvoicesInDateRange(startDate: Long, endDate: Long): Flow<List<InvoiceWithItems>>

    @Query("""
        SELECT productName, SUM(quantity) as totalQuantity, SUM(subtotal) as totalSales 
        FROM invoice_items 
        JOIN invoices ON invoice_items.invoiceId = invoices.id 
        WHERE invoices.date BETWEEN :startDate AND :endDate 
        GROUP BY productId 
        ORDER BY totalQuantity DESC
    """)
    fun getBestSellingProducts(startDate: Long, endDate: Long): Flow<List<com.xatruch.pos.data.entity.ProductSales>>
}
