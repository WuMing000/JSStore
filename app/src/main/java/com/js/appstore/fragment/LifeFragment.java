package com.js.appstore.fragment;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.activity.APPInformationActivity;
import com.js.appstore.adapter.ChoiceRecyclerViewAdapter;
import com.js.appstore.adapter.UserRecyclerViewAdapter;
import com.js.appstore.bean.APPLocalBean;
import com.js.appstore.bean.APPServerBean;
import com.js.appstore.bean.RemoveBean;
import com.js.appstore.manager.Contacts;
import com.js.appstore.service.MyService;
import com.js.appstore.utils.CustomUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint({"NotifyDataSetChanged", "LongLogTag", "Range"})
public class LifeFragment extends Fragment {

    private static final String TAG = "LifeFragment==============>";

    private RecyclerView foodRecyclerView, shoppingRecyclerView, serveRecyclerView, practicalRecyclerView, sportRecyclerView, healthRecyclerView, investmentRecyclerView, bringRecyclerView;
    private UserRecyclerViewAdapter investmentRecyclerViewAdapter;
    private ChoiceRecyclerViewAdapter foodRecyclerViewAdapter, shoppingRecyclerViewAdapter, serveRecyclerViewAdapter, practicalRecyclerViewAdapter, sportRecyclerViewAdapter, healthRecyclerViewAdapter, bringRecyclerViewAdapter;

    private List<APPLocalBean> foodList;
    private List<APPLocalBean> shoppingList;
    private List<APPLocalBean> serveList;
    private List<APPLocalBean> practicalList;
    private List<APPLocalBean> sportList;
    private List<APPLocalBean> healthList;
    private List<APPLocalBean> investmentList;
    private List<APPLocalBean> bringList;

