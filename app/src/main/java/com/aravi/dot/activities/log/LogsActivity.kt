/*
 * Copyright (C) 2020.  Aravind Chowdary (@kamaravichow)
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
package com.aravi.dot.activities.log

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aravi.dot.Constants
import com.aravi.dot.R
import com.aravi.dot.activities.log.adapter.LogsAdapter
import com.aravi.dot.database.AppDatabase
import com.aravi.dot.databinding.ActivityLogsBinding
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.util.PermissionUtils
import com.aravi.dot.util.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.aravi.commons.base.BaseActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LogsActivity : BaseActivity() {
    private lateinit var binding: ActivityLogsBinding
    private val viewModel: LogsViewModel by viewModel()
    private lateinit var adapter: LogsAdapter

    private val utils: Utils by inject()
    private val permissionUtils: PermissionUtils by inject()
    private val preferences: PreferenceManager by inject()
    private val database: AppDatabase by inject()

    var permission: String = Constants.PERMISSION_CAMERA


    override val contentView: View
        get() {
            binding = ActivityLogsBinding.inflate(layoutInflater)
            return (binding.root)
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        transparentStatusBar()
        binding.appbar.setPadding(0, statusBarHeight(resources) + 10, 0, 0)

        setSupportActionBar(binding.toolbar)
        setBack()
        binding.clearLogsButton.setOnClickListener { confirmClearLogs() }
        init()

    }

    private fun init() {
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(Constants.EXTRA_PERMISSION)) {
                permission = bundle.getString(Constants.EXTRA_PERMISSION)!!
            }
        }



        supportActionBar!!.title = ""
        binding.logsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LogsAdapter(this, permission, utils, permissionUtils, preferences)
        binding.logsRecyclerView.adapter = adapter

        binding.logsRecyclerView.isRefreshing = true
        viewModel.loadList(permission).observe(this) {
            adapter.stopLoading();
            adapter.setLogsList(it);

            if (it.isEmpty()) {
                binding.emptyListImage.visibility = View.VISIBLE
                binding.clearLogsButton.hide()
            } else {
                binding.emptyListImage.visibility = View.GONE
                binding.clearLogsButton.show()
            }
            binding.logsRecyclerView.isRefreshing = false
        }

//        binding.logsRecyclerView.setOnRefreshListener {
//            adapter.stopLoading();
//            init()
//        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun confirmClearLogs() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_dialog_title)
            .setMessage(R.string.clear_dialog_description)
            .setPositiveButton(R.string.clear_dialog_yes) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.logsDao().clearLogs(permission)
                }
                Snackbar.make(binding.root, R.string.logs_cleared, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.clear_dialog_no, null)
            .show()
    }

//    override fun finish() {
//        super.finish()
//        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right)
//    }
}
