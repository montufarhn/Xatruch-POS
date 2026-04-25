package com.xatruch.pos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.databinding.ActivityMainBinding
import com.xatruch.pos.util.ImageUtils

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var syncManager: com.xatruch.pos.data.repository.SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        syncManager = com.xatruch.pos.data.repository.SyncManager(this)
        if (FirebaseAuth.getInstance().currentUser != null) {
            syncManager.startRealtimeSync(lifecycleScope)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Definimos los destinos principales
        val topLevelDestinations = setOf(
            R.id.nav_caja, R.id.nav_cocina, R.id.nav_inventario, R.id.nav_reportes, R.id.nav_menu
        )

        appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Vincular los componentes de navegación
        binding.navView?.setupWithNavController(navController)

        setupNavigationHeader()

        binding.navView?.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, com.xatruch.pos.ui.auth.LoginActivity::class.java))
                    finish()
                    true
                }
                else -> {
                    val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
                    if (handled) {
                        binding.drawerLayout?.closeDrawers()
                    }
                    handled
                }
            }
        }
    }

    private fun setupNavigationHeader() {
        val navView = binding.navView ?: return
        val headerView = navView.getHeaderView(0)
        
        val imgLogo = headerView.findViewById<ImageView>(R.id.img_header_logo)
        val tvBusinessName = headerView.findViewById<TextView>(R.id.tv_header_business_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_header_user_email)

        val currentUser = FirebaseAuth.getInstance().currentUser
        tvUserEmail.text = currentUser?.email ?: "Sin sesión"

        val database = AppDatabase.getDatabase(this)
        database.businessDao().getBusinessData().asLiveData().observe(this) { businessData ->
            businessData?.let {
                tvBusinessName.text = it.name.ifBlank { "Xatruch POS" }
                
                if (!it.logoUri.isNullOrEmpty()) {
                    val source: Any = if (it.logoUri.startsWith("data:image")) {
                        Log.d("MainActivity", "Decodificando Base64 para el logo")
                        ImageUtils.decodeBase64(it.logoUri) ?: R.mipmap.ic_launcher_round
                    } else {
                        it.logoUri
                    }
                    
                    imgLogo.load(source) {
                        crossfade(true)
                        placeholder(R.mipmap.ic_launcher_round)
                        error(R.mipmap.ic_launcher_round)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    imgLogo.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return when (item.itemId) {
            R.id.nav_settings -> {
                navController.navigate(R.id.nav_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
