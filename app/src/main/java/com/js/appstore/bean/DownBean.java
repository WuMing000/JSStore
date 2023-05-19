package com.js.appstore.bean;

import java.util.Timer;

public class DownBean {

    private long downloadId;
    private Timer timer;

    public DownBean() {
    }

    public DownBean(long downloadId) {
        this.downloadId = downloadId;
    }

    public DownBean(long downloadId, Timer timer) {
        this.downloadId = downloadId;
        this.timer = timer;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }


    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "DownBean{" +
                "downloadId=" + downloadId +
                ", timer=" + timer +
                '}';
    }
}
