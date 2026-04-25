package com.xatruch.pos.ui.inventario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.InventoryItem
import com.xatruch.pos.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class InventarioViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryDao = AppDatabase.getDatabase(application).inventoryDao()
    private val firestoreRepository = FirestoreRepository()
    val allInventoryItems: LiveData<List<InventoryItem>> = inventoryDao.getAllInventoryItems().asLiveData()

    fun saveInventoryItem(name: String, quantity: Double, unit: String, minStock: Double) {
        viewModelScope.launch {
            val item = InventoryItem(
                name = name,
                quantity = quantity,
                unit = unit,
                minStock = minStock
            )
            val id = inventoryDao.insert(item)
            firestoreRepository.saveInventoryItem(item.copy(id = id))
        }
    }
    
    fun updateQuantity(item: InventoryItem, newQuantity: Double) {
        viewModelScope.launch {
            val updatedItem = item.copy(quantity = newQuantity)
            inventoryDao.update(updatedItem)
            firestoreRepository.saveInventoryItem(updatedItem)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            inventoryDao.delete(item)
            firestoreRepository.deleteInventoryItem(item)
        }
    }
}
