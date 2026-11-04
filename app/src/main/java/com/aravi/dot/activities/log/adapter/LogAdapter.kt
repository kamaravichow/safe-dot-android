package com.aravi.dot.activities.log.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aravi.dot.Constants
import com.aravi.dot.R
import com.aravi.dot.activities.log.adapter.holders.LogHeaderViewHolder
import com.aravi.dot.activities.log.adapter.holders.LogViewHolder
import com.aravi.dot.bean.Log
import com.aravi.dot.databinding.ItemLogBinding
import com.aravi.dot.databinding.ItemLogHeaderBinding
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.util.PermissionUtils
import com.aravi.dot.util.Utils


class LogsAdapter(
    val context: Context,
    val permission: String,
    val utils: Utils,
    val permissionUtils: PermissionUtils,
    val preferences: PreferenceManager,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val logsList: MutableList<Log> = ArrayList()
    private val helpPackages: MutableList<String> = ArrayList()
    private var load = false

    init {
        helpPackages.addAll(utils.getSystemApps())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            val binding: ItemLogBinding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val viewHolder = LogViewHolder(binding)
            viewHolder.imageHelp.setOnClickListener {
//                showHelpDialog(
//                    context,
//                    layoutInflater
//                )
            }
            return viewHolder
        } else {
            val binding: ItemLogHeaderBinding =
                ItemLogHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LogHeaderViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LogViewHolder) {
            val logsView: LogViewHolder = holder
            val item: Log = logsList[position - 1]
            val date: String = utils.getDateFromTimestamp(item.timestamp)
            val showHeader = if (position - 1 == 0) {
                true
            } else {
                val previousDate = utils.getDateFromTimestamp(logsList[position - 2].timestamp)
                date != previousDate
            }
            if (showHeader) {
                logsView.textDate.text = date
                logsView.textDate.visibility = View.VISIBLE
                logsView.viewHorizontal.visibility = View.VISIBLE
            } else {
                logsView.textDate.visibility = View.GONE
                logsView.viewHorizontal.visibility = View.GONE
            }
            logsView.textTime.text = if (preferences.isRelativeTimeEnabled) {
                utils.formatRelativeTime(item.timestamp)
            } else {
                utils.getTimeFromTimestamp(item.timestamp)
            }
            logsView.textAppName.text = utils.getNameFromPackageName(item.packageName)
            logsView.imageApp.background = utils.getIconFromPackageName(item.packageName)
            logsView.imageApp.backgroundTintList = null
            when (item.state) {
                Constants.STATE_ON -> logsView.textSession.text =
                    context.getString(R.string.log_permission_start)
                Constants.STATE_OFF -> logsView.textSession.text =
                    context.getString(R.string.log_permission_stop)
                Constants.STATE_INVALID -> logsView.textSession.text =
                    context.getString(R.string.log_permission_invalid)
                else -> logsView.textSession.text =
                    context.getString(R.string.log_permission_invalid)
            }
            if (helpPackages.contains(item.packageName)) {
                logsView.imageHelp.visibility = View.VISIBLE
            } else {
                logsView.imageHelp.visibility = View.GONE
            }
            logsView.rlAppInfo.setOnClickListener {
                utils.openAppSettings(context, item.packageName)
            }
        } else if (holder is LogHeaderViewHolder) {
            val headerView: LogHeaderViewHolder = holder as LogHeaderViewHolder
            headerView.icon.setImageResource(permissionUtils.getIcon(context, permission))
//            headerView.icon.imageTintList = ColorStateList.valueOf(
//                utils.getAttrColor(R.attr.colorPrimaryText)
//            )
            val permission_name: String = permissionUtils.getName(permission)
            headerView.title.text = permission_name
            headerView.title.append(" ")
            headerView.title.append(context.getString(R.string.log_usage))
            headerView.info.text = context.getString(R.string.log_info)
                .replace("#ALIAS#", permission_name)
            if (load) {
                headerView.progressIndicator.visibility = View.VISIBLE
            } else {
                headerView.progressIndicator.visibility = View.GONE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLogsList(logsList: List<Log>) {
        this.logsList.clear()
        this.logsList.addAll(logsList)
        notifyDataSetChanged()
    }

    fun stopLoading() {
        load = false
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return logsList.size + 1
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }


}
