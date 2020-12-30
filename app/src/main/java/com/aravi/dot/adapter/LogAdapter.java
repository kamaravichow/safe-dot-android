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

package com.aravi.dot.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aravi.dot.R;
import com.aravi.dot.model.Log;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final Context context;
    private final List<Log> logList;

    public LogAdapter(Context context, List<Log> logList) {
        this.context = context;
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(inflatedView);
    }


    @Override
    public void onBindViewHolder(@NonNull final LogViewHolder holder, int position) {
        final Log item = logList.get(position);
        holder.appName.setText(item.getAppName());
        holder.appPackage.setText(item.getAppPackage());
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(item.getAppPackage());
            holder.appIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.appTimestamp.setText(item.getTimestamp());

        switch (item.getCamera()) {
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

        switch (item.getMic()) {
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
    }

    @Override
    public int getItemCount() {
        if (logList != null) {
            return logList.size();
        }
        return 0;
    }


    class LogViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout item;
        final CircleImageView appIcon;
        final ImageView logCamDot, logMicDot;
        final TextView appName, appPackage, appTimestamp;
        final View cameraStart, cameraStop, micStart, micStop;

        private LogViewHolder(View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.logItem);
            appIcon = itemView.findViewById(R.id.logAppIcon);
            appName = itemView.findViewById(R.id.logAppName);
            appPackage = itemView.findViewById(R.id.logAppPackage);
            appTimestamp = itemView.findViewById(R.id.logTimestamp);

            logCamDot = itemView.findViewById(R.id.logCameraDot);
            logMicDot = itemView.findViewById(R.id.logMicDot);

            cameraStart = itemView.findViewById(R.id.lineCameraStart);
            cameraStop = itemView.findViewById(R.id.lineCameraStop);
            micStart = itemView.findViewById(R.id.lineMicStart);
            micStop = itemView.findViewById(R.id.lineMicStop);
        }
    }

}

