package com.aravi.dot.model;

public class Log {

    private String appName;
    private String appPackage;
    private String timestamp;
    private int camera;
    private int mic;

    public Log() {

    }

    public Log(String appName, String appPackage, String timestamp, int camera, int mic) {
        this.appName = appName;
        this.appPackage = appPackage;
        this.timestamp = timestamp;
        this.camera = camera;
        this.mic = mic;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getCamera() {
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    public int getMic() {
        return mic;
    }

    public void setMic(int mic) {
        this.mic = mic;
    }
}