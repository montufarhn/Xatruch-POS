package com.xatruch.pos.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.Product
import kotlinx.coroutines.launch

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao = AppDatabase.getDatabase(application).productDao()
    val allProducts: LiveData<List<Product>> = productDao.getAllProducts().asLiveData()

    fun saveProduct(name: String, price: Double, category: String) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                price = price,
                category = category
            )
            productDao.insert(product)
        }
    }
}