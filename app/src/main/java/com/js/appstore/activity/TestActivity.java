package com.js.appstore.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.js.appstore.R;
import com.js.appstore.utils.DownloadUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestActivity extends AppCompatActivity {

    Handler handler = new Handler(Looper.myLooper()) {

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == 0x001) {
                String text = (String) msg.obj;
                tvInternet.setText(text);
//                ArrayList<APPServerBean> list = new Gson().fromJson(text, new TypeToken<List<APPServerBean>>() {
//                }.getType());
//                //Log.e("TAG", text);
//                Log.e("TAG", list.get(0).getAppIcon());

//                Glide.with(TestActivity.this).load(list.get(0).getAppIcon()).into(ivIcon);

            }
        }
    };

     private TextView tvInternet;
     private ImageView ivIcon;
     private Button btnDownload;
     private ProgressBar progressBar;
     private TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tvInternet = findViewById(R.id.tv_internet);
        ivIcon = findViewById(R.id.iv_icon);
        btnDownload = findViewById(R.id.btn_download);
        progressBar = findViewById(R.id.pb_progress);
        tvProgress = findViewById(R.id.tv_progress);
        new Thread() {
            @Override
            public void run() {
                super.run();
                //1.创建OkHttpClient对象
                OkHttpClient okHttpClient = new OkHttpClient();
                //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
                Request request = new Request.Builder().url("http://114.132.220.67:8086/getAPPData").method("GET",null).build();
                //3.创建一个call对象,参数就是Request请求对象
                Call call = okHttpClient.newCall(request);
                //4.请求加入调度，重写回调方法
                call.enqueue(new Callback() {
                    //请求失败执行的方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.e("TAG", "失败");
                    }
                    //请求成功执行的方法
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e("TAG", response.body().string());
                        String text = response.body().string();
                        //ArrayList<APPHomeBean> list = new Gson().fromJson(text, new TypeToken<List<APPHomeBean>>() {}.getType());
                        Log.e("TAG", text);
                        //Log.e("TAG", list.toString());
                        Message message = new Message();
                        message.what = 0x001;
                        message.obj = text;
                        handler.sendMessageAtTime(message, 100);
                    }
                });
            }
        }.start();

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadApk();
            }
        });
    }

    public void downloadApk() {
        DownloadUtil.get().download("http://114.132.220.67:8080/test/store/app/com.atomicadd.fotos.apk", "download", new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(final File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TestActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                        Log.e("LZ---apkpath", file.getAbsolutePath());//文件存储的路径
                        installAPK(TestActivity.this, file);
                    }
                });

            }

            @Override
            public void onDownloading(final float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat decimalFormat = new DecimalFormat( "##0.00 ");
                        String dd = decimalFormat.format(progress);
                        Log.e("TAG", progress + "");
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress((int) progress);//下载进度条
                        tvProgress.setText(dd + "");
                    }
                });

            }

            @Override
            public void onDownloadFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TestActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }


    /**
     * 安装APK内容
     */
    public void installAPK(Context mContext, File apkName) {
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
}