package com.js.appstore.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.js.appstore.MyApplication;
import com.js.appstore.bean.DownBean;
import com.js.appstore.bean.DownProgressBean;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.FileProvider;

import static android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS;

public class CustomUtil {

    private static final String TAG = "CustomUtil============>";

    public static void hideBottomUIMenu(Activity activity) {
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){

            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show

            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    public static DownBean download(String url, String packageName) {

        DownloadManager manager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        /*
         * 1. 封装下载请求
         */
        // 创建下载请求
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        /*
         * 设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
         *    VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
         *    VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
         *    VISIBILITY_HIDDEN:                    始终不显示通知
         */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        // 设置通知的标题和描述
//        request.setTitle("通知标题XXX");
//        request.setDescription("对于该请求文件的描述");
        /*
         * 设置允许使用的网络类型, 可选值:
         *     NETWORK_MOBILE:      移动网络
         *     NETWORK_WIFI:        WIFI网络
         *     NETWORK_BLUETOOTH:   蓝牙网络
         * 默认为所有网络都允许
         */
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 添加请求头
        // request.addRequestHeader("User-Agent", "Chrome Mozilla/5.0");
        // 设置下载文件的保存位置
        Log.e(TAG, MyApplication.getInstance().getContext().getExternalFilesDir(null).getAbsolutePath());
        Log.e(TAG, url.substring(url.lastIndexOf("/") + 1));
        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), packageName);
        request.setDestinationUri(Uri.fromFile(saveFile));

        if (saveFile.exists()) {
            saveFile.delete();
            Log.e(TAG, "删除");
        }

        /*
         * 2. 获取下载管理器服务的实例, 添加下载任务
         */
        // 将下载请求加入下载队列, 返回一个下载ID
        long downloadId = manager.enqueue(request);
        // 如果中途想取消下载, 可以调用remove方法, 根据返回的下载ID取消下载, 取消下载后下载保存的文件将被删除
        // manager.remove(downloadId);

//        //定义定时器
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                //定义一个消息传过去
//                downloadProgress(downloadId, timer);
//            }
//        },0,500); //延时0毫秒开始计时，每隔1秒计时一次

        return new DownBean(downloadId);
    }

    public static DownProgressBean downloadProgress(long downloadId, Timer timer) {
        DownProgressBean downProgressBean = new DownProgressBean();
        DownloadManager manager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        // 创建一个查询对象
        DownloadManager.Query query = new DownloadManager.Query();
        // 根据 下载ID 过滤结果
        query.setFilterById(downloadId);
        // 还可以根据状态过滤结果
        // query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
        // 执行查询, 返回一个 Cursor (相当于查询数据库)
        Cursor cursor = manager.query(query);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return downProgressBean;
        }
        // 下载ID
        @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
        // 下载请求的状态
        @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        // 下载文件在本地保存的路径（Android 7.0 以后 COLUMN_LOCAL_FILENAME 字段被弃用, 需要用 COLUMN_LOCAL_URI 字段来获取本地文件路径的 Uri）
        @SuppressLint("Range") String localFilename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        // 已下载的字节大小
        @SuppressLint("Range") long downloadedSoFar = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        // 下载文件的总字节大小
        @SuppressLint("Range") long totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) == -1 ? 1 : cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        cursor.close();
//        System.out.println("下载进度: " + downloadedSoFar  + "/" + totalSize);
        DecimalFormat decimalFormat = new DecimalFormat( "##0.00 ");
        String dd = decimalFormat.format(downloadedSoFar * 1.0f / totalSize * 100);
//        Log.e(TAG, downloadedSoFar * 1.0f / totalSize * 100 + "");
        Log.e(TAG, dd);
        downProgressBean = new DownProgressBean(downloadId, dd);
        /*
         * 判断是否下载成功，其中状态 status 的值有 5 种:
         *     DownloadManager.STATUS_SUCCESSFUL:   下载成功
         *     DownloadManager.STATUS_FAILED:       下载失败
         *     DownloadManager.STATUS_PENDING:      等待下载
         *     DownloadManager.STATUS_RUNNING:      正在下载
         *     DownloadManager.STATUS_PAUSED:       下载暂停
         */
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            /*
             * 特别注意: 查询获取到的 localFilename 才是下载文件真正的保存路径，在创建
             * 请求时设置的保存路径不一定是最终的保存路径，因为当设置的路径已是存在的文件时，
             * 下载器会自动重命名保存路径，例如: .../demo-1.apk, .../demo-2.apk
             */
            File installFile = null;
            File saveFile = new File(localFilename.substring(7));
            if (saveFile.exists()) {
                installFile = renameFile(localFilename.substring(7), localFilename.substring(7) + ".apk");
            }
//            System.out.println("下载成功, 打开文件, 文件路径: " + localFilename);
            installAPK(MyApplication.getInstance().getContext(), installFile);
            timer.cancel();
        }

        return downProgressBean;
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    private static File renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return null;
        }

        if (TextUtils.isEmpty(newPath)) {
            return null;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        boolean b = oldFile.renameTo(newFile);
        File file2 = new File(newPath);
        return file2;
    }

    /**
     * 安装APK内容
     */
    public static void installAPK(Context mContext, File apkName) {
        try {
            if (!apkName.exists()) {
                Log.e("TAG", "app not exists!");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 给目标应用一个临时授权
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                Log.e("TAG", "11111111111111");
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
                String packageName = mContext.getApplicationContext().getPackageName();
                String authority = new StringBuilder(packageName).append(".fileprovider").toString();
                Uri apkUri = FileProvider.getUriForFile(mContext, authority, apkName);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkName), "application/vnd.android.package-archive");
            }
            mContext.startActivity(intent);
//            android.os.Process.killProcess(android.os.Process.myPid());//安装完之后会提示”完成” “打开”。

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", e.toString());
        }
    }

    public static boolean isAppExists(String packageName) {
        PackageManager pm = MyApplication.getInstance().getContext().getPackageManager();
        //获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
        List<PackageInfo> list2 = pm.getInstalledPackages(GET_URI_PERMISSION_PATTERNS);
//        int j = 0;
        for (PackageInfo packageInfo : list2) {
            if (NotActiveApp(MyApplication.getInstance().getContext(), packageName)) {
                continue;
            }
            if (isSystemApplication(MyApplication.getInstance().getContext(), packageName)) {
                continue;
            }
            if (packageName.equals(packageInfo.packageName)) {
                return true;
            }
//            j++;
        }
//        Log.e("========ccc", "应用的总个数:" + j);
        return false;
    }

    public static boolean isAppInstalled(String packageName) {
        PackageManager manager = MyApplication.getInstance().getContext().getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
        }
        return true;
    }

    /** 判断是否是系统应用 */
    public static boolean isSystemApplication(Context context, String packageName){
        PackageManager mPackageManager = context.getPackageManager();
        try {
            final PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断app能不能主动启动 否就隐藏
     * */
    public static boolean NotActiveApp(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        return intent == null;
    }

    public static void openAPK(String packageName) {
        PackageManager pm = MyApplication.getInstance().getContext().getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getInstance().getContext().startActivity(intent);
    }

    public static void killAppProcess()
    {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager) MyApplication.getInstance().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList)
        {
            if (runningAppProcessInfo.pid != android.os.Process.myPid())
            {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


}
