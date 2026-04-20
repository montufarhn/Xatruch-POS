package com.xatruch.pos.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private var selectedLogoUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedLogoUri = uri
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.imgLogo.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        setupUI()
        observeViewModel()
        
        return binding.root
    }

    private fun setupUI() {
        binding.btnSelectLogo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            selectImageLauncher.launch(intent)
        }

        binding.btnSaveBusinessData.setOnClickListener {
            saveData()
        }
    }

    private fun observeViewModel() {
        viewModel.businessData.observe(viewLifecycleOwner) { data ->
            data?.let {
                binding.etBusinessName.setText(it.name)
                binding.etAddress.setText(it.address)
                binding.etPhone1.setText(it.phone1)
                binding.etPhone2.setText(it.phone2)
                binding.etEmail.setText(it.email)
                binding.etRtn.setText(it.rtn)
                binding.etCai.setText(it.cai)
                binding.etBillingRange.setText(it.billingRange)
                binding.etInitialInvoice.setText(it.initialInvoiceNumber.toString())
                
                if (!it.logoUri.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(it.logoUri)
                        // Verify if we still have permission to access this URI
                        context?.contentResolver?.query(uri, null, null, null, null)?.use {
                            selectedLogoUri = uri
                            binding.imgLogo.setImageURI(uri)
                        } ?: run {
                            // If we can't access it, don't try to load it and maybe clear it
                            selectedLogoUri = null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun saveData() {
        val businessData = BusinessData(
            name = binding.etBusinessName.text.toString(),
            address = binding.etAddress.text.toString(),
            phone1 = binding.etPhone1.text.toString(),
            phone2 = binding.etPhone2.text.toString(),
            email = binding.etEmail.text.toString(),
            rtn = binding.etRtn.text.toString(),
            cai = binding.etCai.text.toString(),
            billingRange = binding.etBillingRange.text.toString(),
            initialInvoiceNumber = binding.etInitialInvoice.text.toString().toIntOrNull() ?: 1,
            logoUri = selectedLogoUri?.toString()
        )

        viewModel.saveBusinessData(businessData)
        Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}