package com.xatruch.pos.data.dao

import androidx.room.*
import com.xatruch.pos.data.entity.ProductIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Upsert
    suspend fun insert(ingredient: ProductIngredient)

    @Query("SELECT * FROM product_ingredients WHERE productId = :productId")
    suspend fun getIngredientsForProduct(productId: Long): List<ProductIngredient>

    @Delete
    suspend fun delete(ingredient: ProductIngredient)
}
