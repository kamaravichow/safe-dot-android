package com.aravi.dot.activities.customise

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.aravi.dot.R
import com.aravi.dot.databinding.ActivityCustomisationBinding
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.util.DotColorPicker
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject

class CustomisationActivity : BaseActivity() {
    private lateinit var binding: ActivityCustomisationBinding
    private val preferences: PreferenceManager by inject()

    override val contentView: View
        get() {
            binding = ActivityCustomisationBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(binding.toolbar)
        setBack()
        binding.appbar.setPadding(0, statusBarHeight(resources), 0, 0)
        binding.iconsSwitch.setMessage(getString(R.string.dot_icons_description))
        binding.iconsSwitch.setCheckedSilently(preferences.isIconsEnabled)
        binding.iconsSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.isIconsEnabled = checked
            applyIcons(checked)
        }
        binding.vibrationSwitch.setCheckedSilently(preferences.isVibrationEnabled)
        binding.vibrationSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.isVibrationEnabled = checked
        }
        binding.dotAlignmentGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val position = when (checkedId) {
                R.id.align_left -> 0
                R.id.align_center -> 2
                else -> 1
            }
            preferences.setDotPosition(position)
            applyPreviewAlignment(position)
        }
        binding.dotAlignmentGroup.check(
            when (preferences.dotPosition) {
                0 -> R.id.align_left
                2 -> R.id.align_center
                else -> R.id.align_right
            }
        )
        binding.cameraColor.setOnClickListener {
            DotColorPicker.show(this) { color ->
                preferences.cameraDotColor = color
                applyColors()
            }
        }
        binding.micColor.setOnClickListener {
            DotColorPicker.show(this) { color ->
                preferences.micDotColor = color
                applyColors()
            }
        }
        binding.locColor.setOnClickListener {
            DotColorPicker.show(this) { color ->
                preferences.locationDotColor = color
                applyColors()
            }
        }
        binding.resetDefaults.setOnClickListener { resetDefaults() }
        applyColors()
        applyIcons(preferences.isIconsEnabled)
        applyPreviewAlignment(preferences.dotPosition)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun applyColors() {
        tint(binding.dotCamera, preferences.cameraDotColor)
        tint(binding.dotMic, preferences.micDotColor)
        tint(binding.dotLocation, preferences.locationDotColor)
        binding.cameraColor.iconTint = ColorStateList.valueOf(preferences.cameraDotColor)
        binding.micColor.iconTint = ColorStateList.valueOf(preferences.micDotColor)
        binding.locColor.iconTint = ColorStateList.valueOf(preferences.locationDotColor)
    }

    private fun tint(view: View, color: Int) {
        view.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun applyIcons(enabled: Boolean) {
        binding.dotCamera.setImageResource(if (enabled) R.drawable.ic_round_camera else 0)
        binding.dotMic.setImageResource(if (enabled) R.drawable.ic_round_mic else 0)
        binding.dotLocation.setImageResource(if (enabled) R.drawable.ic_round_location else 0)
        val tint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        binding.dotCamera.imageTintList = tint
        binding.dotMic.imageTintList = tint
        binding.dotLocation.imageTintList = tint
    }

    private fun applyPreviewAlignment(position: Int) {
        val params = binding.dotHolder.layoutParams as RelativeLayout.LayoutParams
        params.removeRule(RelativeLayout.ALIGN_PARENT_START)
        params.removeRule(RelativeLayout.ALIGN_PARENT_END)
        params.removeRule(RelativeLayout.CENTER_HORIZONTAL)
        when (position) {
            0 -> params.addRule(RelativeLayout.ALIGN_PARENT_START)
            2 -> params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            else -> params.addRule(RelativeLayout.ALIGN_PARENT_END)
        }
        binding.dotHolder.layoutParams = params
    }

    private fun resetDefaults() {
        preferences.cameraDotColor = ContextCompat.getColor(this, R.color.green_500)
        preferences.micDotColor = ContextCompat.getColor(this, R.color.orange_500)
        preferences.locationDotColor = ContextCompat.getColor(this, R.color.purple_500)
        preferences.isIconsEnabled = true
        preferences.isVibrationEnabled = false
        preferences.setDotPosition(1)
        binding.iconsSwitch.setCheckedSilently(true)
        binding.vibrationSwitch.setCheckedSilently(false)
        binding.dotAlignmentGroup.check(R.id.align_right)
        applyColors()
        applyIcons(true)
        applyPreviewAlignment(1)
    }
}
