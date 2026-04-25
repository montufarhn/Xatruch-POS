package com.xatruch.pos.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.xatruch.pos.MainActivity
import com.xatruch.pos.data.repository.SyncManager
import com.xatruch.pos.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        syncManager = SyncManager(this)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser != null) {
            performSyncAndNavigate()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.loading.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            performSyncAndNavigate()
                        } else {
                            binding.loading.visibility = View.GONE
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Correo de restablecimiento enviado a $email", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Ingresa tu correo electrónico primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSyncAndNavigate() {
        binding.loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                syncManager.syncFromCloud()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
}
