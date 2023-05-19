package com.js.appstore.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.js.appstore.bean.DownBean;
import com.js.appstore.utils.CustomUtil;

@SuppressLint("LongLogTag")
public class DownloadReceiver extends BroadcastReceiver {

    private final DataCallBack mDataCallBack;

    public DownloadReceiver(DataCallBack callBack) {
        mDataCallBack = callBack;
    }

    private static final String TAG = "DownloadReceiver===========>";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, intent.getAction());

        if ("js.download.app".equals(intent.getAction())) {
            Log.e(TAG, "收到了广播");
            String url = intent.getStringExtra("url");
            String packageName = intent.getStringExtra("packageName");
            if (url != null && packageName != null) {
                mDataCallBack.onDataChanged(url, packageName);
            }
            Log.e(TAG, "url:" + url + ",packageName:" + packageName);
        }

    }

    public interface DataCallBack {
        void onDataChanged(String url, String packageName);
    }
}