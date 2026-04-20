package com.xatruch.pos.ui.caja

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.databinding.FragmentCajaBinding
import com.xatruch.pos.ui.menu.ProductAdapter

class CajaFragment : Fragment() {

    private var _binding: FragmentCajaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CajaViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

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
            Toast.makeText(requireContext(), "Factura procesada y enviada a cocina", Toast.LENGTH_SHORT).show()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}