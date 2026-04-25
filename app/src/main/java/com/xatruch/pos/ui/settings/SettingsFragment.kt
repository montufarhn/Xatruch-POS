package com.xatruch.pos.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.xatruch.pos.R
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.databinding.FragmentSettingsBinding
import com.xatruch.pos.util.ImageUtils

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private var selectedLogoUri: Uri? = null
    private var isNewLogoSelected = false

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                Log.d("SettingsFragment", "Nueva imagen seleccionada: $uri")
                selectedLogoUri = uri
                isNewLogoSelected = true
                binding.imgLogo.load(uri) {
                    transformations(CircleCropTransformation())
                }
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
                
                if (!it.logoUri.isNullOrEmpty() && !isNewLogoSelected) {
                    val source: Any = if (it.logoUri.startsWith("data:image")) {
                        ImageUtils.decodeBase64(it.logoUri) ?: R.mipmap.ic_launcher_round
                    } else {
                        it.logoUri
                    }
                    binding.imgLogo.load(source) {
                        crossfade(true)
                        placeholder(R.mipmap.ic_launcher_round)
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
                isNewLogoSelected = false
            }.onFailure { error ->
                Log.e("SettingsFragment", "Error al guardar", error)
                Toast.makeText(context, "Error al guardar: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveData() {
        val currentData = viewModel.businessData.value
        val newInitialNumber = binding.etInitialInvoice.text.toString().toIntOrNull() ?: 1
        
        val nextInvoiceNumber = if (currentData != null && currentData.initialInvoiceNumber != newInitialNumber) {
            newInitialNumber
        } else {
            currentData?.currentInvoiceNumber ?: newInitialNumber
        }

        val businessData = BusinessData(
            id = 1,
            name = binding.etBusinessName.text.toString(),
            address = binding.etAddress.text.toString(),
            phone1 = binding.etPhone1.text.toString(),
            phone2 = binding.etPhone2.text.toString(),
            email = binding.etEmail.text.toString(),
            rtn = binding.etRtn.text.toString(),
            cai = binding.etCai.text.toString(),
            billingRange = binding.etBillingRange.text.toString(),
            initialInvoiceNumber = newInitialNumber,
            currentInvoiceNumber = nextInvoiceNumber,
            logoUri = currentData?.logoUri
        )

        viewModel.saveBusinessData(businessData, if (isNewLogoSelected) selectedLogoUri else null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
