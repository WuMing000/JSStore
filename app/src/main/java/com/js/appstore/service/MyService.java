package com.js.appstore.service;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.bean.DownBean;
import com.js.appstore.bean.DownProgressBean;
import com.js.appstore.receiver.DownloadReceiver;
import com.js.appstore.receiver.MyInstalledReceiver;
import com.js.appstore.utils.CustomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static android.app.NotificationManager.IMPORTANCE_MIN;

public class MyService extends Service {
    private static final String TAG = "MyService=========>";

    private final IBinder binder = new MyBinder();

    private DownloadReceiver receiver;
    private MyInstalledReceiver myInstalledReceiver;

    private List<Long> downloadIds;
    private DownloadManager downloadManager;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand()");
//        downloadIds = new ArrayList<>();
//        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//        String channelId;
//        // 8.0 以上需要特殊处理
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // 静音通知
//            channelId = createNotificationChannel("kim.hsl", "ForegroundService");
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
//            Notification notification = builder.setOngoing(true)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setPriority(IMPORTANCE_MIN)
//                    .setCategory(Notification.CATEGORY_SERVICE)
//                    .build();
//            startForeground(1, notification);
//        }
//        receiver();
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind............");
        downloadIds = new ArrayList<>();
        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        String channelId;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 静音通知
            channelId = createNotificationChannel("kim.hsl", "ForegroundService");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            Notification notification = builder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(1, notification);
        }
        receiver();
        return binder;
    }
    public class MyBinder extends Binder {
        public MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (myInstalledReceiver != null) {
            unregisterReceiver(myInstalledReceiver);
        }
        if (downloadIds.size() != 0) {
            Log.e(TAG, downloadIds.toString());
            for (Long downloadId : downloadIds) {
                downloadManager.remove(downloadId);
            }
        }
    }

    private void receiver() {
        receiver = new DownloadReceiver(new DownloadReceiver.DataCallBack() {
            @Override
            public void onDataChanged(String url, String packageName) {
                DownBean downBean = CustomUtil.download(url, packageName);
                downloadIds.add(downBean.getDownloadId());
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        DownProgressBean downProgressBean = CustomUtil.downloadProgress(downBean.getDownloadId(), timer);
                        if (downProgressBean.getProgress() != null) {
                            Log.e(TAG, downProgressBean.getProgress() + "");
                            if (downProgressBean.getProgress().contains("100.00")) {
                                Log.e(TAG, "js.app.download.completed");
                                Intent completedIntent = new Intent("js.app.download.completed");
//                            completedIntent.putExtra("position", position);
//                            completedIntent.putExtra("getList", getList);
                                completedIntent.putExtra("packageName", packageName);
                                sendBroadcast(completedIntent);
                            } else {
//                            Log.e(TAG, "js.download.progress");
                                Intent intent = new Intent("js.download.progress");
                                intent.putExtra("downloadId", downProgressBean.getDownloadId());
//                            intent.putExtra("position", downProgressBean.getPosition());
                                intent.putExtra("progress", downProgressBean.getProgress());
//                            intent.putExtra("getList", getList);
                                intent.putExtra("packageName", packageName);
                                sendBroadcast(intent);
                            }
                        }
                    }
                }, 0, 1000);
//                Log.e(TAG, downProgressBeanList.toString());
            }
        });
        IntentFilter intentFilter = new IntentFilter("js.download.app");
        registerReceiver(receiver, intentFilter);

        myInstalledReceiver = new MyInstalledReceiver();
        IntentFilter installFilter = new IntentFilter();
        installFilter.addDataScheme("package");
        installFilter.addAction("android.intent.action.PACKAGE_ADDED");
        installFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        registerReceiver(myInstalledReceiver, installFilter);
    }

    /**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
}