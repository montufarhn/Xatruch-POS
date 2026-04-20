package com.xatruch.pos.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xatruch.pos.data.Producto
import com.xatruch.pos.data.TipoProducto
import com.xatruch.pos.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnGuardar.setOnClickListener {
            guardarProducto()
        }

        return root
    }

    private fun guardarProducto() {
        val nombre = binding.editNombre.text.toString()
        val precioStr = binding.editPrecio.text.toString()
        val tipo = if (binding.radioPlatillo.isChecked) TipoProducto.PLATILLO else TipoProducto.BEBIDA

        if (nombre.isNotEmpty() && precioStr.isNotEmpty()) {
            val precio = precioStr.toDouble()
            val nuevoProducto = Producto(nombre = nombre, precio = precio, tipo = tipo)
            
            // TODO: Guardar en una base de datos o ViewModel compartido
            Toast.makeText(requireContext(), "Producto guardado: ${nuevoProducto.nombre}", Toast.LENGTH_SHORT).show()
            
            binding.editNombre.text?.clear()
            binding.editPrecio.text?.clear()
        } else {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}