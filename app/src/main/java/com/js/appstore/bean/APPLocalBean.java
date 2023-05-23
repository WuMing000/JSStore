package com.js.appstore.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class APPLocalBean implements Serializable, Parcelable {

    private static final long serialVersionUID = -5053412967314724078L;

    private int appId;
    private String appIcon;
    private String appName;
    private String appPackage;
    private String appInformation;
    private String appDownLoadURL;
    private String appIntroduce;
    private String appPicture;
    private String appState;

    public APPLocalBean() {
    }

    public APPLocalBean(int appId, String appIcon, String appName, String appPackage, String appInformation, String appDownLoadURL, String appIntroduce, String appPicture, String appState) {
        this.appId = appId;
        this.appIcon = appIcon;
        this.appName = appName;
        this.appPackage = appPackage;
        this.appInformation = appInformation;
        this.appDownLoadURL = appDownLoadURL;
        this.appIntroduce = appIntroduce;
        this.appPicture = appPicture;
        this.appState = appState;
    }

    protected APPLocalBean(Parcel in) {
        appId = in.readInt();
        appIcon = in.readString();
        appName = in.readString();
        appPackage = in.readString();
        appInformation = in.readString();
        appDownLoadURL = in.readString();
        appIntroduce = in.readString();
        appPicture = in.readString();
        appState = in.readString();
    }

    public static final Creator<APPLocalBean> CREATOR = new Creator<APPLocalBean>() {
        @Override
        public APPLocalBean createFromParcel(Parcel in) {
            return new APPLocalBean(in);
        }

        @Override
        public APPLocalBean[] newArray(int size) {
            return new APPLocalBean[size];
        }
    };

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

    public String getAppState() {
        return appState;
    }

    public void setAppState(String appState) {
        this.appState = appState;
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
        return "APPLocalBean{" +
                "appId=" + appId +
                ", appIcon='" + appIcon + '\'' +
                ", appName='" + appName + '\'' +
                ", appPackage='" + appPackage + '\'' +
                ", appInformation='" + appInformation + '\'' +
                ", appDownLoadURL='" + appDownLoadURL + '\'' +
                ", appIntroduce='" + appIntroduce + '\'' +
                ", appPicture='" + appPicture + '\'' +
                ", appState='" + appState + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(appId);
        dest.writeString(appIcon);
        dest.writeString(appName);
        dest.writeString(appPackage);
        dest.writeString(appInformation);
        dest.writeString(appDownLoadURL);
        dest.writeString(appIntroduce);
        dest.writeString(appPicture);
        dest.writeString(appState);
    }
}
