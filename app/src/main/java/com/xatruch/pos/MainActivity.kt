package com.xatruch.pos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import com.xatruch.pos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Definimos los destinos principales (los que muestran el icono del Drawer en lugar de la flecha atrás)
        val topLevelDestinations = setOf(
            R.id.nav_caja, R.id.nav_cocina, R.id.nav_inventario, R.id.nav_reportes, R.id.nav_menu
        )

        appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            binding.navView?.let { binding.drawerLayout }
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Vincular los componentes de navegación si existen en el layout actual
        binding.navView?.setupWithNavController(navController)

        binding.navView?.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, com.xatruch.pos.ui.auth.LoginActivity::class.java))
                    finish()
                    true
                }
                else -> {
                    // This allows normal navigation for other items
                    val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
                    if (handled) {
                        binding.drawerLayout?.closeDrawers()
                    }
                    handled
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
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