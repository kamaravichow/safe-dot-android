package com.aravi.dot.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "logs_database")
public class Logs implements Serializable {

    @PrimaryKey
    @NonNull
    private long timestamp;
    private String packageName;
    private int camera_state;
    private int mic_state;

    public Logs(long timestamp, String packageName, int camera_state, int mic_state) {
        this.timestamp = timestamp;
        this.packageName = packageName;
        this.camera_state = camera_state;
        this.mic_state = mic_state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getCamera_state() {
        return camera_state;
    }

    public void setCamera_state(int camera_state) {
        this.camera_state = camera_state;
    }

    public int getMic_state() {
        return mic_state;
    }

    public void setMic_state(int mic_state) {
        this.mic_state = mic_state;
    }
}
