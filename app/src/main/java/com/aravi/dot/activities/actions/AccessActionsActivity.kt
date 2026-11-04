package com.aravi.dot.activities.actions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aravi.dot.R
import com.aravi.dot.activities.ignore.WhitelistActivity
import com.aravi.dot.database.AppDatabase
import com.aravi.dot.databinding.ActivityAccessActionsBinding
import com.aravi.dot.manager.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject

class AccessActionsActivity : BaseActivity() {
    private lateinit var binding: ActivityAccessActionsBinding
    private val preferences: PreferenceManager by inject()
    private val database: AppDatabase by inject()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            preferences.isLocationEnabled = true
            binding.locationSwitch.setCheckedSilently(true)
            requestBackgroundLocationIfNeeded()
        } else {
            preferences.isLocationEnabled = false
            binding.locationSwitch.setCheckedSilently(false)
        }
    }

    private val backgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override val contentView: View
        get() {
            binding = ActivityAccessActionsBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(binding.toolbar)
        setBack()
        binding.appbar.setPadding(0, statusBarHeight(resources), 0, 0)
        binding.cameraSwitch.setCheckedSilently(preferences.isCameraEnabled)
        binding.microphoneSwitch.setCheckedSilently(preferences.isMicEnabled)
        binding.locationSwitch.setCheckedSilently(preferences.isLocationEnabled && hasLocationPermission())
        binding.timeSwitch.setCheckedSilently(preferences.isRelativeTimeEnabled)

        binding.cameraSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.isCameraEnabled = checked
        }
        binding.microphoneSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.isMicEnabled = checked
        }
        binding.locationSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked && !hasLocationPermission()) {
                binding.locationSwitch.setCheckedSilently(false)
                explainLocationPermission()
            } else {
                preferences.isLocationEnabled = checked
            }
        }
        binding.timeSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.isRelativeTimeEnabled = checked
        }
        binding.excludedApps.setOnClickListener { openActivity(WhitelistActivity::class.java) }
        binding.excludedAppsButton.setOnClickListener { openActivity(WhitelistActivity::class.java) }
        binding.clearLogs.setOnClickListener { confirmClearLogs() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun explainLocationPermission() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.requires_locationperm)
            .setMessage(R.string.requires_locationperm_message)
            .setPositiveButton(R.string.button_continue) { _, _ ->
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.button_later, null)
            .show()
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.requires_locationperm)
            .setMessage(R.string.requires_background_location_message)
            .setPositiveButton(R.string.button_continue) { _, _ ->
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton(R.string.button_later, null)
            .show()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun confirmClearLogs() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_dialog_title)
            .setMessage(R.string.clear_dialog_description)
            .setPositiveButton(R.string.clear_dialog_yes) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.logsDao().clearLogs()
                }
                Snackbar.make(binding.root, R.string.logs_cleared, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.clear_dialog_no, null)
            .show()
    }
}
