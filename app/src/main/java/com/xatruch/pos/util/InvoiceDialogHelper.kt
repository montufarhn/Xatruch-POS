package com.xatruch.pos.util

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import coil.load
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.databinding.DialogInvoicePreviewBinding
import java.text.SimpleDateFormat
import java.util.*

object InvoiceDialogHelper {
    fun showInvoiceDialog(
        context: Context,
        layoutInflater: LayoutInflater,
        invoiceWithItems: InvoiceWithItems,
        businessData: BusinessData
    ) {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items
        val dialogBinding = DialogInvoicePreviewBinding.inflate(layoutInflater)

        if (!businessData.logoUri.isNullOrEmpty()) {
            val source: Any = if (businessData.logoUri.startsWith("data:image")) {
                ImageUtils.decodeBase64(businessData.logoUri) ?: View.GONE
            } else {
                businessData.logoUri
            }

            if (source != View.GONE) {
                dialogBinding.imgLogo.visibility = View.VISIBLE
                dialogBinding.imgLogo.load(source) {
                    crossfade(true)
                    // IMPORTANTE: Desactivar hardware bitmaps para que el logo sea visible al imprimir
                    allowHardware(false)
                }
            } else {
                dialogBinding.imgLogo.visibility = View.GONE
            }
        } else {
            dialogBinding.imgLogo.visibility = View.GONE
        }

        dialogBinding.tvBusinessName.text = businessData.name.ifBlank { "Nombre del Negocio" }
        dialogBinding.tvBusinessDetails.text = String.format(
            Locale.getDefault(),
            "RTN: %s\n%s\n%s",
            businessData.rtn,
            businessData.address,
            businessData.phone1
        )
        dialogBinding.tvInvoiceNumber.text = "Factura: ${invoice.invoiceNumber}"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        dialogBinding.tvDate.text = "Fecha: ${sdf.format(Date(invoice.date))}"
        dialogBinding.tvCustomer.text = "Cliente: ${invoice.customerName}"

        if (!invoice.rtn.isNullOrBlank()) {
            dialogBinding.tvCustomerRtn.visibility = View.VISIBLE
            dialogBinding.tvCustomerRtn.text = "RTN: ${invoice.rtn}"
        } else {
            dialogBinding.tvCustomerRtn.visibility = View.GONE
        }

        // Add items to table
        items.forEach { item ->
            val row = TableRow(context)

            val nameTv = TextView(context).apply {
                text = item.productName
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                setTextColor(Color.BLACK)
            }
            val qtyTv = TextView(context).apply {
                text = item.quantity.toString()
                gravity = Gravity.CENTER
                setPadding(16, 0, 16, 0)
                setTextColor(Color.BLACK)
            }
            val priceTv = TextView(context).apply {
                text = String.format(Locale.getDefault(), "%.2f", item.subtotal)
                gravity = Gravity.END
                setTextColor(Color.BLACK)
            }

            row.addView(nameTv)
            row.addView(qtyTv)
            row.addView(priceTv)
            dialogBinding.tableItems.addView(row)
        }

        dialogBinding.tvTotal.text = String.format(Locale.getDefault(), "L. %.2f", invoice.totalAmount)
        dialogBinding.tvCai.text = "CAI: ${businessData.cai}\nRango: ${businessData.billingRange}"

        AlertDialog.Builder(context)
            .setTitle("Vista de Factura")
            .setView(dialogBinding.root)
            .setPositiveButton("Imprimir") { _, _ ->
                Toast.makeText(context, "Enviando a impresora...", Toast.LENGTH_SHORT).show()
                // Imprimimos el contenedor interno para capturar toda la factura incluso si hay scroll
                PrintUtils.printView(context, dialogBinding.invoiceContainer, "Factura_${invoice.invoiceNumber}")
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
}
