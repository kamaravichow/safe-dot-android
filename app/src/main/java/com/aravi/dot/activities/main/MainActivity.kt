/*
 * Copyright (C) 2020.  Aravind Chowdary (@kamaravichow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aravi.dot.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.android.charts.donut.AnimationMode
import com.android.charts.donut.FitChartValue
import com.aravi.dot.BuildConfig
import com.aravi.dot.Constants
import com.aravi.dot.R
import com.aravi.dot.activities.actions.AccessActionsActivity
import com.aravi.dot.activities.customise.CustomisationActivity
import com.aravi.dot.database.AppDatabase
import com.aravi.dot.databinding.ActivityMainBinding
import com.aravi.dot.extensions.doesHavePermissions
import com.aravi.dot.extensions.permission
import com.aravi.dot.extensions.isAccessibilityServiceEnabled
import com.aravi.dot.manager.DevLogger
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.service.DotService
import com.aravi.dot.util.PermissionUtils
import com.aravi.dot.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.webianks.easy_feedback.EasyFeedback
import logcat.logcat
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject


class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private val devLogger: DevLogger by inject()
    private val utils: Utils by inject()

    private val permissionUtils: PermissionUtils by inject()
    private val database: AppDatabase by inject()
    private val prefManager: PreferenceManager by inject()

    private val viewModel by viewModels<MainActivityViewModel>()

    var date = ""


    private val barSet = listOf(
        "Camera" to 34f,
        "Microphone" to 120f,
        "Location" to 23f,
    )
    private lateinit var bundle: Bundle


    override val contentView: View
        get() {
            binding = ActivityMainBinding.inflate(layoutInflater)
            return (binding.root)
        }


    @SuppressLint("SetTextI18n")
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        edge2Edge()
        bundle = ActivityOptions
            .makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
            .toBundle();

        binding.appbarLayout.root.setPadding(0, statusBarHeight(resources), 0, 0)
        binding.openSettings.setOnClickListener {
            viewModel.onAccessibilityClicked(this@MainActivity)
            devLogger.log("ACCESSIBILITY : Opening permission window")
        }

        binding.moreAccessControls.setOnClickListener {
            openActivity(AccessActionsActivity::class.java)
        }



        binding.cameraSwitch.setOnClickListener {
            if (prefManager.isCameraEnabled) {
                utils.openHistoryActivity(this, Constants.PERMISSION_CAMERA)
            } else {
                openActivity(AccessActionsActivity::class.java)
            }
        }

        binding.microphoneSwitch.setOnClickListener {
            if (prefManager.isMicEnabled) {
                utils.openHistoryActivity(this, Constants.PERMISSION_MICROPHONE)
            } else {
                openActivity(AccessActionsActivity::class.java)
            }
        }

        binding.locationSwitch.setOnClickListener {
            if (prefManager.isLocationEnabled) {
                utils.openHistoryActivity(this, Constants.PERMISSION_LOCATION)
            } else {
                openActivity(AccessActionsActivity::class.java)
            }
        }

        binding.customiseDots.setOnClickListener {
            openActivity(CustomisationActivity::class.java)
        }

        with(viewModel) {
            setupAccessibilityListener(this@MainActivity)
            setupAccessibilityLaunchListener(this@MainActivity)
            isAccessibilityServiceEnabled.observe(this@MainActivity) {
                onServiceStateChanged(it)
            }
        }


        binding.bugReport.setOnClickListener {
            if (doesHavePermissions(Manifest.permission.READ_PHONE_STATE)) {
                EasyFeedback.Builder(this)
                    .withEmail("contact.24ac@gmail.com")
                    .withSystemInfo()
                    .build()
                    .start()
            } else {
                permission(Manifest.permission.READ_PHONE_STATE)
            }
        }

        binding.versionText.text = "Version : ${BuildConfig.VERSION_NAME}"
    }

    override fun onResume() {
        super.onResume()
        date = utils.getLogDayKey(System.currentTimeMillis())
        onServiceStateChanged(isAccessibilityServiceEnabled(this, DotService::class.java))
        updateTimelineButtons()
        initData()
    }


    @SuppressLint("SetTextI18n")
    private fun onServiceStateChanged(enabled: Boolean) {
        if (enabled) {
            binding.accessibilityStatusIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle_outline_24)
            )
            binding.accessibilityStatusIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.green_300)
            )
            binding.accessibilityStatusTitle.text = "Accessibility Service is enabled"
            binding.accessibilityStatusMessage.text = "This must be enabled for the app to work"
            devLogger.log("ACCESSIBILITY : Permission ACCEPTED")

        } else {
            binding.accessibilityStatusIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_outline_cancel_24)
            )
            binding.accessibilityStatusIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.red_300)
            )
            binding.accessibilityStatusTitle.text = "Accessibility Service is disabled"
            binding.accessibilityStatusMessage.text =
                "The dots will not currently work. Tap to enable"
            devLogger.log("ACCESSIBILITY : Permission DENIED")
        }

        devLogger.log("ACCESSIBILITY : Permission state $enabled")
    }

    private fun updateTimelineButtons() {
        if (prefManager.isCameraEnabled) {
            binding.cameraSwitch.alpha = 1f
            binding.cameraSwitch.setMessage("View timeline of when your camera was accessed")
        } else {
            binding.cameraSwitch.alpha = 0.5f
            binding.cameraSwitch.setMessage("Monitoring disabled. Tap to change in Access Actions")
        }

        if (prefManager.isMicEnabled) {
            binding.microphoneSwitch.alpha = 1f
            binding.microphoneSwitch.setMessage("View timeline of when your microphone was accessed")
        } else {
            binding.microphoneSwitch.alpha = 0.5f
            binding.microphoneSwitch.setMessage("Monitoring disabled. Tap to change in Access Actions")
        }

        if (prefManager.isLocationEnabled) {
            binding.locationSwitch.alpha = 1f
            binding.locationSwitch.setMessage("View timeline of when your location was accessed")
        } else {
            binding.locationSwitch.alpha = 0.5f
            binding.locationSwitch.setMessage("Monitoring disabled. Tap to change in Access Actions")
        }
    }


    private fun initData() {
        val logs: MutableList<Int> = ArrayList()
        val entries: MutableList<FitChartValue> = ArrayList()
        val colors = listOf(
            prefManager.locationDotColor,
            prefManager.cameraDotColor,
            prefManager.micDotColor
        )

        val location_logs = getLogCount(Constants.PERMISSION_LOCATION)
        val camera_logs = getLogCount(Constants.PERMISSION_CAMERA)
        val microphone_logs = getLogCount(Constants.PERMISSION_MICROPHONE)

        val total_logs = location_logs + camera_logs + microphone_logs

        logcat { "TOTAL LOGS : $total_logs" }

        binding.pieChart.maxValue = if (total_logs == 0) 1f else total_logs.toFloat()
        binding.pieChart.minValue = 0f

        logs.add(location_logs)
        logs.add(camera_logs)
        logs.add(microphone_logs)

        logcat { "LOGS LIST SIZE: ${logs.size}" }

        logcat { "LOG INDICES: ${logs.indices}" }

        for (i in logs.indices) {
            logcat { "i in for loop : $i" }
            if (logs[i] != 0) {
                logcat { "DATA EXISTS" }
                entries.add(FitChartValue(logs[i].toFloat(), colors[i]))
            }
        }

        logcat { "Entries size: ${entries.size}" }

        if (entries.size == 0) {
            logcat { "SETTING NO DATA MODE" }
            entries.add(FitChartValue(1f, 0xFFF2F2F2.toInt()))
        }

        binding.pieChart.setAnimationMode(AnimationMode.OVERDRAW)

        logcat { "SETTING VALUES $entries" }
        binding.pieChart.setValues(entries)
    }

    private fun getLogCount(perm: String): Int {
        return database.logsDao().getLogsCount(perm, date)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 12030) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                snackbar(
                    binding.root,
                    "Permission is required for sending feedback as it contains useful logs & device info",
                    Snackbar.LENGTH_LONG
                )
            } else {
                binding.bugReport.callOnClick()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}
