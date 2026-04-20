package com.xatruch.pos.ui.cocina

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.databinding.ItemOrderBinding

class OrderAdapter(
    private val viewModel: CocinaViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : ListAdapter<Invoice, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(invoice: Invoice) {
            binding.tvOrderNumber.text = "Orden #${invoice.id}"
            binding.tvCustomer.text = "Cliente: ${invoice.customerName}"
            
            // Fetch items for this specific invoice
            viewModel.getItemsForOrder(invoice.id).observe(viewLifecycleOwner) { items ->
                val itemsText = items.joinToString("\n") { "${it.quantity}x ${it.productName}" }
                binding.tvItems.text = itemsText
            }

            binding.btnReady.setOnClickListener {
                viewModel.markOrderAsReady(invoice.id)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Invoice>() {
        override fun areItemsTheSame(oldItem: Invoice, newItem: Invoice): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Invoice, newItem: Invoice): Boolean {
            return oldItem == newItem
        }
    }
}
