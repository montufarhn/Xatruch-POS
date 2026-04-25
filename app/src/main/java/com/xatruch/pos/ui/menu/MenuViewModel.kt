package com.xatruch.pos.ui.menu

import android.app.Application
import androidx.lifecycle.*
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.Product
import com.xatruch.pos.data.entity.ProductIngredient
import com.xatruch.pos.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val ingredientDao = db.ingredientDao()
    private val inventoryDao = db.inventoryDao()
    private val firestoreRepository = FirestoreRepository()
    
    val allProducts: LiveData<List<Product>> = productDao.getAllProducts().asLiveData()
    val allInventoryItems = inventoryDao.getAllInventoryItems().asLiveData()

    fun saveProduct(name: String, price: Double, category: String) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                price = price,
                category = category
            )
            val id = productDao.insertWithId(product)
            firestoreRepository.saveProduct(product.copy(id = id))
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.delete(product)
            firestoreRepository.deleteProduct(product)
        }
    }

    fun addIngredient(productId: Long, inventoryItemId: Long, quantity: Double) {
        viewModelScope.launch {
            val ingredient = ProductIngredient(productId, inventoryItemId, quantity)
            ingredientDao.insert(ingredient)
            firestoreRepository.saveIngredient(ingredient)
        }
    }

    suspend fun getIngredientsForProduct(productId: Long) = ingredientDao.getIngredientsForProduct(productId)
}
