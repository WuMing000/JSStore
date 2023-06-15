package com.js.appstore;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.js.appstore.manager.MyDBOpenHelper;
import com.js.appstore.service.MyService;

public class MyApplication extends Application {

    // 定义单态
    private static MyApplication singleton;

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public static MyApplication getInstance(){
        return singleton;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }

    public void setSqLiteDatabase(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        singleton = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(context, MyService.class));
        } else {
            startService(new Intent(this, MyService.class));
        }
        MyDBOpenHelper myDBOpenHelper = new MyDBOpenHelper(context);
        sqLiteDatabase = myDBOpenHelper.getWritableDatabase();
    }

}
