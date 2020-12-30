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
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aravi.dot.R;
import com.aravi.dot.adapter.LogAdapter;
import com.aravi.dot.model.Logs;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    private LogsViewModel mLogsViewModel;
    private List<Logs> logsList;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private ExtendedFloatingActionButton clearLogsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        mLogsViewModel = ViewModelProviders.of(this).get(LogsViewModel.class);
        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.logsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        clearLogsButton = findViewById(R.id.clearLogsButton);
        clearLogsButton.hide();

        progressBar.setVisibility(View.VISIBLE);
        logsList = new ArrayList<>();
        logsList.clear();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLogsViewModel.getAllWords().observe(this, logs -> {
            progressBar.setVisibility(View.INVISIBLE);
//            adapter = new LogAdapter(LogsActivity.this, logs);
            recyclerView.setAdapter(adapter);
            if (logs.isEmpty()) {
                clearLogsButton.hide();
                findViewById(R.id.emptyListImage).setVisibility(View.VISIBLE);
            } else {
                clearLogsButton.show();
                clearLogsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLogsViewModel.clearLogs();
                        showSnackBar("Logs Cleared");
                    }
                });
                findViewById(R.id.emptyListImage).setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }


}