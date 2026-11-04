package com.aravi.dot.activities.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.aravi.dot.manager.DevLogger
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.util.PermissionUtils
import com.aravi.dot.util.Utils
import com.google.android.material.snackbar.Snackbar
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private val devLogger: DevLogger by inject()
    private val utils: Utils by inject()
    private val preferences: PreferenceManager by inject()
    private val permissionUtils: PermissionUtils by inject()
    private val database: AppDatabase by inject()
    private val viewModel by viewModels<MainActivityViewModel>()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onBeforeCreate() {
        installSplashScreen()
    }

    override val contentView: View
        get() {
            binding = ActivityMainBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        edge2Edge()
        binding.appbarLayout.root.setPadding(0, statusBarHeight(resources), 0, 0)
        binding.openSettings.setOnClickListener {
            viewModel.onAccessibilityClicked(this@MainActivity)
            devLogger.log("ACCESSIBILITY : Opening permission window")
        }
        binding.moreAccessControls.setOnClickListener {
            openActivity(AccessActionsActivity::class.java)
        }
        binding.cameraSwitch.setOnClickListener {
            utils.openHistoryActivity(this, Constants.PERMISSION_CAMERA)
        }
        binding.microphoneSwitch.setOnClickListener {
            utils.openHistoryActivity(this, Constants.PERMISSION_MICROPHONE)
        }
        binding.locationSwitch.setOnClickListener {
            utils.openHistoryActivity(this, Constants.PERMISSION_LOCATION)
        }
        binding.customiseDots.setOnClickListener {
            openActivity(CustomisationActivity::class.java)
        }
        binding.notificationSettings.setOnClickListener {
            openNotificationSettings()
        }
        binding.feedbackOption.setOnClickListener {
            sendFeedback(getString(R.string.feedback_subject))
        }
        binding.bugReport.setOnClickListener {
            sendFeedback(getString(R.string.feedback_subject))
        }
        binding.featureRequest.setOnClickListener {
            sendFeedback(getString(R.string.feature_request_subject))
        }
        binding.shareOption.setOnClickListener { shareApp() }
        binding.githubButton.setOnClickListener { openWeb(Constants.URL_GITHUB) }
        binding.twitterButton.setOnClickListener { openWeb(Constants.URL_TWITTER) }
        binding.telegramButton.setOnClickListener { openWeb(Constants.URL_PROJECT) }

        with(viewModel) {
            setupAccessibilityListener(this@MainActivity)
            setupAccessibilityLaunchListener(this@MainActivity)
            isAccessibilityServiceEnabled.observe(this@MainActivity) {
                onServiceStateChanged(it)
            }
        }

        binding.versionText.text = getString(R.string.version) + BuildConfig.VERSION_NAME
        requestNotificationsIfNeeded()
        maybeShowAutostart()
        initData()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) {
            initData()
        }
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !doesHavePermissions(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun maybeShowAutostart() {
        if (!preferences.isFirstLaunch) return
        val manufacturer = Build.MANUFACTURER
        if (manufacturer.equals("xiaomi", true) ||
            manufacturer.equals("oppo", true) ||
            manufacturer.equals("vivo", true) ||
            manufacturer.equals("Honor", true)
        ) {
            utils.showAutoStartDialog(this, manufacturer)
        }
        preferences.setFirstLaunch()
    }

    private fun onServiceStateChanged(enabled: Boolean) {
        if (enabled) {
            binding.accessibilityStatusIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle_outline_24)
            )
            binding.accessibilityStatusIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.green_300)
            )
            binding.accessibilityStatusTitle.setText(R.string.accessibility_enabled_title)
            binding.accessibilityStatusMessage.setText(R.string.accessibility_enabled_message)
            devLogger.log("ACCESSIBILITY : Permission ACCEPTED")
        } else {
            binding.accessibilityStatusIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_outline_cancel_24)
            )
            binding.accessibilityStatusIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.red_300)
            )
            binding.accessibilityStatusTitle.setText(R.string.accessibility_disabled_title)
            binding.accessibilityStatusMessage.setText(R.string.accessibility_disabled_message)
            devLogger.log("ACCESSIBILITY : Permission DENIED")
        }
    }

    private fun initData() {
        val since = System.currentTimeMillis() - DAY_MS
        val locationLogs = database.logsDao().countDistinctPackagesSince(Constants.PERMISSION_LOCATION, since)
        val cameraLogs = database.logsDao().countDistinctPackagesSince(Constants.PERMISSION_CAMERA, since)
        val microphoneLogs = database.logsDao().countDistinctPackagesSince(Constants.PERMISSION_MICROPHONE, since)
        val totalLogs = locationLogs + cameraLogs + microphoneLogs
        val counts = listOf(cameraLogs, microphoneLogs, locationLogs)
        val colors = listOf(R.color.green_500, R.color.orange_500, R.color.purple_500)

        binding.cameraSwitch.setMessage(permissionUtils.getPermissionUsageInfo(cameraLogs))
        binding.microphoneSwitch.setMessage(permissionUtils.getPermissionUsageInfo(microphoneLogs))
        binding.locationSwitch.setMessage(permissionUtils.getPermissionUsageInfo(locationLogs))

        binding.pieChart.maxValue = if (totalLogs == 0) 1f else totalLogs.toFloat()
        binding.pieChart.minValue = 0f
        val entries = ArrayList<FitChartValue>()
        counts.forEachIndexed { index, count ->
            if (count != 0) {
                entries.add(
                    FitChartValue(count.toFloat(), ContextCompat.getColor(this, colors[index]))
                )
            }
        }
        if (entries.isEmpty()) {
            entries.add(FitChartValue(1f, Color.parseColor("#FFF2F2F2")))
        }
        binding.pieChart.setAnimationMode(AnimationMode.OVERDRAW)
        binding.pieChart.setValues(entries)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_others)))
    }

    private fun sendFeedback(subject: String) {
        val body = buildString {
            append("App version: ")
            append(BuildConfig.VERSION_NAME)
            append(" (")
            append(BuildConfig.VERSION_CODE)
            append(")\nAndroid: ")
            append(Build.VERSION.RELEASE)
            append(" (API ")
            append(Build.VERSION.SDK_INT)
            append(")\nDevice: ")
            append(Build.MANUFACTURER)
            append(' ')
            append(Build.MODEL)
            append("\n\n")
        }
        val mailto = Uri.parse("mailto:${Constants.FEEDBACK_EMAIL}").buildUpon()
            .appendQueryParameter("subject", subject)
            .appendQueryParameter("body", body)
            .build()
        val intent = Intent(Intent.ACTION_SENDTO, mailto)
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            snackbar(binding.root, getString(R.string.open_settings_failed), Snackbar.LENGTH_LONG)
        }
    }

    private fun openWeb(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            snackbar(binding.root, getString(R.string.open_settings_failed), Snackbar.LENGTH_LONG)
        }
    }

    private fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            snackbar(binding.root, getString(R.string.open_settings_failed), Snackbar.LENGTH_LONG)
        }
    }

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
