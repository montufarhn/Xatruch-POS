package com.xatruch.pos.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.R
import com.xatruch.pos.data.entity.Product
import com.xatruch.pos.databinding.DialogAddIngredientBinding
import com.xatruch.pos.databinding.FragmentMenuBinding
import kotlinx.coroutines.launch

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
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                showIngredientsDialog(product)
            },
            onDeleteClick = { product ->
                confirmDelete(product)
            }
        )
        binding.recyclerMenu.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }
        // Necesitamos observar el inventario para que el LiveData se active y cargue los datos
        viewModel.allInventoryItems.observe(viewLifecycleOwner) { _ -> 
            // Solo observar para mantener el valor actualizado en viewModel.allInventoryItems.value
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

    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_msg)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                viewModel.deleteProduct(product)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showIngredientsDialog(product: Product) {
        val dialogBinding = DialogAddIngredientBinding.inflate(layoutInflater)
        val inventoryItems = viewModel.allInventoryItems.value ?: emptyList()
        
        if (inventoryItems.isEmpty()) {
            Toast.makeText(requireContext(), "Debe agregar items al inventario primero", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, inventoryItems.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerInventory.adapter = adapter

        lifecycleScope.launch {
            val existing = viewModel.getIngredientsForProduct(product.id)
            if (existing.isNotEmpty()) {
                val ing = existing[0] // Simple case: 1 principal ingredient for now
                val index = inventoryItems.indexOfFirst { it.id == ing.inventoryItemId }
                if (index != -1) dialogBinding.spinnerInventory.setSelection(index)
                dialogBinding.etQuantity.setText(ing.quantityNeeded.toString())
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Vincular con Inventario: ${product.name}")
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val selectedIndex = dialogBinding.spinnerInventory.selectedItemPosition
                val selectedItem = inventoryItems[selectedIndex]
                val quantity = dialogBinding.etQuantity.text.toString().toDoubleOrNull() ?: 1.0
                
                viewModel.addIngredient(product.id, selectedItem.id, quantity)
                Toast.makeText(requireContext(), "Vínculo guardado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
