package com.js.appstore.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.js.appstore.BaseActivity;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.bean.APPLocalBean;
import com.js.appstore.bean.APPServerBean;
import com.js.appstore.utils.CustomUtil;

import java.io.File;

@SuppressLint("LongLogTag")
public class APPInformationActivity extends BaseActivity {
    private static final String TAG = "APPInformationActivity===============>";

    private Button btnBack, btnState;
    private ImageView ivIcon;
    private TextView tvAppName, tvAppApplication, tvIntroduce;

    private APPLocalBean appLocalBean;

    private DownloadReceiver downloadReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomUtil.hideBottomUIMenu(this);
        setContentView(R.layout.activity_appinformation);

        btnBack = findViewById(R.id.btn_back);
        btnState = findViewById(R.id.btn_state);
        ivIcon = findViewById(R.id.iv_icon);
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppApplication = findViewById(R.id.tv_app_information);
        tvIntroduce = findViewById(R.id.tv_introduce);

        initData();
        initOnClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter("js.download.progress");
        intentFilter.addAction("js.app.download.completed");
        registerReceiver(downloadReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean appExists = CustomUtil.isAppInstalled(appLocalBean.getAppPackage());
        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
        if (appExists) {
            btnState.setText("打开");
            appLocalBean.setAppState("打开");
        } else if (saveFile.exists()) {
            btnState.setText("安装");
            appLocalBean.setAppState("安装");
        } else {
            btnState.setText("下载");
            appLocalBean.setAppState("下载");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        appLocalBean = (APPLocalBean) intent.getSerializableExtra("appHomeBean");
        btnState.setText(appLocalBean.getAppState());
        Glide.with(MyApplication.getInstance().getContext()).load(appLocalBean.getAppIcon()).into(ivIcon);
        tvAppName.setText(appLocalBean.getAppName());
        tvAppApplication.setText(appLocalBean.getAppInformation());
        tvIntroduce.setText(appLocalBean.getAppIntroduce());
    }

    private void initOnClickListener() {

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("下载".equals(appLocalBean.getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", appLocalBean.getAppDownLoadURL());
                    intent.putExtra("packageName", appLocalBean.getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(appLocalBean.getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(appLocalBean.getAppState())) {
                    CustomUtil.openAPK(appLocalBean.getAppPackage());
                }
            }
        });

    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("js.download.progress".equals(intent.getAction())) {
                if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                    btnState.setText(intent.getStringExtra("progress") + "%");
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                    btnState.setText("安装");
                }
            }
        }
    }
}