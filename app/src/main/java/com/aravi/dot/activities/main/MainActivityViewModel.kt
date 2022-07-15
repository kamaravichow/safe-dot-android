package com.aravi.dot.activities.main

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.aravi.dot.BuildConfig
import com.aravi.dot.extensions.*
import com.aravi.dot.service.DotService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivityViewModel() : ViewModel() {


    companion object {
        private val ACCESSIBILITY_SERVICE_COMPONENT =
            ComponentName(BuildConfig.APPLICATION_ID, DotService::class.java.name)
        const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
        const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";

    }

    val isAccessibilityServiceEnabled = MutableLiveData<Boolean>()

    fun setupAccessibilityListener(context: Context) = viewModelScope.launch {
        getAccessibilityListener(context).collect {
            isAccessibilityServiceEnabled.update(
                it?.doesContainComponentName(
                    ACCESSIBILITY_SERVICE_COMPONENT
                ) ?: false
            )
        }
    }

    fun setupAccessibilityLaunchListener(activity: Activity) = viewModelScope.launch {
        getAccessibilityStartListener(activity).collect {
            activity.startActivity(activity.intent.apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
    }

    private suspend fun getAccessibilityListener(context: Context): Flow<String?> {
        return Settings.Secure::class.java.getSettingAsFlow(
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            context.contentResolver,
            true
        )
    }

    private suspend fun getAccessibilityStartListener(activity: Activity): Flow<Intent?> {
        return IntentFilter(DotService.KEY_ACCESSIBILITY_START).asFlow(activity)
    }

    fun onAccessibilityClicked(context: Context) {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            val compName = ComponentName(BuildConfig.APPLICATION_ID, DotService::class.java.name)
                .flattenToString()
            bundle.putString(EXTRA_FRAGMENT_ARG_KEY, compName)
            putExtra(EXTRA_FRAGMENT_ARG_KEY, compName)
            putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
        }.toActivityDestination(context).run {
            ActivityNavigator(context).navigate(
                this, null, NavOptions.Builder().withStandardAnimations().build(), null
            )
        }
    }

}