    private DownloadReceiver receiver;
    private DownloadManager downloadManager;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0x001 :
                    initAPPData();
                    break;
                case 0x002 :
                    investmentRecyclerViewAdapter.notifyDataSetChanged();
                    foodRecyclerViewAdapter.notifyDataSetChanged();
                    shoppingRecyclerViewAdapter.notifyDataSetChanged();
                    serveRecyclerViewAdapter.notifyDataSetChanged();
                    practicalRecyclerViewAdapter.notifyDataSetChanged();
                    sportRecyclerViewAdapter.notifyDataSetChanged();
                    healthRecyclerViewAdapter.notifyDataSetChanged();
                    bringRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x003 :
                    try {
                        Bundle bundle = (Bundle) msg.obj;
                        String text = bundle.getString("text");
                        String url = bundle.getString("url");
//                        Log.e(TAG, "text:" + text + ",url:" + url.split("/")[3]);
                        ArrayList<APPServerBean> list = new Gson().fromJson(text, new TypeToken<List<APPServerBean>>() {
                        }.getType());
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                for (APPServerBean appServerBean : list) {
                                    String appState = "下载";
                                    boolean appExists = CustomUtil.isAppInstalled(appServerBean.getAppPackage());
                                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appServerBean.getAppPackage() + ".apk");
                                    if (appExists) {
                                        appState = "打开";
                                    } else if (saveFile.exists()) {
                                        appState = "安装";
                                    }
                                    //定义一个对象，构建一行数据
                                    ContentValues values = new ContentValues();//用 value 表示一行
//                                        values.put("appId", appServerBean.getAppId());
                                    values.put("appName", appServerBean.getAppName());
                                    values.put("appPackage", appServerBean.getAppPackage());
                                    values.put("appInformation", appServerBean.getAppInformation());
                                    values.put("appIcon", appServerBean.getAppIcon());
                                    values.put("appDownLoadURL", appServerBean.getAppDownLoadURL());
                                    values.put("appIntroduce", appServerBean.getAppIntroduce());
                                    values.put("appPicture", appServerBean.getAppPicture());
                                    if (Contacts.GET_FOOD_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("FoodInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("FoodInformation", null, values);
                                            foodList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x004, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("FoodInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_SHOPPING_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ShoppingInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ShoppingInformation", null, values);
                                            shoppingList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x005, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ShoppingInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_SERVE_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ServeInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ServeInformation", null, values);
                                            serveList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x006, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ServeInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_PRACTICAL_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("PracticalInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("PracticalInformation", null, values);
                                            practicalList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x007, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("PracticalInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_SPORT_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("SportInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("SportInformation", null, values);
                                            sportList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x008, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("SportInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_HEALTH_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("HealthInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("HealthInformation", null, values);
                                            healthList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x009, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("HealthInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_INVESTMENT_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("InvestmentInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("InvestmentInformation", null, values);
                                            investmentList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x010, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("InvestmentInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_BRING_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("BringInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("BringInformation", null, values);
                                            bringList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x011, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("BringInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    }
                                }
//                                handler.sendEmptyMessageAtTime(0x002, 100);
                            }
                        }.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x004 :
                    foodRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x005 :
                    shoppingRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x006 :
                    serveRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x007 :
                    practicalRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x008 :
                    sportRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x009 :
                    healthRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x010 :
                    investmentRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x011 :
                    bringRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displayMetrics = MyApplication.getInstance().getContext().getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels == 1080 || heightPixels == 1080) {
            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                investmentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                shoppingRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                serveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                practicalRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                sportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                healthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                bringRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            } else {
                investmentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                shoppingRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                serveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                practicalRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                sportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                healthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                bringRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }
        } else {
            investmentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            shoppingRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            serveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            practicalRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            sportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            healthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            bringRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "saveInstanceState");
        outState.putParcelableArrayList("foodList", (ArrayList<? extends Parcelable>) foodList);
        outState.putParcelableArrayList("shoppingList", (ArrayList<? extends Parcelable>) shoppingList);
        outState.putParcelableArrayList("serveList", (ArrayList<? extends Parcelable>) serveList);
        outState.putParcelableArrayList("practicalList", (ArrayList<? extends Parcelable>) practicalList);
        outState.putParcelableArrayList("sportList", (ArrayList<? extends Parcelable>) sportList);
        outState.putParcelableArrayList("healthList", (ArrayList<? extends Parcelable>) healthList);
        outState.putParcelableArrayList("investmentList", (ArrayList<? extends Parcelable>) investmentList);
        outState.putParcelableArrayList("bringList", (ArrayList<? extends Parcelable>) bringList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_life, null);

        foodList = new ArrayList<>();
        shoppingList = new ArrayList<>();
        serveList = new ArrayList<>();
        practicalList = new ArrayList<>();
        sportList = new ArrayList<>();
        healthList = new ArrayList<>();
        investmentList = new ArrayList<>();
        bringList = new ArrayList<>();

        foodRecyclerView = inflate.findViewById(R.id.food_recycler_view);
        shoppingRecyclerView = inflate.findViewById(R.id.shopping_recycler_view);
        serveRecyclerView = inflate.findViewById(R.id.serve_recycler_view);
        practicalRecyclerView = inflate.findViewById(R.id.practical_recycler_view);
        sportRecyclerView = inflate.findViewById(R.id.sport_recycler_view);
        healthRecyclerView = inflate.findViewById(R.id.health_recycler_view);
        investmentRecyclerView = inflate.findViewById(R.id.investment_recycler_view);

        bringRecyclerView = inflate.findViewById(R.id.bring_recycler_view);

        investmentRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), investmentList);

        foodRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), foodList);
        shoppingRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), shoppingList);
        serveRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), serveList);
        practicalRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), practicalList);
        sportRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), sportList);
        healthRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), healthList);
        bringRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), bringList);

        DisplayMetrics displayMetrics = MyApplication.getInstance().getContext().getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels == 1080 || heightPixels == 1080) {
            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                investmentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                shoppingRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                serveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                practicalRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                sportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                healthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                bringRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                investmentRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            } else {
                investmentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                shoppingRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                serveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                practicalRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                sportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                healthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                bringRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                investmentRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            }
        } else {
            investmentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            shoppingRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            serveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            practicalRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            sportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            healthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            bringRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        investmentRecyclerView.setAdapter(investmentRecyclerViewAdapter);
        foodRecyclerView.setAdapter(foodRecyclerViewAdapter);
        shoppingRecyclerView.setAdapter(shoppingRecyclerViewAdapter);
        serveRecyclerView.setAdapter(serveRecyclerViewAdapter);
        practicalRecyclerView.setAdapter(practicalRecyclerViewAdapter);
        sportRecyclerView.setAdapter(sportRecyclerViewAdapter);
        healthRecyclerView.setAdapter(healthRecyclerViewAdapter);
        bringRecyclerView.setAdapter(bringRecyclerViewAdapter);

        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        if (savedInstanceState != null) {
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("foodList")) {
                foodList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("shoppingList")) {
                shoppingList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("serveList")) {
                serveList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("practicalList")) {
                practicalList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("sportList")) {
                sportList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("healthList")) {
                healthList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("investmentList")) {
                investmentList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("bringList")) {
                bringList.add((APPLocalBean) parcelable);
            }
            handler.sendEmptyMessageAtTime(0x002, 100);
        } else {
            handler.sendEmptyMessageAtTime(0x001, 100);
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query("FoodInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        foodList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x004, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("ShoppingInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        shoppingList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x005, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("ServeInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        serveList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x006, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("PracticalInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        practicalList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x007, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("SportInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        sportList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x008, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("HealthInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        healthList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x009, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("InvestmentInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        investmentList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x010, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("BringInformation", null, null, null, null, null, null);
                if(cursor.getCount() != 0) {
                    //循环遍历结果集，取出数据，显示出来
                    while (cursor.moveToNext()) {
                        int appId = cursor.getInt(cursor.getColumnIndex("appId"));
                        String appName = cursor.getString(cursor.getColumnIndex("appName"));
                        String appPackage = cursor.getString(cursor.getColumnIndex("appPackage"));
                        String appInformation = cursor.getString(cursor.getColumnIndex("appInformation"));
                        String appIcon = cursor.getString(cursor.getColumnIndex("appIcon"));
                        String appDownLoadURL = cursor.getString(cursor.getColumnIndex("appDownLoadURL"));
                        String appIntroduce = cursor.getString(cursor.getColumnIndex("appIntroduce"));
                        String appPicture = cursor.getString(cursor.getColumnIndex("appPicture"));
                        String appState = "下载";
                        boolean appExists = CustomUtil.isAppInstalled(appPackage);
                        File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appPackage + ".apk");
                        if (appExists) {
                            appState = "打开";
                        } else if (saveFile.exists()) {
                            appState = "安装";
                        }
                        bringList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x011, 100);
                    }
                }
                cursor.close();
            }
        }.start();

        initOnClickListener();

        return inflate;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                for (APPLocalBean appLocalBean : foodList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x004, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x004, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x004, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : shoppingList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x005, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x005, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x005, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : serveList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x006, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x006, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x006, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : practicalList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x007, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x007, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x007, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : sportList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x008, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x008, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x008, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : healthList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x009, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x009, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x009, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : investmentList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x010, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x010, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x010, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : bringList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x011, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x011, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x011, 100);
//                    }
//                }
//            }
//        }.start();

        receiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter("js.download.progress");
        intentFilter.addAction("js.app.download.completed");
        intentFilter.addAction("js.app.install.completed");
        intentFilter.addAction("js.app.remove.completed");
        intentFilter.addAction("js.app.again.download");
        MyApplication.getInstance().getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (receiver != null) {
            MyApplication.getInstance().getContext().unregisterReceiver(receiver);
        }
    }

    private void initAPPData() {
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_FOOD_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_SHOPPING_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_SERVE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PRACTICAL_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_SPORT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_HEALTH_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_INVESTMENT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_BRING_INFORMATION);
    }

    private void initOnClickListener() {

        investmentRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) investmentList.get(position));
                startActivity(intent);
            }
        });

        foodRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) foodList.get(position));
                startActivity(intent);
            }
        });

        shoppingRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) shoppingList.get(position));
                startActivity(intent);
            }
        });

        serveRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) serveList.get(position));
                startActivity(intent);
            }
        });

        practicalRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) practicalList.get(position));
                startActivity(intent);
            }
        });

        sportRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) sportList.get(position));
                startActivity(intent);
            }
        });

        healthRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) healthList.get(position));
                startActivity(intent);
            }
        });

        bringRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) bringList.get(position));
                startActivity(intent);
            }
        });

        foodRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", foodList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", foodList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), foodList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(foodList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", foodList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (foodList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        shoppingRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", shoppingList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", shoppingList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), shoppingList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(shoppingList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", shoppingList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (shoppingList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        serveRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", serveList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", serveList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), serveList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(serveList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", serveList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (serveList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        practicalRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", practicalList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", practicalList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), practicalList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(practicalList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", practicalList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (practicalList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        sportRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", sportList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", sportList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), sportList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(sportList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", sportList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (sportList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        healthRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", healthList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", healthList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), healthList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(healthList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", healthList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (healthList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        investmentRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", investmentList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", investmentList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), investmentList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(investmentList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", investmentList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (investmentList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        bringRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", bringList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", bringList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), bringList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(bringList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", bringList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (bringList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
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
//            Log.e(TAG, intent.getAction());
            if ("js.download.progress".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : foodList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        foodRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : shoppingList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        shoppingRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : serveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        serveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : practicalList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        practicalRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : sportList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        sportRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : healthList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        healthRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : investmentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        investmentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : bringList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        bringRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : foodList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        foodRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : shoppingList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        shoppingRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : serveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        serveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : practicalList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        practicalRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : sportList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        sportRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : healthList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        healthRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : investmentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        investmentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : bringList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        bringRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : foodList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        foodRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : shoppingList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        shoppingRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : serveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        serveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : practicalList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        practicalRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : sportList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        sportRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : healthList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        healthRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : investmentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        investmentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : bringList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        bringRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.again.download".equals(intent.getAction()) || "js.app.remove.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : foodList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        foodRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : shoppingList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        shoppingRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : serveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        serveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : practicalList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        practicalRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : sportList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        sportRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : healthList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        healthRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : investmentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        investmentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : bringList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        bringRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void getAPPData(String url) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //1.创建OkHttpClient对象
                OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(120000, TimeUnit.MILLISECONDS).readTimeout(120000, TimeUnit.MILLISECONDS).build();
                //2.创建Request对象，设置一个url地址,设置请求方式。
                Request request = new Request.Builder().url(url).method("GET",null).build();
                //3.创建一个call对象,参数就是Request请求对象
                Call call = okHttpClient.newCall(request);
                //4.请求加入调度，重写回调方法
                call.enqueue(new Callback() {
                    //请求失败执行的方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        getAPPData(url);
                        Log.e("TAG", "服务器异常，请求数据失败");
//                        handler.sendEmptyMessageAtTime(0x015, 100);
                    }
                    //请求成功执行的方法
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e("TAG", response.body().string());
                        String text = response.body().string();
                        //ArrayList<APPHomeBean> list = new Gson().fromJson(text, new TypeToken<List<APPHomeBean>>() {}.getType());
//                        Log.e("TAG", text);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("text", text);
                        bundle.putString("url", url);
                        message.what = 0x003;
                        message.obj = bundle;
                        handler.sendMessageAtTime(message, 100);
                    }
                });
            }
        }.start();
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

            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                if (parent.getChildLayoutPosition(view) % 3 == 1) {
                    outRect.left = space;
                    outRect.right = space;
                }
            } else {
                if (parent.getChildLayoutPosition(view) % 2 == 1) {
                    outRect.left = space;
                }
            }
            // Add top margin only for the first item to avoid double space between items
        }
    }
}
