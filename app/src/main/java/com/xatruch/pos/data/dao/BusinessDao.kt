package com.xatruch.pos.data.dao

import androidx.room.*
import com.xatruch.pos.data.entity.BusinessData
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM business_data WHERE id = 1")
    fun getBusinessData(): Flow<BusinessData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(businessData: BusinessData)
}
