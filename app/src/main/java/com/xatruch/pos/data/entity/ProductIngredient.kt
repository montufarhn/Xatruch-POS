package com.xatruch.pos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "product_ingredients",
    primaryKeys = ["productId", "inventoryItemId"],
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventoryItem::class,
            parentColumns = ["id"],
            childColumns = ["inventoryItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("inventoryItemId")]
)
data class ProductIngredient(
    val productId: Long = 0,
    val inventoryItemId: Long = 0,
    val quantityNeeded: Double = 0.0
)
