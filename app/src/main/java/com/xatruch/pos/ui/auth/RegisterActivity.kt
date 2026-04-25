package com.xatruch.pos.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.xatruch.pos.MainActivity
import com.xatruch.pos.data.entity.UserProfile
import com.xatruch.pos.data.repository.FirestoreRepository
import com.xatruch.pos.data.repository.SyncManager
import com.xatruch.pos.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository()
    private lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        syncManager = SyncManager(this)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.etPhone.text.toString()
            val password = binding.etPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                binding.loading.visibility = View.VISIBLE
                binding.btnRegister.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: ""
                            val profile = UserProfile(uid, name, email, phone)
                            
                            // Guardar perfil en Firestore
                            firestoreRepository.saveUserProfile(profile)
                            
                            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                            performSyncAndNavigate()
                        } else {
                            binding.loading.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performSyncAndNavigate() {
        lifecycleScope.launch {
            try {
                syncManager.syncFromCloud()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
