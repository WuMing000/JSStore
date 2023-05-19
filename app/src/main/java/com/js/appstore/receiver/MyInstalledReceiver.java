package com.js.appstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.js.appstore.MyApplication;

import java.io.File;

// 应用安装卸载广播接收者
public class MyInstalledReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
 
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {		// install
			String packageName = intent.getDataString();
			Log.i("homer", "安装了 :" + packageName);
			File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), packageName.split(":")[1] + ".apk");
			Log.e("homer", saveFile.getAbsolutePath());
			if (saveFile.exists()) {
				saveFile.delete();
				Toast.makeText(context, "安装成功，已删除安装包！", Toast.LENGTH_SHORT).show();
			}
		}

	}
}