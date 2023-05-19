package com.js.appstore.manager;

import android.os.Handler;

// 全局handler，使用线程池进行保存和获取
public class HandlerManager {
    public static final int GET_DATA = 0;
    public static ThreadLocal<Handler> threadLocal = new ThreadLocal<Handler>();

    public static Handler getHandler() {
        return threadLocal.get();
    }
 
    public static void putHandler(Handler value) {
        threadLocal.set(value);//UiThread  id
    }
}