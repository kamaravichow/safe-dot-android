package com.aravi.dot.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.GridLayout
import com.aravi.dot.Constants
import com.aravi.dot.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DotColorPicker {
    fun show(context: Context, onColor: (Int) -> Unit) {
        val density = context.resources.displayMetrics.density
        val pad = (8 * density).toInt()
        val grid = GridLayout(context).apply {
            columnCount = 4
            setPadding(pad, pad, pad, pad)
        }
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.pick_color)
            .setView(grid)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        Constants.BASIC_COLORS.forEach { hex ->
            val color = Color.parseColor(hex)
            val size = (40 * density).toInt()
            val swatch = View(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(pad, pad, pad, pad)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
                contentDescription = context.getString(R.string.pick_color)
                setOnClickListener {
                    onColor(color)
                    dialog.dismiss()
                }
            }
            grid.addView(swatch)
        }
        dialog.show()
    }
}
