package com.xatruch.pos.ui.reportes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xatruch.pos.data.entity.ProductSales
import com.xatruch.pos.databinding.ItemProductSalesBinding
import java.util.Locale

class BestSellerAdapter : ListAdapter<ProductSales, BestSellerAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductSalesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemProductSalesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductSales) {
            binding.tvProductName.text = item.productName
            binding.tvQuantity.text = "${item.totalQuantity} uds"
            binding.tvTotalSales.text = String.format(Locale.getDefault(), "L. %.2f", item.totalSales)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProductSales>() {
        override fun areItemsTheSame(oldItem: ProductSales, newItem: ProductSales) = oldItem.productName == newItem.productName
        override fun areContentsTheSame(oldItem: ProductSales, newItem: ProductSales) = oldItem == newItem
    }
}
