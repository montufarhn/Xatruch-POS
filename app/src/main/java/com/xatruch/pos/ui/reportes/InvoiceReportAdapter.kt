package com.xatruch.pos.ui.reportes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.databinding.ItemInvoiceReportBinding
import java.text.SimpleDateFormat
import java.util.*

class InvoiceReportAdapter(private val onItemClick: (InvoiceWithItems) -> Unit) : ListAdapter<InvoiceWithItems, InvoiceReportAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInvoiceReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InvoiceWithItems) {
            binding.tvInvoiceNum.text = "Factura: ${item.invoice.invoiceNumber}"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(item.invoice.date))
            binding.tvCustomer.text = "Cliente: ${item.invoice.customerName}"
            binding.tvTotal.text = String.format(Locale.getDefault(), "L. %.2f", item.invoice.totalAmount)
            
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InvoiceWithItems>() {
        override fun areItemsTheSame(oldItem: InvoiceWithItems, newItem: InvoiceWithItems) = oldItem.invoice.id == newItem.invoice.id
        override fun areContentsTheSame(oldItem: InvoiceWithItems, newItem: InvoiceWithItems) = oldItem == newItem
    }
}
