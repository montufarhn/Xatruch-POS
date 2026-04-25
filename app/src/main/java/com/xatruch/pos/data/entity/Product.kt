package com.xatruch.pos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "", // "Platillo" or "Bebida"
    val imageUri: String? = null
)
