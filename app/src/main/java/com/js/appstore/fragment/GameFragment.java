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
public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment==============>";

    private RecyclerView playfulRecyclerView, chessRecyclerView, puzzleRecyclerView, cardRecyclerView;
    private UserRecyclerViewAdapter playfulRecyclerViewAdapter, cardRecyclerViewAdapter;
    private ChoiceRecyclerViewAdapter puzzleRecyclerViewAdapter, chessRecyclerViewAdapter;

    private List<APPLocalBean> playList;
    private List<APPLocalBean> chessList;
    private List<APPLocalBean> puzzleList;
    private List<APPLocalBean> cardList;

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
                    playfulRecyclerViewAdapter.notifyDataSetChanged();
                    cardRecyclerViewAdapter.notifyDataSetChanged();
                    puzzleRecyclerViewAdapter.notifyDataSetChanged();
                    chessRecyclerViewAdapter.notifyDataSetChanged();
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
                                    if (Contacts.GET_PLAY_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("PlayInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("PlayInformation", null, values);
                                            playList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x004, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("PlayInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_CHESS_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ChessInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ChessInformation", null, values);
                                            chessList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x005, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ChessInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_PUZZLE_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("PuzzleInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("PuzzleInformation", null, values);
                                            puzzleList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x006, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("PuzzleInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_CARD_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("CardInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("CardInformation", null, values);
                                            cardList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x007, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("CardInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
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
                    playfulRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x005 :
                    chessRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x006 :
                    puzzleRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x007 :
                    cardRecyclerViewAdapter.notifyDataSetChanged();
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
                playfulRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                cardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                chessRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                puzzleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            } else {
                playfulRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                cardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                chessRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                puzzleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }
        } else {
            playfulRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            cardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            chessRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            puzzleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "saveInstanceState");
        outState.putParcelableArrayList("playList", (ArrayList<? extends Parcelable>) playList);
        outState.putParcelableArrayList("chessList", (ArrayList<? extends Parcelable>) chessList);
        outState.putParcelableArrayList("puzzleList", (ArrayList<? extends Parcelable>) puzzleList);
        outState.putParcelableArrayList("cardList", (ArrayList<? extends Parcelable>) cardList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_game, null);

        playList = new ArrayList<>();
        chessList = new ArrayList<>();
        puzzleList = new ArrayList<>();
        cardList = new ArrayList<>();

        playfulRecyclerView = inflate.findViewById(R.id.playful_recycler_view);
        chessRecyclerView = inflate.findViewById(R.id.chess_recycler_view);
        puzzleRecyclerView = inflate.findViewById(R.id.puzzle_recycler_view);
        cardRecyclerView = inflate.findViewById(R.id.card_recycler_view);

        playfulRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), playList);
        cardRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), cardList);

        puzzleRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), puzzleList);
        chessRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), chessList);

        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);

        DisplayMetrics displayMetrics = MyApplication.getInstance().getContext().getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels == 1080 || heightPixels == 1080) {
            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                playfulRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                cardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                chessRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                puzzleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                playfulRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                cardRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            } else {
                playfulRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                cardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                chessRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                puzzleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                playfulRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                cardRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            }
        } else {
            playfulRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            cardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            chessRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            puzzleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        playfulRecyclerView.setAdapter(playfulRecyclerViewAdapter);
        cardRecyclerView.setAdapter(cardRecyclerViewAdapter);
        chessRecyclerView.setAdapter(chessRecyclerViewAdapter);
        puzzleRecyclerView.setAdapter(puzzleRecyclerViewAdapter);

        if (savedInstanceState != null) {
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("playList")) {
                playList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("chessList")) {
                chessList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("puzzleList")) {
                puzzleList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("cardList")) {
                cardList.add((APPLocalBean) parcelable);
            }
            handler.sendEmptyMessageAtTime(0x002, 100);
        } else {
            handler.sendEmptyMessageAtTime(0x001, 100);
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query("PlayInformation", null, null, null, null, null, null);
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
                        playList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x004, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("ChessInformation", null, null, null, null, null, null);
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
                        chessList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x005, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("PuzzleInformation", null, null, null, null, null, null);
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
                        puzzleList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x006, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("CardInformation", null, null, null, null, null, null);
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
                        cardList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x007, 100);
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
//                for (APPLocalBean appLocalBean : playList) {
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
//                for (APPLocalBean appLocalBean : chessList) {
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
//                for (APPLocalBean appLocalBean : puzzleList) {
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
//                for (APPLocalBean appLocalBean : cardList) {
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
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PLAY_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_CHESS_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PUZZLE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_CARD_INFORMATION);
    }

    private void initOnClickListener() {

        playfulRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) playList.get(position));
                startActivity(intent);
            }
        });

        cardRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) cardList.get(position));
                startActivity(intent);
            }
        });

        puzzleRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) puzzleList.get(position));
                startActivity(intent);
            }
        });

        chessRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) chessList.get(position));
                startActivity(intent);
            }
        });

        playfulRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", playList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", playList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), playList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(playList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", playList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (playList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        chessRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", chessList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", chessList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), chessList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(chessList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", chessList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (chessList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        puzzleRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", puzzleList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", puzzleList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), puzzleList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(puzzleList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", puzzleList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (puzzleList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        cardRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", cardList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", cardList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), cardList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(cardList.get(position).getAppPackage());
                } else {
                    Intent intent = new Intent("js.app.again.download");
                    intent.putExtra("packageName", cardList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (cardList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
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
                for (APPLocalBean appLocalBean : playList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        playfulRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : chessList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        chessRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : puzzleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        puzzleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cardList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        cardRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : playList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        playfulRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : chessList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        chessRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : puzzleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        puzzleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cardList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        cardRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : playList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        playfulRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : chessList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        chessRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : puzzleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        puzzleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cardList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        cardRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.again.download".equals(intent.getAction()) || "js.app.remove.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : playList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        playfulRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : chessList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        chessRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : puzzleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        puzzleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cardList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        cardRecyclerViewAdapter.notifyDataSetChanged();
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
