package com.aravi.dot.activities.log.adapter.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aravi.dot.databinding.ItemLogBinding


class LogViewHolder(binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
    var imageApp: ImageView = binding.imageApp
    var imageHelp: ImageView = binding.imageHelp
    var textDate: TextView = binding.textDate
    var textTime: TextView = binding.textTime
    var textSession: TextView = binding.textSession
    var textAppName: TextView = binding.textAppName
    var viewVertical: View = binding.viewVertical
    var viewHorizontal: View = binding.viewHorizontal
}
