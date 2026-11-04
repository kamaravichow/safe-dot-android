package me.aravi.commons.ui.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewOnSteroids @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    var isRefreshing: Boolean = false

    fun addFooterView(@Suppress("UNUSED_PARAMETER") layoutRes: Int) = Unit

    fun removeAllFooterView() = Unit
}
