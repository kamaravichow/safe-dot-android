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

package com.aravi.dot.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aravi.dot.Constants;
import com.aravi.dot.R;
import com.aravi.dot.adapter.LogAdapter;
import com.aravi.dot.model.Log;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    private List<Log> logList;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.logsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        ExtendedFloatingActionButton clearLogsButton = findViewById(R.id.clearLogsButton);

        progressBar.setVisibility(View.VISIBLE);
        logList = new ArrayList<>();
        logList.clear();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        clearLogsButton.setOnClickListener(view -> new MaterialAlertDialogBuilder(LogsActivity.this)
                .setTitle(R.string.clear_dialog_title)
                .setMessage(R.string.clear_dialog_description)
                .setPositiveButton(R.string.clear_dialog_yes, (dialogInterface, i) -> {
                    clearLogs();
                    new Handler().postDelayed(this::loadLogData, 1000);
                })
                .setNegativeButton(R.string.clear_dialog_no, null)
                .show());

        new Handler().postDelayed(this::loadLogData, 1000);
    }

    private void loadLogData() {
        logList.clear();
        AsyncTask.execute(() -> {
            Gson gson = new Gson();
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(Constants.LOGS_PREFERENCE_NAME, Context.MODE_PRIVATE);
            Type logListType = new TypeToken<ArrayList<Log>>() {
            }.getType();
            List<Log> savedList = gson.fromJson(preferences.getString(Constants.LOGS_PREFERENCE_TAG, null), logListType);
            if (savedList != null) {
                logList = savedList;
            }
            progressBar.setVisibility(View.INVISIBLE);
            setAdapter();
        });
    }

    private void setAdapter() {
        Collections.reverse(logList);
        runOnUiThread(() -> {
            if (logList.isEmpty()) {
                findViewById(R.id.emptyListImage).setVisibility(View.VISIBLE);
                findViewById(R.id.clearLogsButton).setVisibility(View.INVISIBLE);
            } else {
                findViewById(R.id.emptyListImage).setVisibility(View.GONE);
                findViewById(R.id.clearLogsButton).setVisibility(View.VISIBLE);
            }
            adapter = new LogAdapter(LogsActivity.this, logList);
            recyclerView.setAdapter(adapter);
        });

    }

    private void clearLogs() {
        SharedPreferences preferences = this.getSharedPreferences("APP.USAGE.LOG", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        Toast.makeText(this, "Log Cleared Successfully", Toast.LENGTH_SHORT).show();
    }
}