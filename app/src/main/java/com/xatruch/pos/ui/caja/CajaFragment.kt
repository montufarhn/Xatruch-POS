package com.xatruch.pos.ui.caja

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.data.entity.InvoiceWithItems
import com.xatruch.pos.databinding.DialogInvoicePreviewBinding
import com.xatruch.pos.databinding.FragmentCajaBinding
import com.xatruch.pos.ui.menu.ProductAdapter
import com.xatruch.pos.ui.settings.SettingsViewModel
import com.xatruch.pos.util.InvoiceDialogHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CajaFragment : Fragment() {

    private var _binding: FragmentCajaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CajaViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCajaBinding.inflate(inflater, container, false)
        
        setupRecyclerViews()
        observeViewModel()
        
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }
        
        binding.rgCustomerType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbConDatos.id) {
                binding.layoutCustomerInputs.visibility = View.VISIBLE
            } else {
                binding.layoutCustomerInputs.visibility = View.GONE
                binding.etCustomerName.text.clear()
                binding.etCustomerRtn.text.clear()
            }
        }

        binding.btnFacturar.setOnClickListener {
            val customerName = if (binding.rbConDatos.isChecked) {
                binding.etCustomerName.text.toString()
            } else {
                "Consumidor Final"
            }
            val customerRtn = if (binding.rbConDatos.isChecked) {
                binding.etCustomerRtn.text.toString()
            } else {
                ""
            }
            
            if (viewModel.cartItems.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.processInvoice(customerName, customerRtn)
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Product Menu Adapter - Especificamos onItemClick para evitar ambigüedad
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                android.util.Log.d("CajaFragment", "Product clicked in fragment: ${product.name}")
                viewModel.addToCart(product)
                Toast.makeText(requireContext(), "Agregado: ${product.name}", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Cart Adapter
        cartAdapter = CartAdapter(
            onIncrease = { viewModel.addToCart(it.product) },
            onDecrease = { viewModel.removeFromCart(it.product) }
        )
        binding.recyclerCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }

        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            binding.tvTotalAmount.text = String.format(Locale.getDefault(), "L. %.2f", total)
        }

        viewModel.lastProcessedInvoice.observe(viewLifecycleOwner) { pair ->
            pair?.let { (invoiceWithItems, businessData) ->
                InvoiceDialogHelper.showInvoiceDialog(requireContext(), layoutInflater, invoiceWithItems, businessData)
                viewModel.clearLastInvoice()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
