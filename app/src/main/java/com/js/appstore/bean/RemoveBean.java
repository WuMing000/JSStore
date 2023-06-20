package com.js.appstore.bean;

import java.util.Objects;
import java.util.Timer;

public class RemoveBean {

    private String packageName;
    private long removeId;
    private Timer timer;

    public RemoveBean() {
    }

    public RemoveBean(String packageName, long removeId, Timer timer) {
        this.packageName = packageName;
        this.removeId = removeId;
        this.timer = timer;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getRemoveId() {
        return removeId;
    }

    public void setRemoveId(long removeId) {
        this.removeId = removeId;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoveBean that = (RemoveBean) o;
        return removeId == that.removeId && packageName.equals(that.packageName) && timer.equals(that.timer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, removeId, timer);
    }

    @Override
    public String toString() {
        return "RemoveBean{" +
                "packageName='" + packageName + '\'' +
                ", removeId=" + removeId +
                ", timer=" + timer +
                '}';
    }
}
