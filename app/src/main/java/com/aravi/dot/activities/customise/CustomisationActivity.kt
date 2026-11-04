/*
 * Copyright (C) 2021.  Aravind Chowdary (@kamaravichow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aravi.dot.activities.customise

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aravi.dot.R
import com.aravi.dot.databinding.ActivityCustomisationBinding
import com.aravi.dot.manager.PreferenceManager
import com.pes.androidmaterialcolorpickerdialog.ColorPicker

class CustomisationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomisationBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomisationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        preferenceManager = PreferenceManager(this)

        binding.iconsSwitch.checked(preferenceManager.isIconsEnabled)
        updatePreviewIcons(preferenceManager.isIconsEnabled)
        binding.iconsSwitch.setOnClickListener {
            preferenceManager.isIconsEnabled = !preferenceManager.isIconsEnabled
            binding.iconsSwitch.checked(preferenceManager.isIconsEnabled)
            updatePreviewIcons(preferenceManager.isIconsEnabled)
        }

        when (preferenceManager.dotPosition) {
            0 -> binding.dotAlignmentGroup.check(binding.alignLeft.id)
            1 -> binding.dotAlignmentGroup.check(binding.alignRight.id)
            2 -> binding.dotAlignmentGroup.check(binding.alignCenter.id)
        }
        updatePreviewAlignment(preferenceManager.dotPosition)

        binding.dotAlignmentGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.alignLeft.id -> {
                    preferenceManager.setDotPostion(0)
                    updatePreviewAlignment(0)
                }
                binding.alignRight.id -> {
                    preferenceManager.setDotPostion(1)
                    updatePreviewAlignment(1)
                }
                binding.alignCenter.id -> {
                    preferenceManager.setDotPostion(2)
                    updatePreviewAlignment(2)
                }
            }
        }

        updatePreviewColors()
        binding.cameraColor.setOnClickListener {
            showColorPicker(preferenceManager.cameraDotColor) { color ->
                preferenceManager.cameraDotColor = color
                updatePreviewColors()
            }
        }
        binding.micColor.setOnClickListener {
            showColorPicker(preferenceManager.micDotColor) { color ->
                preferenceManager.micDotColor = color
                updatePreviewColors()
            }
        }
        binding.locColor.setOnClickListener {
            showColorPicker(preferenceManager.locationDotColor) { color ->
                preferenceManager.locationDotColor = color
                updatePreviewColors()
            }
        }

        binding.resetDefaults.setOnClickListener {
            preferenceManager.isIconsEnabled = true
            preferenceManager.setDotPostion(1)
            preferenceManager.cameraDotColor = ContextCompat.getColor(this, R.color.green_500)
            preferenceManager.micDotColor = ContextCompat.getColor(this, R.color.orange_500)
            preferenceManager.locationDotColor = ContextCompat.getColor(this, R.color.purple_500)

            binding.iconsSwitch.checked(true)
            binding.dotAlignmentGroup.check(binding.alignRight.id)
            updatePreviewAlignment(1)
            updatePreviewIcons(true)
            updatePreviewColors()
        }
    }

    private fun showColorPicker(currentColor: Int, onColor: (Int) -> Unit) {
        val colorPicker = ColorPicker(
            this,
            Color.red(currentColor),
            Color.green(currentColor),
            Color.blue(currentColor)
        )
        colorPicker.enableAutoClose()
        colorPicker.setCallback { color -> onColor(color) }
        colorPicker.show()
    }

    private fun updatePreviewAlignment(position: Int) {
        val params = binding.dotHolder.layoutParams as RelativeLayout.LayoutParams
        params.removeRule(RelativeLayout.ALIGN_PARENT_START)
        params.removeRule(RelativeLayout.ALIGN_PARENT_END)
        params.removeRule(RelativeLayout.CENTER_HORIZONTAL)

        val marginEdge = (45 * resources.displayMetrics.density).toInt()
        when (position) {
            0 -> {
                params.addRule(RelativeLayout.ALIGN_PARENT_START)
                params.marginStart = marginEdge
                params.marginEnd = 0
            }
            1 -> {
                params.addRule(RelativeLayout.ALIGN_PARENT_END)
                params.marginStart = 0
                params.marginEnd = marginEdge
            }
            else -> {
                params.addRule(RelativeLayout.CENTER_HORIZONTAL)
                params.marginStart = 0
                params.marginEnd = 0
            }
        }
        binding.dotHolder.layoutParams = params
    }

    private fun updatePreviewIcons(showIcons: Boolean) {
        if (showIcons) {
            binding.dotCamera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_camera))
            binding.dotMic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_mic))
            binding.dotLocation.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_location))
        } else {
            binding.dotCamera.setImageDrawable(null)
            binding.dotMic.setImageDrawable(null)
            binding.dotLocation.setImageDrawable(null)
        }
    }

    private fun updatePreviewColors() {
        binding.dotCamera.backgroundTintList = ColorStateList.valueOf(preferenceManager.cameraDotColor)
        binding.dotMic.backgroundTintList = ColorStateList.valueOf(preferenceManager.micDotColor)
        binding.dotLocation.backgroundTintList = ColorStateList.valueOf(preferenceManager.locationDotColor)
        binding.cameraColor.iconTint = ColorStateList.valueOf(preferenceManager.cameraDotColor)
        binding.micColor.iconTint = ColorStateList.valueOf(preferenceManager.micDotColor)
        binding.locColor.iconTint = ColorStateList.valueOf(preferenceManager.locationDotColor)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
