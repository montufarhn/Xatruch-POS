package com.xatruch.pos.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.Product
import com.xatruch.pos.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao = AppDatabase.getDatabase(application).productDao()
    private val firestoreRepository = FirestoreRepository()
    val allProducts: LiveData<List<Product>> = productDao.getAllProducts().asLiveData()

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
}