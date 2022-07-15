package com.aravi.dot.activities.log.adapter.holders

import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aravi.dot.databinding.ItemLogHeaderBinding


class LogHeaderViewHolder(binding: ItemLogHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    var icon: ImageView = binding.imageIcon
    var title: TextView = binding.textTitle
    var info: TextView = binding.textInfo
    var progressIndicator: ProgressBar = binding.progressHorizontal

}
