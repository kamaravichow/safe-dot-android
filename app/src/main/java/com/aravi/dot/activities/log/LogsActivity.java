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

package com.aravi.dot.activities.log;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.aravi.dot.R;
import com.aravi.dot.adapter.LogAdapter;
import com.aravi.dot.databinding.ActivityLogsBinding;
import com.aravi.dot.model.Logs;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {
    private ActivityLogsBinding mBinding;
    private LogsViewModel mLogsViewModel;
    private List<Logs> logsList;
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLogsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mLogsViewModel = ViewModelProviders.of(this).get(LogsViewModel.class);
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        init();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    @Override
    protected void onStart() {
        // Hides the clear button and shows progress bar on start
        mBinding.clearLogsButton.hide();
        mBinding.progressBar.setVisibility(View.VISIBLE);
        super.onStart();
    }

    private void init() {
        // Creates a new array-list object on initialisation
        logsList = new ArrayList<>();

        // I've set the linear layout manager in the layout file
        // mBinding.logsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // get all the log list live data from the view model
        mLogsViewModel.getmLogsList().observe(this, logs -> {
            mBinding.progressBar.setVisibility(View.INVISIBLE);
            adapter = new LogAdapter(LogsActivity.this, logs);
            mBinding.logsRecyclerView.setAdapter(adapter);
            if (logs.isEmpty()) {
                mBinding.clearLogsButton.hide();
                findViewById(R.id.emptyListImage).setVisibility(View.VISIBLE);
            } else {
                mBinding.clearLogsButton.show();
                mBinding.clearLogsButton.setOnClickListener(v -> {
                    mLogsViewModel.clearLogs();
                    showSnackBar("Logs Cleared");
                });
                mBinding.emptyListImage.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Snack-bar Method
     * Gets String As Input
     */
    private void showSnackBar(String message) {
        Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }


}