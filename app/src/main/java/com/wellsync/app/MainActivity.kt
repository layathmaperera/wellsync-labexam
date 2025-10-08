package com.wellsync.app

import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.wellsync.app.databinding.ActivityMainBinding
import com.wellsync.app.fragments.DashboardFragment
import com.wellsync.app.fragments.JournalFragment
import com.wellsync.app.fragments.MoodDialogFragment
import com.wellsync.app.fragments.SettingsFragment  // Changed from SettingsDialogFragment
import com.wellsync.app.services.HydrationReminderService
import com.wellsync.app.utils.NotificationHelper
import com.wellsync.app.viewmodels.WellSyncViewModel
import android.Manifest
import com.wellsync.app.utils.AuthManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WellSyncViewModel by viewModels()
    private lateinit var authManager: AuthManager

    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MainActivity onCreate started")

        // Splash screen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        authManager = AuthManager(this)
        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            redirectToLogin()
            return
        }


        // Emoji support
        val config = BundledEmojiCompatConfig(this)
        EmojiCompat.init(config)



        // Request notification permission on Android 13+
        requestNotificationPermissionIfNeeded()

//        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Test notifications and service
        testNotificationSystem()
        testHydrationServiceDirectly()

        // Setup toolbar as action bar
        setupToolbar()

        // Setup bottom navigation
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        // Setup FAB for mood logging
        binding.fabAddMood.setOnClickListener {
            val moodDialog = MoodDialogFragment()
            moodDialog.show(supportFragmentManager, "MoodDialog")
        }

        splashScreen.setKeepOnScreenCondition { false }
        Log.d(TAG, "MainActivity onCreate completed")
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }



    // --- NOTIFICATION PERMISSION ---
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                testNotificationSystem()
            }
        }
    }

    // --- TOOLBAR & MENU ---
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Load Settings as a fragment instead of dialog
                loadFragment(SettingsFragment())
                // Hide FAB when in settings
                binding.fabAddMood.hide()
                // Hide bottom navigation when in settings
                binding.bottomNavigation.visibility = View.GONE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- BOTTOM NAVIGATION ---
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    // Show FAB when not in settings
                    binding.fabAddMood.show()
                    // Show bottom navigation
                    binding.bottomNavigation.visibility = View.VISIBLE
                    true
                }
                R.id.nav_journal -> {
                    loadFragment(JournalFragment())
                    // Show FAB when not in settings
                    binding.fabAddMood.show()
                    // Show bottom navigation
                    binding.bottomNavigation.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    // --- FRAGMENT LOADING ---
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)  // Add to back stack for proper navigation
            .commit()
    }

    // Override back button behavior
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            // If we're in settings, show the FAB and bottom nav again
            binding.fabAddMood.show()
            binding.bottomNavigation.visibility = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    // --- NOTIFICATION & SERVICE TESTING ---
    private fun testNotificationSystem() {
        val notificationHelper = NotificationHelper(this)
        notificationHelper.testNotification()
    }

    private fun testHydrationServiceDirectly() {
        startHydrationServiceWithInterval(5)
    }

    private fun startHydrationServiceWithInterval(intervalSeconds: Int) {
        val intent = Intent(this, HydrationReminderService::class.java)
        intent.action = HydrationReminderService.ACTION_START_REMINDERS
        intent.putExtra(HydrationReminderService.EXTRA_INTERVAL, intervalSeconds)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun stopHydrationService() {
        val intent = Intent(this, HydrationReminderService::class.java)
        intent.action = HydrationReminderService.ACTION_STOP_REMINDERS
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHydrationService()
    }
}