package com.xatruch.pos.ui.reportes

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.databinding.FragmentReportesBinding
import com.xatruch.pos.util.InvoiceDialogHelper
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class ReportesFragment : Fragment() {

    private var _binding: FragmentReportesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportesViewModel by viewModels()
    
    private lateinit var bestSellerAdapter: BestSellerAdapter
    private lateinit var invoiceAdapter: InvoiceReportAdapter
    
    private var currentBusinessData: BusinessData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportesBinding.inflate(inflater, container, false)
        
        setupRecyclerViews()
        observeViewModel()
        setupListeners()
        
        return binding.root
    }

    private fun setupRecyclerViews() {
        bestSellerAdapter = BestSellerAdapter()
        binding.recyclerBestSellers.apply {
            adapter = bestSellerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        invoiceAdapter = InvoiceReportAdapter { invoiceWithItems ->
            currentBusinessData?.let { business ->
                InvoiceDialogHelper.showInvoiceDialog(requireContext(), layoutInflater, invoiceWithItems, business)
            }
        }
        binding.recyclerInvoices.apply {
            adapter = invoiceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.invoicesInRange.observe(viewLifecycleOwner) { invoices ->
            invoiceAdapter.submitList(invoices)
        }

        viewModel.bestSellingProducts.observe(viewLifecycleOwner) { products ->
            bestSellerAdapter.submitList(products)
        }

        viewModel.totalSales.observe(viewLifecycleOwner) { total ->
            binding.tvTotalSales.text = String.format(Locale.getDefault(), "L. %.2f", total)
        }
        
        viewModel.businessData.observe(viewLifecycleOwner) {
            currentBusinessData = it
        }
        
        viewModel.dateRange.observe(viewLifecycleOwner) { range ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.tvSelectedRange.text = String.format(Locale.getDefault(), "%s - %s", sdf.format(Date(range.first)), sdf.format(Date(range.second)))
        }
    }

    private fun setupListeners() {
        binding.btnDateRange.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Seleccionar Rango de Fechas")
                .build()
            
            datePicker.addOnPositiveButtonClickListener { range ->
                val start = range.first ?: return@addOnPositiveButtonClickListener
                val end = range.second ?: return@addOnPositiveButtonClickListener
                viewModel.setDateRange(start, end + 86399999) // Add almost 1 day to include end date
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.btnExportCsv.setOnClickListener { exportData("text/csv", "reporte.csv") }
        binding.btnExportJson.setOnClickListener { exportData("application/json", "reporte.json") }
        binding.btnExportXlsx.setOnClickListener { exportData("text/csv", "reporte_excel.csv") }
    }

    private var pendingExportType: String? = null
    
    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        uri?.let { writeExportFile(it) }
    }

    private fun exportData(mimeType: String, fileName: String) {
        pendingExportType = mimeType
        createFileLauncher.launch(fileName)
    }

    private fun writeExportFile(uri: Uri) {
        val invoices = viewModel.invoicesInRange.value ?: return
        val bestSellers = viewModel.bestSellingProducts.value ?: return
        
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = OutputStreamWriter(outputStream)
                
                when (pendingExportType) {
                    "text/csv" -> {
                        writer.write("Reporte de Ventas\n")
                        writer.write("Fecha,Factura,Cliente,Total\n")
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        invoices.forEach { 
                            writer.write("${sdf.format(Date(it.invoice.date))},${it.invoice.invoiceNumber},${it.invoice.customerName},${it.invoice.totalAmount}\n")
                        }
                        writer.write("\nLo mas vendido\n")
                        writer.write("Producto,Cantidad,Total\n")
                        bestSellers.forEach {
                            writer.write("${it.productName},${it.totalQuantity},${it.totalSales}\n")
                        }
                    }
                    "application/json" -> {
                        val gson = Gson()
                        val data = mapOf(
                            "invoices" to invoices.map { it.invoice },
                            "bestSellers" to bestSellers
                        )
                        writer.write(gson.toJson(data))
                    }
                }
                writer.flush()
                Toast.makeText(requireContext(), "Reporte exportado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
