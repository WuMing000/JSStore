package com.js.appstore.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.js.appstore.manager.HandlerManager;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {

    private static Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == 0x001) {
                String text = (String) msg.obj;
                Message message = new Message();
                message.what = HandlerManager.GET_DATA;
                message.obj = text;
            }
        }
    };

    public static void getAPPData() {
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
    }

}
