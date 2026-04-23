package com.xatruch.pos.ui.caja

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.databinding.ItemCartBinding
import java.util.Locale

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
            binding.tvQuantity.text = item.quantity.toString()
            binding.tvSubtotal.text = String.format(Locale.getDefault(), "L. %.2f", item.product.price * item.quantity)
            
            binding.btnPlus.setOnClickListener {
                onIncrease(item)
            }
            
            binding.btnMinus.setOnClickListener {
                onDecrease(item)
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
