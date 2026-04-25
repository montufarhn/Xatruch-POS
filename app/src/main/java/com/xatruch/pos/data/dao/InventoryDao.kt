package com.xatruch.pos.data.dao

import androidx.room.*
import com.xatruch.pos.data.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getInventoryItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE name = :name LIMIT 1")
    suspend fun getInventoryItemByName(name: String): InventoryItem?

    @Upsert
    suspend fun insert(item: InventoryItem): Long

    @Upsert
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)
}
