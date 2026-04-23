package com.xatruch.pos.ui.cocina

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xatruch.pos.databinding.FragmentCocinaBinding

class CocinaFragment : Fragment() {

    private var _binding: FragmentCocinaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CocinaViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCocinaBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        observeViewModel()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(viewModel)
        binding.recyclerOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.pendingOrders.observe(viewLifecycleOwner) { orders ->
            orderAdapter.submitList(orders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}