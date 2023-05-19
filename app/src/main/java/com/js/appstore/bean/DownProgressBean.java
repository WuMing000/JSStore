package com.js.appstore.bean;

import java.util.Objects;

public class DownProgressBean {

    private long downloadId;
    private String progress;

    public DownProgressBean() {
    }

    public DownProgressBean(long downloadId, String progress) {
        this.downloadId = downloadId;
        this.progress = progress;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "DownProgressBean{" +
                "downloadId=" + downloadId +
                ", progress='" + progress + '\'' +
                '}';
    }
}
