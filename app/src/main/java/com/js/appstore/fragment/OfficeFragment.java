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
public class OfficeFragment extends Fragment {

    private static final String TAG = "OfficeFragment==========>";

    private RecyclerView officeRecyclerView, documentRecyclerView, communicationRecyclerView, thoughtRecyclerView, instrumentRecyclerView, networkDiskRecyclerView;
    private UserRecyclerViewAdapter officeRecyclerViewAdapter, thoughtRecyclerViewAdapter, networkDiskRecyclerViewAdapter;
    private ChoiceRecyclerViewAdapter documentRecyclerViewAdapter, communicationRecyclerViewAdapter, instrumentRecyclerViewAdapter;

    private List<APPLocalBean> officeList;
    private List<APPLocalBean> documentList;
    private List<APPLocalBean> communicationList;
    private List<APPLocalBean> thoughtList;
    private List<APPLocalBean> instrumentList;
    private List<APPLocalBean> networkDiskList;

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
                    officeRecyclerViewAdapter.notifyDataSetChanged();
                    documentRecyclerViewAdapter.notifyDataSetChanged();
                    communicationRecyclerViewAdapter.notifyDataSetChanged();
                    instrumentRecyclerViewAdapter.notifyDataSetChanged();
                    thoughtRecyclerViewAdapter.notifyDataSetChanged();
                    networkDiskRecyclerViewAdapter.notifyDataSetChanged();
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
                                    if (Contacts.GET_OFFICE_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("OfficeInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("OfficeInformation", null, values);
                                            officeList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x004, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("OfficeInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_DOCUMENT_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("DocumentInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("DocumentInformation", null, values);
                                            documentList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x005, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("DocumentInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_COMMUNICATION_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("CommunicationInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("CommunicationInformation", null, values);
                                            communicationList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x006, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("CommunicationInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_THOUGHT_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ThoughtInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ThoughtInformation", null, values);
                                            thoughtList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x007, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ThoughtInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_INSTRUMENT_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("InstrumentInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("InstrumentInformation", null, values);
                                            instrumentList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x008, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("InstrumentInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_NETWORK_DISK_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("NetworkDiskInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("NetworkDiskInformation", null, values);
                                            networkDiskList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x009, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("NetworkDiskInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
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
                    officeRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x005 :
                    documentRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x006 :
                    communicationRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x007 :
                    thoughtRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x008 :
                    instrumentRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x009 :
                    networkDiskRecyclerViewAdapter.notifyDataSetChanged();
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
                officeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                thoughtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                networkDiskRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                instrumentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                documentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                communicationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            } else {
                officeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                thoughtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                networkDiskRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                instrumentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                documentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                communicationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }
        } else {
            officeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            thoughtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            networkDiskRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            instrumentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            documentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            communicationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "saveInstanceState");
        outState.putParcelableArrayList("officeList", (ArrayList<? extends Parcelable>) officeList);
        outState.putParcelableArrayList("documentList", (ArrayList<? extends Parcelable>) documentList);
        outState.putParcelableArrayList("communicationList", (ArrayList<? extends Parcelable>) communicationList);
        outState.putParcelableArrayList("thoughtList", (ArrayList<? extends Parcelable>) thoughtList);
        outState.putParcelableArrayList("instrumentList", (ArrayList<? extends Parcelable>) instrumentList);
        outState.putParcelableArrayList("networkDiskList", (ArrayList<? extends Parcelable>) networkDiskList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_office, null);

        officeList = new ArrayList<>();
        documentList = new ArrayList<>();
        communicationList = new ArrayList<>();
        thoughtList = new ArrayList<>();
        instrumentList = new ArrayList<>();
        networkDiskList = new ArrayList<>();

        officeRecyclerView = inflate.findViewById(R.id.office_recycler_view);
        documentRecyclerView = inflate.findViewById(R.id.document_recycler_view);
        communicationRecyclerView = inflate.findViewById(R.id.communication_recycler_view);

        thoughtRecyclerView = inflate.findViewById(R.id.thought_recycler_view);

        instrumentRecyclerView = inflate.findViewById(R.id.instrument_recycler_view);

        networkDiskRecyclerView = inflate.findViewById(R.id.network_disk_recycler_view);

        officeRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), officeList);
        thoughtRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), thoughtList);
        networkDiskRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), networkDiskList);

        documentRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), documentList);
        communicationRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), communicationList);
        instrumentRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), instrumentList);

        DisplayMetrics displayMetrics = MyApplication.getInstance().getContext().getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels == 1080 || heightPixels == 1080) {
            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                officeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                thoughtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                networkDiskRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                instrumentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                documentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                communicationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                officeRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                thoughtRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                networkDiskRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            } else {
                officeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                thoughtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                networkDiskRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                instrumentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                documentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                communicationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                officeRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                thoughtRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                networkDiskRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            }
        } else {
            officeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            thoughtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            networkDiskRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            instrumentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            documentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            communicationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        officeRecyclerView.setAdapter(officeRecyclerViewAdapter);
        thoughtRecyclerView.setAdapter(thoughtRecyclerViewAdapter);
        networkDiskRecyclerView.setAdapter(networkDiskRecyclerViewAdapter);
        instrumentRecyclerView.setAdapter(instrumentRecyclerViewAdapter);
        documentRecyclerView.setAdapter(documentRecyclerViewAdapter);
        communicationRecyclerView.setAdapter(communicationRecyclerViewAdapter);

        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        if (savedInstanceState != null) {
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("officeList")) {
                officeList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("documentList")) {
                 documentList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("communicationList")) {
                communicationList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("thoughtList")) {
                thoughtList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("instrumentList")) {
                instrumentList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("networkDiskList")) {
                networkDiskList.add((APPLocalBean) parcelable);
            }
            handler.sendEmptyMessageAtTime(0x002, 100);
        } else {
            handler.sendEmptyMessageAtTime(0x001, 100);
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query("OfficeInformation", null, null, null, null, null, null);
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
                        officeList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x004, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("DocumentInformation", null, null, null, null, null, null);
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
                        documentList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x005, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("CommunicationInformation", null, null, null, null, null, null);
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
                        communicationList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x006, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("ThoughtInformation", null, null, null, null, null, null);
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
                        thoughtList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x007, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("InstrumentInformation", null, null, null, null, null, null);
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
                        instrumentList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x008, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("NetworkDiskInformation", null, null, null, null, null, null);
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
                        networkDiskList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x009, 100);
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
//                for (APPLocalBean appLocalBean : officeList) {
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
//                for (APPLocalBean appLocalBean : documentList) {
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
//                for (APPLocalBean appLocalBean : communicationList) {
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
//                for (APPLocalBean appLocalBean : thoughtList) {
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
//                for (APPLocalBean appLocalBean : instrumentList) {
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
//                for (APPLocalBean appLocalBean : networkDiskList) {
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
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_OFFICE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_DOCUMENT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_COMMUNICATION_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_THOUGHT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_INSTRUMENT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_NETWORK_DISK_INFORMATION);
    }

    private void initOnClickListener() {

        officeRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) officeList.get(position));
                startActivity(intent);
            }
        });

        thoughtRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) thoughtList.get(position));
                startActivity(intent);
            }
        });

        networkDiskRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) networkDiskList.get(position));
                startActivity(intent);
            }
        });

        documentRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) documentList.get(position));
                startActivity(intent);
            }
        });

        communicationRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) communicationList.get(position));
                startActivity(intent);
            }
        });

        instrumentRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) instrumentList.get(position));
                startActivity(intent);
            }
        });

        officeRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", officeList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", officeList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), officeList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(officeList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", officeList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (officeList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        documentRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", documentList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", documentList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), documentList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(documentList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", documentList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (documentList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        communicationRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", communicationList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", communicationList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), communicationList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(communicationList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", communicationList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (communicationList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        thoughtRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", thoughtList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", thoughtList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), thoughtList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(thoughtList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", thoughtList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (thoughtList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        instrumentRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", instrumentList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", instrumentList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), instrumentList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(instrumentList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", instrumentList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (instrumentList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        networkDiskRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", networkDiskList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", networkDiskList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), networkDiskList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(networkDiskList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", networkDiskList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (networkDiskList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
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
                for (APPLocalBean appLocalBean : officeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        officeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : documentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        documentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : communicationList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        communicationRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : thoughtList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        thoughtRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : instrumentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        instrumentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : networkDiskList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        networkDiskRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : officeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        officeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : documentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        documentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : communicationList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        communicationRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : thoughtList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        thoughtRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : instrumentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        instrumentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : networkDiskList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        networkDiskRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : officeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        officeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : documentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        documentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : communicationList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        communicationRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : thoughtList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        thoughtRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : instrumentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        instrumentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : networkDiskList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        networkDiskRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.again.download".equals(intent.getAction()) || "js.app.remove.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : officeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        officeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : documentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        documentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : communicationList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        communicationRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : thoughtList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        thoughtRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : instrumentList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        instrumentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : networkDiskList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        networkDiskRecyclerViewAdapter.notifyDataSetChanged();
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
                        Log.e("TAG", "服务器异常，请求数据失败");
                        getAPPData(url);
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
