package com.xatruch.pos.ui.inventario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.data.entity.InventoryItem
import com.xatruch.pos.databinding.ItemInventoryBinding
import java.util.Locale

class InventoryAdapter(
    private val onItemClick: ((InventoryItem) -> Unit)? = null,
    private val onDeleteClick: ((InventoryItem) -> Unit)? = null
) : ListAdapter<InventoryItem, InventoryAdapter.InventoryViewHolder>(InventoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventoryViewHolder(private val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventoryItem) {
            binding.tvName.text = item.name
            binding.tvUnit.text = item.unit
            binding.tvQuantity.text = String.format(Locale.getDefault(), "%.2f", item.quantity)
            
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick?.invoke(item)
            }
        }
    }

    class InventoryDiffCallback : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
