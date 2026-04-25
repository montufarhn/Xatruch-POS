package com.xatruch.pos.data.dao

import androidx.room.*
import com.xatruch.pos.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Upsert
    suspend fun insert(product: Product)

    @Upsert
    suspend fun insertWithId(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)
}
