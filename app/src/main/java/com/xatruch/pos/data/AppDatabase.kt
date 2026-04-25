package com.xatruch.pos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xatruch.pos.data.dao.BusinessDao
import com.xatruch.pos.data.dao.IngredientDao
import com.xatruch.pos.data.dao.InventoryDao
import com.xatruch.pos.data.dao.InvoiceDao
import com.xatruch.pos.data.dao.ProductDao
import com.xatruch.pos.data.entity.*

@Database(
    entities = [BusinessData::class, Product::class, Invoice::class, InvoiceItem::class, InventoryItem::class, ProductIngredient::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xatruch_pos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
