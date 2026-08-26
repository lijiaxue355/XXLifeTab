package com.lifelab

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lifelab.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.content_container) as NavHostFragment
        val navController = navHostFragment.navController
        if (navController.currentDestination == null) {
            val graph = navController.navInflater.inflate(
                R.navigation.main_nav_graph
            )
            val hashToken = (application as LifeLabApplication)
                .authTokenStore
                .hasAccessToken()

            graph.setStartDestination(
                if (hashToken) {
                    R.id.today_nav_graph
                } else {
                    R.id.auth_nav_graph
                }
            )

            navController.graph = graph
        }
        binding.bottomNavigation.setupWithNavController(navController)

        val topLevelDestinations = setOf(
            R.id.todayFragment,
            R.id.experimentListFragment,
            R.id.settingsFragment,
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationContainer.isVisible =
                destination.id in topLevelDestinations
        }
    }
}
