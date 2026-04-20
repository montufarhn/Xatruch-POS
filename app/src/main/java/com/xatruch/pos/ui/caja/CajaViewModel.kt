package com.xatruch.pos.ui.caja

import android.app.Application
import androidx.lifecycle.*
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.Product
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CajaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val invoiceDao = db.invoiceDao()
    private val businessDao = db.businessDao()

    val allProducts: LiveData<List<Product>> = productDao.getAllProducts().asLiveData()
    
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    val totalAmount: LiveData<Double> = _cartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }

    fun addToCart(product: Product) {
        val currentItems = _cartItems.value.orEmpty().toMutableList()
        val existingItem = currentItems.find { it.product.id == product.id }
        
        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentItems.add(CartItem(product, 1))
        }
        _cartItems.value = currentItems
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
        _cartItems.value = currentItems
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun processInvoice(customerName: String, customerRtn: String) {
        viewModelScope.launch {
            val businessData = businessDao.getBusinessData().firstOrNull()
            val currentCart = _cartItems.value.orEmpty()
            if (currentCart.isEmpty()) return@launch

            val invoiceNumber = "INV-${System.currentTimeMillis()}"
            
            val invoice = Invoice(
                invoiceNumber = invoiceNumber,
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

            invoiceDao.insertInvoiceWithItems(invoice, invoiceItems)
            clearCart()
        }
    }

    data class CartItem(
        val product: Product,
        val quantity: Int
    )
}
