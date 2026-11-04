package com.aravi.dot.activities.actions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.aravi.dot.database.AppDatabase
import com.aravi.dot.databinding.ActivityAccessActionsBinding
import com.aravi.dot.extensions.doesHavePermissions
import com.aravi.dot.extensions.permissions
import com.aravi.dot.manager.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject

class AccessActionsActivity : BaseActivity() {

    private lateinit var binding: ActivityAccessActionsBinding
    private val preferenceManager: PreferenceManager by inject()
    private val appDatabase: AppDatabase by inject()

    override val contentView: View
        get() {
            binding = ActivityAccessActionsBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(binding.toolbar)
        setBack()
        bindMonitoringSwitches()

        binding.clearAccessLogs.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Clear Access Logs")
                .setMessage("Are you sure you want to clear all access logs? This cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    appDatabase.logsDao().clearLogs()
                    Snackbar.make(binding.root, "Logs cleared", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        bindMonitoringSwitches()
    }

    private fun bindMonitoringSwitches() {
        binding.cameraSwitch.checked(preferenceManager.isCameraEnabled)
        binding.cameraSwitch.setOnClickListener {
            preferenceManager.isCameraEnabled = !preferenceManager.isCameraEnabled
            binding.cameraSwitch.checked(preferenceManager.isCameraEnabled)
        }

        binding.microphoneSwitch.checked(preferenceManager.isMicEnabled)
        binding.microphoneSwitch.setOnClickListener {
            preferenceManager.isMicEnabled = !preferenceManager.isMicEnabled
            binding.microphoneSwitch.checked(preferenceManager.isMicEnabled)
        }

        binding.locationSwitch.checked(preferenceManager.isLocationEnabled)
        binding.locationSwitch.setOnClickListener {
            if (!preferenceManager.isLocationEnabled) {
                if (doesHavePermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    preferenceManager.isLocationEnabled = true
                    binding.locationSwitch.checked(true)
                } else {
                    permissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    binding.locationSwitch.checked(false)
                }
            } else {
                preferenceManager.isLocationEnabled = false
                binding.locationSwitch.checked(false)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 12030) {
            val fineLocationIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (fineLocationIndex != -1 &&
                fineLocationIndex < grantResults.size &&
                grantResults[fineLocationIndex] == PackageManager.PERMISSION_GRANTED
            ) {
                preferenceManager.isLocationEnabled = true
                binding.locationSwitch.checked(true)
            } else {
                Toast.makeText(this, "Precise location permission is required", Toast.LENGTH_SHORT)
                    .show()
                preferenceManager.isLocationEnabled = false
                binding.locationSwitch.checked(false)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
