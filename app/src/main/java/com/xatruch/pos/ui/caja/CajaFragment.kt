package com.xatruch.pos.ui.caja

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.entity.Invoice
import com.xatruch.pos.data.entity.InvoiceItem
import com.xatruch.pos.databinding.DialogInvoicePreviewBinding
import com.xatruch.pos.databinding.FragmentCajaBinding
import com.xatruch.pos.ui.menu.ProductAdapter
import com.xatruch.pos.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CajaFragment : Fragment() {

    private var _binding: FragmentCajaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CajaViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCajaBinding.inflate(inflater, container, false)
        
        setupRecyclerViews()
        observeViewModel()
        
        binding.btnFacturar.setOnClickListener {
            // Here we could show a dialog to enter customer name and RTN
            viewModel.processInvoice("", "")
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Product Menu Adapter
        productAdapter = ProductAdapter { product ->
            viewModel.addToCart(product)
        }
        
        binding.recyclerProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Cart Adapter
        cartAdapter = CartAdapter(
            onIncrease = { item -> viewModel.addToCart(item.product) },
            onDecrease = { item -> viewModel.removeFromCart(item.product) }
        )
        binding.recyclerCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }

        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            binding.tvTotalAmount.text = "L. ${String.format("%.2f", total)}"
        }

        viewModel.lastProcessedInvoice.observe(viewLifecycleOwner) { triple ->
            triple?.let { (invoice, items, businessData) ->
                showInvoiceDialog(invoice, items, businessData)
                viewModel.clearLastInvoice()
            }
        }
    }

    private fun showInvoiceDialog(invoice: Invoice, items: List<InvoiceItem>, businessData: BusinessData) {
        val dialogBinding = DialogInvoicePreviewBinding.inflate(layoutInflater)
        
        if (!businessData.logoUri.isNullOrEmpty()) {
            try {
                dialogBinding.imgLogo.visibility = View.VISIBLE
                dialogBinding.imgLogo.setImageURI(Uri.parse(businessData.logoUri))
            } catch (e: Exception) {
                dialogBinding.imgLogo.visibility = View.GONE
            }
        } else {
            dialogBinding.imgLogo.visibility = View.GONE
        }

        dialogBinding.tvBusinessName.text = if (businessData.name.isBlank()) "Nombre del Negocio" else businessData.name
        dialogBinding.tvBusinessDetails.text = "RTN: ${businessData.rtn}\n${businessData.address}\n${businessData.phone1}"
        dialogBinding.tvInvoiceNumber.text = "Factura: ${invoice.invoiceNumber}"
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        dialogBinding.tvDate.text = "Fecha: ${sdf.format(Date(invoice.date))}"
        dialogBinding.tvCustomer.text = "Cliente: ${invoice.customerName}"
        
        // Add items to table
        items.forEach { item ->
            val row = TableRow(requireContext())
            
            val nameTv = TextView(requireContext()).apply { 
                text = item.productName
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                setTextColor(android.graphics.Color.BLACK)
            }
            val qtyTv = TextView(requireContext()).apply { 
                text = item.quantity.toString()
                gravity = android.view.Gravity.CENTER
                setPadding(16, 0, 16, 0)
                setTextColor(android.graphics.Color.BLACK)
            }
            val priceTv = TextView(requireContext()).apply { 
                text = String.format("%.2f", item.subtotal)
                gravity = android.view.Gravity.END
                setTextColor(android.graphics.Color.BLACK)
            }
            
            row.addView(nameTv)
            row.addView(qtyTv)
            row.addView(priceTv)
            dialogBinding.tableItems.addView(row)
        }
        
        dialogBinding.tvTotal.text = "L. ${String.format("%.2f", invoice.totalAmount)}"
        dialogBinding.tvCai.text = "CAI: ${businessData.cai}\nRango: ${businessData.billingRange}"

        AlertDialog.Builder(requireContext())
            .setTitle("Factura Generada")
            .setView(dialogBinding.root)
            .setPositiveButton("Imprimir") { _, _ ->
                Toast.makeText(requireContext(), "Enviando a impresora...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}