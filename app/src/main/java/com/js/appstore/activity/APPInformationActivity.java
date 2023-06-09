package com.js.appstore.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.js.appstore.BaseActivity;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.adapter.ImageRecyclerViewAdapter;
import com.js.appstore.bean.APPLocalBean;
import com.js.appstore.bean.APPServerBean;
import com.js.appstore.bean.RemoveBean;
import com.js.appstore.service.MyService;
import com.js.appstore.utils.CustomUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint("LongLogTag")
public class APPInformationActivity extends BaseActivity {
    private static final String TAG = "APPInformationActivity===============>";

    LinearLayout llBack;
    private Button btnState;
    private ImageView ivIcon;
    private TextView tvAppName, tvAppApplication, tvIntroduce;
    private RecyclerView rvImageList;
    private ImageRecyclerViewAdapter adapter;
    private List<String> mList;

    private APPLocalBean appLocalBean;

    private DownloadReceiver downloadReceiver;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        CustomUtil.hideBottomUIMenu(this);
//        CustomUtil.setStatusBar(this);
        setContentView(R.layout.activity_appinformation);
        CustomUtil.hideNavigationBar(this);

        initData();
        initOnClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter("js.download.progress");
        intentFilter.addAction("js.app.download.completed");
        intentFilter.addAction("js.app.install.completed");
        intentFilter.addAction("js.app.remove.completed");
        intentFilter.addAction("js.app.again.download");
        registerReceiver(downloadReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        boolean appExists = CustomUtil.isAppInstalled(appLocalBean.getAppPackage());
//        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//        if (appExists) {
//            btnState.setText("打开");
//            appLocalBean.setAppState("打开");
//        } else if (saveFile.exists()) {
//            btnState.setText("安装");
//            appLocalBean.setAppState("安装");
//        } else {
//            btnState.setText("下载");
//            appLocalBean.setAppState("下载");
//        }
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
        llBack = findViewById(R.id.ll_back);
        btnState = findViewById(R.id.btn_state);
        ivIcon = findViewById(R.id.iv_icon);
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppApplication = findViewById(R.id.tv_app_information);
        tvIntroduce = findViewById(R.id.tv_introduce);

        rvImageList = findViewById(R.id.rv_image_list);
        rvImageList.addItemDecoration(new SpacesItemDecoration(30));

        mList = new ArrayList<>();
        adapter = new ImageRecyclerViewAdapter(this, mList);
        rvImageList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvImageList.setAdapter(adapter);

        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        Intent intent = getIntent();
        appLocalBean = (APPLocalBean) intent.getSerializableExtra("appHomeBean");
        btnState.setText(appLocalBean.getAppState());
        Glide.with(MyApplication.getInstance().getContext()).load(appLocalBean.getAppIcon()).into(ivIcon);
        tvAppName.setText(appLocalBean.getAppName());
        tvAppApplication.setText(appLocalBean.getAppInformation());
        tvIntroduce.setText(appLocalBean.getAppIntroduce());
        String appPicture = appLocalBean.getAppPicture();
        if (appPicture.length() != 0) {
            String[] split = appPicture.split(",");
            mList.addAll(Arrays.asList(split));
            adapter.notifyDataSetChanged();
        }
    }

    private void initOnClickListener() {

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", appLocalBean.getAppDownLoadURL());
                    intent.putExtra("packageName", appLocalBean.getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(appLocalBean.getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", appLocalBean.getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (appLocalBean.getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
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
//                if ("NaN".equals(intent.getStringExtra("progress"))) {
//                    btnState.setText("下载");
//                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                    btnState.setText("安装");
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                    btnState.setText("打开");
                }
            } else if ("js.app.again.download".equals(intent.getAction()) || "js.app.remove.completed".equals(intent.getAction())) {
                if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                    btnState.setText("下载");
                }
            }
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
//            outRect.left = space;
//            outRect.right = space;
//            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) != mList.size() - 1) {
                outRect.right = space;
            }
        }
    }
}