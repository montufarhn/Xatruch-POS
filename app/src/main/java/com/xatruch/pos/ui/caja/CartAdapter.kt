package com.xatruch.pos.ui.caja

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.databinding.ItemCartBinding

class CartAdapter(
    private val onIncrease: (CajaViewModel.CartItem) -> Unit,
    private val onDecrease: (CajaViewModel.CartItem) -> Unit
) : ListAdapter<CajaViewModel.CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CajaViewModel.CartItem) {
            binding.tvName.text = item.product.name
            binding.tvQuantity.text = "x${item.quantity}"
            binding.tvSubtotal.text = "L. ${String.format("%.2f", item.product.price * item.quantity)}"
            
            binding.root.setOnClickListener {
                onIncrease(item)
            }
            
            binding.root.setOnLongClickListener {
                onDecrease(item)
                true
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CajaViewModel.CartItem>() {
        override fun areItemsTheSame(oldItem: CajaViewModel.CartItem, newItem: CajaViewModel.CartItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CajaViewModel.CartItem, newItem: CajaViewModel.CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
