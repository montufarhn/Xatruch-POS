package com.xatruch.pos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val customerName: String? = null,
    val rtn: String? = null,
    val paymentMethod: String = "Efectivo",
    val status: String = "PENDIENTE" // PENDIENTE, LISTO, COMPLETADO
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)
