package com.js.appstore.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.bean.DownBean;
import com.js.appstore.bean.DownProgressBean;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import static android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS;

public class CustomUtil {

    private static final String TAG = "CustomUtil============>";

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
        if (totalSize == 0) {
            manager.remove(downloadId);
            timer.cancel();
            dd = "NaN";
        }
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

    public static boolean isAppInstalled(String packageName) {
        PackageManager manager = MyApplication.getInstance().getContext().getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        return i != null;
    }

    public static void openAPK(String packageName) {
        try {
            PackageManager pm = MyApplication.getInstance().getContext().getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getInstance().getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * 判断视图v是否应该隐藏输入软键盘，若v不是输入框，返回false
     *
     * @param v     视图
     * @param event 屏幕事件
     * @return 视图v是否应该隐藏输入软键盘，若v不是输入框，返回false
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    //隐藏软键盘
    public static void hideKeyBoard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            // 则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getServerFile(String path) {
        //获取网络数据
        //01.定义获取网络的数据的路径
//        String path="http://114.132.220.67:8080/test/js_project/store/Version.txt";
//        StringBuilder stringBuffer = null;
        String str = "";
        try {
            //2.实例化url
            URL url = new URL(path);
            //3.获取连接属性
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //4.设置请求方式
            conn.setRequestMethod("GET");
            //以及请求时间
            conn.setConnectTimeout(5000);
            //5.获取响应码
            int code = conn.getResponseCode();
            if (200 == code) {
                //6.获取返回的数据json
                InputStream is = conn.getInputStream();
                //7.测试（删除-注释）
                //缓冲字符流
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                stringBuffer = new StringBuilder();
                if ((str = br.readLine()) != null) {
//                    stringBuffer.append(str);
                    Log.i("tt", str);
                    return str;
                }
//                Log.i("tt", stringBuffer.toString());
                //8.解析
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName() {
        String localVersion = "";
        try {
            PackageInfo packageInfo = MyApplication.getInstance().getContext().getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(MyApplication.getInstance().getContext().getPackageName(), 0);
            localVersion = packageInfo.versionName;
            Log.d(TAG, "本软件的版本名：" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 隐藏底部底部导航栏
     */
    public static void hideNavigationBar(Activity activity) {

        Window window;
        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.setAttributes(params);


            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // hide nav bar; // hide status bar

            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower than 19, use magic number directly for higher API target level

            activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }

    /**
     * 下载文件
     * @param FilePath  要存放的文件的路径
     * @param FileName   远程FTP服务器上的那个文件的名字
     * @return   true为成功，false为失败
     */
    public static boolean downLoadFile(FTPClient ftpClient, String FilePath, String FileName) {
        Log.e(TAG, "=================run update APK");
        if (!ftpClient.isConnected()) {
            Log.e(TAG, "ftp is not connect");
            return false;
        }
        APPListDataSaveUtils update_size = new APPListDataSaveUtils(MyApplication.getInstance().getContext(), "update_size");
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            // 转到指定下载目录
//            ftpClient.changeWorkingDirectory("/data");
            // 列出该目录下所有文件
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            FTPFile[] files = ftpClient.listFiles(new String(FileName.getBytes("GBK"),"iso-8859-1"));
            if (files.length != 1) {
                Log.e(TAG, "remote file is not exists!");
                return false;
            }
            //根据绝对路径初始化文件
            File localFile = new File(FilePath);
            if (localFile.length() > files[0].getSize()) {
                Log.e(TAG, "local size large remote size.");
                return false;
            }
            if (update_size.getDataString("updateSize").length() != 0 && !update_size.getDataString("updateSize").equals(String.valueOf(files[0].getSize()))) {
                Log.e(TAG, "remote size is difference.");
                new File(FilePath).delete();
            }
            update_size.setDataString("updateSize", String.valueOf(files[0].getSize()));
            // 输出流
            outputStream = new FileOutputStream(localFile, true);
            ftpClient.setRestartOffset(localFile.length());
            Log.e(TAG, localFile.length() + "");
            inputStream = ftpClient.retrieveFileStream(new String(FileName.getBytes("GBK"),"iso-8859-1"));
            byte[] bytes = new byte[4096];
            int c;
            int finishSize = (int) localFile.length();
            while((c = inputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, c);
                finishSize += c;
                Log.d(TAG, "downSize======" + finishSize);
                if (finishSize == files[0].getSize()) {
                    Log.e(TAG, "download success!");
                    renameFile(FilePath, FilePath + ".apk");
//                            installAPK(MyApplication.getInstance().getContext(), new File(FilePath + ".apk"));
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Log.e(TAG, "close connect");
            try {
                //退出登陆FTP，关闭ftpClient的连接
                ftpClient.logout();
                ftpClient.disconnect();
                //关闭流
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
