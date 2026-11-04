package com.aravi.dot.activities.ignore

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.aravi.dot.databinding.ActivityWhitelistBinding
import com.aravi.dot.databinding.ItemWhitelistBinding
import com.aravi.dot.manager.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject

class WhitelistActivity : BaseActivity() {
    private lateinit var binding: ActivityWhitelistBinding
    private val preferences: PreferenceManager by inject()

    override val contentView: View
        get() {
            binding = ActivityWhitelistBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(binding.toolbar)
        setBack()
        binding.appbar.setPadding(0, statusBarHeight(resources), 0, 0)
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) { loadApps() }
            binding.progress.visibility = View.GONE
            binding.emptyMessage.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
            binding.appList.adapter = ExcludedAppAdapter(apps, preferences.excludedPackages.toMutableSet()) { updated ->
                preferences.excludedPackages = updated
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadApps(): List<InstalledApp> {
        val packageManager = packageManager
        val installed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(0)
        }
        return installed.mapNotNull { info ->
            if (info.packageName == packageName) return@mapNotNull null
            if (packageManager.getLaunchIntentForPackage(info.packageName) == null) return@mapNotNull null
            InstalledApp(
                packageName = info.packageName,
                label = packageManager.getApplicationLabel(info).toString(),
                icon = packageManager.getApplicationIcon(info)
            )
        }.sortedBy { it.label.lowercase() }
    }
}

private data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable
)

private class ExcludedAppAdapter(
    private val apps: List<InstalledApp>,
    private val excluded: MutableSet<String>,
    private val onChanged: (Set<String>) -> Unit
) : RecyclerView.Adapter<ExcludedAppAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemWhitelistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = apps[position]
        holder.binding.appName.text = app.label
        holder.binding.appIcon.setImageDrawable(app.icon)
        holder.binding.appExcluded.setOnCheckedChangeListener(null)
        holder.binding.appExcluded.isChecked = excluded.contains(app.packageName)
        val toggle = {
            if (holder.binding.appExcluded.isChecked) {
                excluded.add(app.packageName)
            } else {
                excluded.remove(app.packageName)
            }
            onChanged(excluded.toSet())
        }
        holder.binding.appExcluded.setOnCheckedChangeListener { _, _ -> toggle() }
        holder.binding.root.setOnClickListener {
            holder.binding.appExcluded.isChecked = !holder.binding.appExcluded.isChecked
        }
    }

    override fun getItemCount(): Int = apps.size

    class Holder(val binding: ItemWhitelistBinding) : RecyclerView.ViewHolder(binding.root)
}
