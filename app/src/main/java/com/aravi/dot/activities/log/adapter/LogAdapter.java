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

package com.aravi.dot.activities.log.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aravi.dot.R;
import com.aravi.dot.util.Utils;
import com.aravi.dot.model.Logs;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogHolder> {

    private final Context context;
    private final List<Logs> logsList;

    public LogAdapter(Context context, List<Logs> logsList) {
        this.context = context;
        this.logsList = logsList;
    }

    @NonNull
    @Override
    public LogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogHolder(inflatedView);
    }


    @Override
    public void onBindViewHolder(@NonNull final LogHolder holder, int position) {
        final Logs item = logsList.get(position);
        holder.appName.setText(Utils.getNameFromPackageName(context, item.getPackageName()));
        holder.appPackage.setText(item.getPackageName());
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(item.getPackageName());
            holder.appIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.appTimestamp.setText(Utils.convertSecondsToHMmSs(item.getTimestamp()));

        switch (item.getCamera_state()) {
            case 0:
                holder.cameraStart.setVisibility(View.INVISIBLE);
                holder.cameraStop.setVisibility(View.INVISIBLE);
                holder.logCamDot.setVisibility(View.INVISIBLE);
                break;
            case 1:
                holder.logCamDot.setVisibility(View.VISIBLE);
                holder.cameraStart.setVisibility(View.VISIBLE);
                holder.cameraStop.setVisibility(View.INVISIBLE);
                break;
            case 2:
                holder.logCamDot.setVisibility(View.VISIBLE);
                holder.cameraStart.setVisibility(View.INVISIBLE);
                holder.cameraStop.setVisibility(View.VISIBLE);
                break;
        }

        switch (item.getMic_state()) {
            case 0:
                holder.micStart.setVisibility(View.INVISIBLE);
                holder.micStop.setVisibility(View.INVISIBLE);
                holder.logMicDot.setVisibility(View.INVISIBLE);
                break;
            case 1:
                holder.logMicDot.setVisibility(View.VISIBLE);
                holder.micStart.setVisibility(View.VISIBLE);
                holder.micStop.setVisibility(View.INVISIBLE);
                break;
            case 2:
                holder.logMicDot.setVisibility(View.VISIBLE);
                holder.micStart.setVisibility(View.INVISIBLE);
                holder.micStop.setVisibility(View.VISIBLE);
                break;

        }


        switch (item.getLoc_state()) {
            case 0:
                holder.locStart.setVisibility(View.INVISIBLE);
                holder.locStop.setVisibility(View.INVISIBLE);
                holder.logLocDot.setVisibility(View.INVISIBLE);
                break;
            case 1:
                holder.logLocDot.setVisibility(View.VISIBLE);
                holder.locStart.setVisibility(View.VISIBLE);
                holder.locStop.setVisibility(View.INVISIBLE);
                break;
            case 2:
                holder.logLocDot.setVisibility(View.VISIBLE);
                holder.locStart.setVisibility(View.INVISIBLE);
                holder.locStop.setVisibility(View.VISIBLE);
                break;

        }
    }

    @Override
    public int getItemCount() {
        if (logsList != null) {
            return logsList.size();
        }
        return 0;
    }


}

