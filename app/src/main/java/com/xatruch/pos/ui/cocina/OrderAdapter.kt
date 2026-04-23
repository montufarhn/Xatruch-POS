package com.xatruch.pos.ui.cocina

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.databinding.ItemOrderBinding

class OrderAdapter(
    private val viewModel: CocinaViewModel
) : ListAdapter<InvoiceWithItems, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(invoiceWithItems: InvoiceWithItems) {
            val invoice = invoiceWithItems.invoice
            binding.tvOrderNumber.text = "Orden #${invoice.id}"
            binding.tvCustomer.text = "Cliente: ${invoice.customerName}"
            
            val itemsText = invoiceWithItems.items.joinToString("\n") { "${it.quantity}x ${it.productName}" }
            binding.tvItems.text = itemsText

            binding.btnReady.setOnClickListener {
                viewModel.markOrderAsReady(invoice.id)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<InvoiceWithItems>() {
        override fun areItemsTheSame(oldItem: InvoiceWithItems, newItem: InvoiceWithItems): Boolean {
            return oldItem.invoice.id == newItem.invoice.id
        }

        override fun areContentsTheSame(oldItem: InvoiceWithItems, newItem: InvoiceWithItems): Boolean {
            return oldItem == newItem
        }
    }
}
