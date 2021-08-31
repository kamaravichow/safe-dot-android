/*
 * Copyright (C) 2021.  Aravind Chowdary (@kamaravichow)
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

package com.aravi.dot.activities.custom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aravi.dot.R;
import com.aravi.dot.databinding.ActivityCustomisationBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.Objects;


public class CustomisationActivity extends AppCompatActivity {
    private ActivityCustomisationBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCustomisationBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);

        mBinding.saveButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(CustomisationActivity.this)
                    .setTitle(R.string.requires_upgrade)
                    .setMessage(R.string.requires_upgrade_message)
                    .setPositiveButton(R.string.requires_upgrade_button, (dialog, which) -> {
                        String url = "https://play.google.com/store/apps/details?id=com.aravi.dotpro";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    })
                    .setNeutralButton(R.string.requires_upgrade_nevermind, null)
                    .show();
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}