package com.xatruch.pos.ui.caja

import android.app.Application
import androidx.lifecycle.*
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.data.entity.Product
import com.xatruch.pos.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

class CajaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val invoiceDao = db.invoiceDao()
    private val businessDao = db.businessDao()
    private val inventoryDao = db.inventoryDao()
    private val ingredientDao = db.ingredientDao()
    private val firestoreRepository = FirestoreRepository()

    private val _allProducts: LiveData<List<Product>> = productDao.getAllProducts().asLiveData()
    
    private val _searchQuery = MutableLiveData<String>("")
    
    val filteredProducts: LiveData<List<Product>> = MediatorLiveData<List<Product>>().apply {
        fun update() {
            val query = _searchQuery.value.orEmpty().trim()
            val products = _allProducts.value.orEmpty()
            
            value = if (query.length >= 2) {
                products.filter { it.name.contains(query, ignoreCase = true) }
            } else {
                products
            }
        }
        
        addSource(_allProducts) { update() }
        addSource(_searchQuery) { update() }
    }
    
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    val totalAmount: LiveData<Double> = _cartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }

    fun addToCart(product: Product) {
        android.util.Log.d("CajaViewModel", "Adding to cart: ${product.name}")
        val currentItems = _cartItems.value.orEmpty().toMutableList()
        val existingItem = currentItems.find { it.product.id == product.id }
        
        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentItems.add(CartItem(product, 1))
        }
        _cartItems.value = ArrayList(currentItems)
        android.util.Log.d("CajaViewModel", "Cart size now: ${currentItems.size}")
    }

    fun removeFromCart(product: Product) {
        val currentItems = _cartItems.value.orEmpty().toMutableList()
        val existingItem = currentItems.find { it.product.id == product.id }
        
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                val index = currentItems.indexOf(existingItem)
                currentItems[index] = existingItem.copy(quantity = existingItem.quantity - 1)
            } else {
                currentItems.remove(existingItem)
            }
        }
        _cartItems.value = ArrayList(currentItems)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _lastProcessedInvoice = MutableLiveData<Pair<InvoiceWithItems, BusinessData>?>()
    val lastProcessedInvoice: LiveData<Pair<InvoiceWithItems, BusinessData>?> = _lastProcessedInvoice

    fun clearLastInvoice() {
        _lastProcessedInvoice.value = null
    }

    fun processInvoice(customerName: String, customerRtn: String) {
        viewModelScope.launch {
            try {
                val currentCart = _cartItems.value.orEmpty()
                android.util.Log.d("CajaViewModel", "Processing invoice for: $customerName (RTN: $customerRtn)")
                
                if (currentCart.isEmpty()) {
                    android.util.Log.e("CajaViewModel", "Cart is empty, cannot process invoice")
                    return@launch
                }

                val business = businessDao.getBusinessData().firstOrNull() ?: BusinessData()

                // Obtener el número de factura secuencial
                val nextInvoiceNum = if (business.currentInvoiceNumber < business.initialInvoiceNumber) {
                    business.initialInvoiceNumber
                } else {
                    business.currentInvoiceNumber
                }
                
                val formattedInvoiceNumber = String.format(Locale.getDefault(), "%06d", nextInvoiceNum)
                
                val invoice = Invoice(
                    invoiceNumber = formattedInvoiceNumber,
                    totalAmount = totalAmount.value ?: 0.0,
                    customerName = if (customerName.isBlank()) "Consumidor Final" else customerName,
                    rtn = customerRtn,
                    status = "PENDIENTE"
                )

                val invoiceItems = currentCart.map { cartItem ->
                    InvoiceItem(
                        invoiceId = 0,
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.product.price,
                        subtotal = cartItem.product.price * cartItem.quantity
                    )
                }

                val id = invoiceDao.insertInvoiceWithItems(invoice, invoiceItems)
                android.util.Log.d("CajaViewModel", "Invoice saved with ID: $id")
                
                // --- Deducción de Inventario ---
                currentCart.forEach { cartItem ->
                    android.util.Log.d("CajaViewModel", "Deducting inventory for product: ${cartItem.product.name}")
                    val ingredients = ingredientDao.getIngredientsForProduct(cartItem.product.id)
                    
                    if (ingredients.isNotEmpty()) {
                        android.util.Log.d("CajaViewModel", "Found ${ingredients.size} ingredients for ${cartItem.product.name}")
                        ingredients.forEach { ingredient ->
                            val inventoryItem = inventoryDao.getInventoryItemById(ingredient.inventoryItemId)
                            if (inventoryItem != null) {
                                val reduction = ingredient.quantityNeeded * cartItem.quantity
                                val newQuantity = (inventoryItem.quantity - reduction).coerceAtLeast(0.0)
                                val updatedItem = inventoryItem.copy(quantity = newQuantity)
                                
                                inventoryDao.update(updatedItem)
                                firestoreRepository.saveInventoryItem(updatedItem)
                                android.util.Log.d("CajaViewModel", "Inventory item ${inventoryItem.name} updated: ${inventoryItem.quantity} -> $newQuantity")
                            }
                        }
                    } else {
                        // Fallback: Buscar un item en inventario con exactamente el mismo nombre (ej. refrescos)
                        android.util.Log.d("CajaViewModel", "No ingredients found, trying name match fallback for ${cartItem.product.name}")
                        val inventoryItem = inventoryDao.getInventoryItemByName(cartItem.product.name)
                        if (inventoryItem != null) {
                            val newQuantity = (inventoryItem.quantity - cartItem.quantity).coerceAtLeast(0.0)
                            val updatedItem = inventoryItem.copy(quantity = newQuantity)
                            
                            inventoryDao.update(updatedItem)
                            firestoreRepository.saveInventoryItem(updatedItem)
                            android.util.Log.d("CajaViewModel", "Inventory item (fallback) ${inventoryItem.name} updated: ${inventoryItem.quantity} -> $newQuantity")
                        } else {
                            android.util.Log.w("CajaViewModel", "No inventory item found for fallback: ${cartItem.product.name}")
                        }
                    }
                }
                
                // Actualizar el número de factura para la próxima vez
                val updatedBusiness = business.copy(currentInvoiceNumber = nextInvoiceNum + 1)
                businessDao.insertOrUpdate(updatedBusiness)
                
                val savedInvoice = invoice.copy(id = id)
                val itemsWithCorrectId = invoiceItems.map { it.copy(invoiceId = id) }
                val savedInvoiceWithItems = InvoiceWithItems(savedInvoice, itemsWithCorrectId)
                
                // Sync to Firestore
                firestoreRepository.saveInvoice(savedInvoice, itemsWithCorrectId)
                firestoreRepository.saveBusinessData(updatedBusiness)
                
                _lastProcessedInvoice.postValue(Pair(savedInvoiceWithItems, updatedBusiness))
                clearCart()
                
            } catch (e: Exception) {
                android.util.Log.e("CajaViewModel", "Error processing invoice", e)
            }
        }
    }

    data class CartItem(
        val product: Product,
        val quantity: Int
    )
}
