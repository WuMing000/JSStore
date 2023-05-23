package com.js.appstore.activity;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.js.appstore.BaseActivity;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.utils.CustomUtil;

@SuppressLint("LongLogTag")
public class NetworkCheckActivity extends BaseActivity {

    AlertDialog dialog;
    private static final String TAG = "NetworkCheckActivity==========>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_check);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean networkAvailable = CustomUtil.isNetworkAvailable(MyApplication.getInstance().getContext());
        Log.e(TAG, networkAvailable + "");
        Bundle extras = getIntent().getExtras();
        if (networkAvailable) {
            Intent intent = new Intent(this, MainActivity.class);
            if (extras != null) {
                int position = extras.getInt("position");
                intent.putExtra("position", position);
            }
            startActivity(intent);
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            dialog = alertDialog.setMessage("请先连接网络")
                    .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CustomUtil.killAppProcess();
                        }
                    })
                    .setPositiveButton("设置网络", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                            startActivity(intent);
                        }
                    }).create();
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}