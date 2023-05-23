package com.js.appstore.bean;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class APPServerBean implements Serializable {

    private static final long serialVersionUID = -5053412967314724078L;
    @SerializedName("appId")
    private int appId;
    @SerializedName("appIcon")
    private String appIcon;
    @SerializedName("appName")
    private String appName;
    @SerializedName("appPackage")
    private String appPackage;
    @SerializedName("appInformation")
    private String appInformation;
    @SerializedName("appDownLoadURL")
    private String appDownLoadURL;
    @SerializedName("appIntroduce")
    private String appIntroduce;

    @SerializedName("appPicture")
    private String appPicture;

    public APPServerBean() {
    }

    public APPServerBean(int appId, String appIcon, String appName, String appPackage, String appInformation, String appDownLoadURL, String appIntroduce, String appPicture) {
        this.appId = appId;
        this.appIcon = appIcon;
        this.appName = appName;
        this.appPackage = appPackage;
        this.appInformation = appInformation;
        this.appDownLoadURL = appDownLoadURL;
        this.appIntroduce = appIntroduce;
        this.appPicture = appPicture;
    }

    public String getAppPicture() {
        return appPicture;
    }

    public void setAppPicture(String appPicture) {
        this.appPicture = appPicture;
    }

    public String getAppIntroduce() {
        return appIntroduce;
    }

    public void setAppIntroduce(String appIntroduce) {
        this.appIntroduce = appIntroduce;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getAppDownLoadURL() {
        return appDownLoadURL;
    }

    public void setAppDownLoadURL(String appDownLoadURL) {
        this.appDownLoadURL = appDownLoadURL;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppInformation() {
        return appInformation;
    }

    public void setAppInformation(String appInformation) {
        this.appInformation = appInformation;
    }

    @Override
    public String toString() {
        return "APPServerBean{" +
                "appId=" + appId +
                ", appIcon='" + appIcon + '\'' +
                ", appName='" + appName + '\'' +
                ", appPackage='" + appPackage + '\'' +
                ", appInformation='" + appInformation + '\'' +
                ", appDownLoadURL='" + appDownLoadURL + '\'' +
                '}';
    }
}
