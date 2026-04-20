package com.xatruch.pos.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MenuViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        observeViewModel()
        
        binding.btnGuardar.setOnClickListener {
            guardarProducto()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.recyclerMenu.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }
    }

    private fun guardarProducto() {
        val nombre = binding.editNombre.text.toString()
        val precioStr = binding.editPrecio.text.toString()
        val category = if (binding.radioPlatillo.isChecked) "Platillo" else "Bebida"

        if (nombre.isNotEmpty() && precioStr.isNotEmpty()) {
            val precio = precioStr.toDoubleOrNull()
            if (precio != null) {
                viewModel.saveProduct(nombre, precio, category)
                Toast.makeText(requireContext(), "Producto guardado: $nombre", Toast.LENGTH_SHORT).show()
                
                binding.editNombre.text?.clear()
                binding.editPrecio.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Precio inválido", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}