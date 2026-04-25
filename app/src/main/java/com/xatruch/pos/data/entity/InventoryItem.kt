package com.xatruch.pos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "", // e.g., "pcs", "kg", "liters"
    val minStock: Double = 0.0
)
