package com.xatruch.pos.ui.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.R
import com.xatruch.pos.data.entity.InventoryItem
import com.xatruch.pos.databinding.DialogAddInventoryBinding
import com.xatruch.pos.databinding.FragmentInventarioBinding

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventarioViewModel by viewModels()
    private lateinit var inventoryAdapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        observeViewModel()
        setupFab()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(onDeleteClick = { item ->
            confirmDelete(item)
        })
        binding.recyclerInventario.apply {
            adapter = inventoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun confirmDelete(item: InventoryItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_msg)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                viewModel.deleteInventoryItem(item)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.allInventoryItems.observe(viewLifecycleOwner) { items ->
            inventoryAdapter.submitList(items)
        }
    }

    private fun setupFab() {
        binding.fabAddInventory.setOnClickListener {
            showAddInventoryDialog()
        }
    }

    private fun showAddInventoryDialog() {
        val dialogBinding = DialogAddInventoryBinding.inflate(layoutInflater)
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_item)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val name = dialogBinding.etName.text.toString()
                val unit = dialogBinding.etUnit.text.toString()
                val quantity = dialogBinding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val minStock = dialogBinding.etMinStock.text.toString().toDoubleOrNull() ?: 0.0
                
                if (name.isNotEmpty() && unit.isNotEmpty()) {
                    viewModel.saveInventoryItem(name, quantity, unit, minStock)
                } else {
                    Toast.makeText(requireContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}