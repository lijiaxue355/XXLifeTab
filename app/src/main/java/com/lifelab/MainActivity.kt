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
import androidx.lifecycle.lifecycleScope
import com.lifelab.core.debug.TouchTargetInspector
import com.lifelab.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var touchTargetInspector: TouchTargetInspector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            touchTargetInspector = TouchTargetInspector(this)
        }

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
            val hasToken = (application as LifeLabApplication)
                .authTokenStore
                .hasAccessToken()

            graph.setStartDestination(
                if (hasToken) {
                    R.id.today_nav_graph
                } else {
                    R.id.auth_nav_graph
                }
            )

            navController.graph = graph
        }

        val application = application as LifeLabApplication
        if (application.authTokenStore.hasAccessToken()) {
            application.authTokenStore.getUserId()?.let { userId ->
                lifecycleScope.launch {
                    try {
                        application.dataSyncRepository
                            .refreshFromServer(userId)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        // Room 中已有数据时仍可离线使用，下次联网再刷新。
                    }
                }
            }
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

    fun isTouchTargetInspectionEnabled(): Boolean {
        return touchTargetInspector?.isEnabled() == true
    }

    fun setTouchTargetInspectionEnabled(enabled: Boolean) {
        touchTargetInspector?.setEnabled(enabled)
    }

    override fun onDestroy() {
        touchTargetInspector?.dispose()
        touchTargetInspector = null
        super.onDestroy()
    }
}
