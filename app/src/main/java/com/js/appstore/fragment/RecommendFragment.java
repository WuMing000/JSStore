package com.js.appstore.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.activity.APPInformationActivity;
import com.js.appstore.adapter.ChoiceRecyclerViewAdapter;
import com.js.appstore.adapter.TopRecyclerViewAdapter;
import com.js.appstore.adapter.UserRecyclerViewAdapter;
import com.js.appstore.bean.APPLocalBean;
import com.js.appstore.bean.APPServerBean;
import com.js.appstore.manager.Contacts;
import com.js.appstore.manager.SmoothLinearLayoutManager;
import com.js.appstore.utils.CustomUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("NotifyDataSetChanged")
public class RecommendFragment extends Fragment {

    private static final String TAG = "RecommendFragment=====>";

    private RecyclerView bannerRecyclerView, userRecyclerView, choiceRecyclerView, watchRecyclerview, barrageRecyclerView, relaxRecyclerView, workpieceRecyclerView, newsRecyclerView, readRecyclerView;
    private UserRecyclerViewAdapter userRecyclerViewAdapter, relaxRecyclerViewAdapter;
    private ChoiceRecyclerViewAdapter choiceRecyclerViewAdapter, watchRecyclerViewAdapter, barrageRecyclerViewAdapter, workpieceRecyclerViewAdapter, newsRecyclerViewAdapter, readRecyclerViewAdapter;

    private TopRecyclerViewAdapter bannerAdapter;

    private List<APPLocalBean> bannerList;
    private List<APPLocalBean> userList;
    private List<APPLocalBean> choiceList;
    private List<APPLocalBean> watchList;
    private List<APPLocalBean> barrageList;
    private List<APPLocalBean> relaxList;
    private List<APPLocalBean> workpieceList;
    private List<APPLocalBean> newsList;
    private List<APPLocalBean> readList;

    private List<View> pointViews;
    private LinearLayout llPoint;

    private SmoothLinearLayoutManager linearLayoutManager;

    private RecyclerView.OnScrollListener bannerOnScrollListener;
    private ScheduledExecutorService scheduledExecutorService;

    private DownloadReceiver receiver;

    int i;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0x001 :
                    initAPPData();
                    break;
                case 0x002 :
                    userRecyclerViewAdapter.notifyDataSetChanged();
                    relaxRecyclerViewAdapter.notifyDataSetChanged();
                    bannerAdapter.notifyDataSetChanged();
                    choiceRecyclerViewAdapter.notifyDataSetChanged();
                    watchRecyclerViewAdapter.notifyDataSetChanged();
                    barrageRecyclerViewAdapter.notifyDataSetChanged();
                    workpieceRecyclerViewAdapter.notifyDataSetChanged();
                    newsRecyclerViewAdapter.notifyDataSetChanged();
                    readRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x003 :
                    i = 0;
                    getPointData();
                    if (llPoint.getChildAt(0) != null) {
                        llPoint.getChildAt(0).setEnabled(true);
                    }
                    scheduledExecutorService = Executors.newScheduledThreadPool(1);
                    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            bannerRecyclerView.smoothScrollToPosition(linearLayoutManager.findFirstVisibleItemPosition() + 1);
                        }
                    }, 5000, 5000, TimeUnit.MILLISECONDS);
                    bannerOnScrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            if (newState == RecyclerView.SCROLL_STATE_IDLE && bannerList.size() != 0) {
                                llPoint.getChildAt(i).setEnabled(false);
                                i = linearLayoutManager.findFirstVisibleItemPosition() % bannerList.size();
                                //得到指示器红点的位置
                                llPoint.getChildAt(i).setEnabled(true);
                            }
                        }
                    };
                    bannerRecyclerView.addOnScrollListener(bannerOnScrollListener);
                    break;
                case 0x004 :
                    if (bannerOnScrollListener != null) {
                        bannerRecyclerView.removeOnScrollListener(bannerOnScrollListener);
                        scheduledExecutorService.shutdown();
                    }
                    break;
                case 0x005 :
                    Bundle bundle = (Bundle) msg.obj;
                    String text = bundle.getString("text");
                    String url = bundle.getString("url");
                    Log.e(TAG, "text:" + text + ",url:" + url.split("/")[3]);
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
                                if (Contacts.GET_BANNER_INFORMATION.equals(url.split("/")[3])) {
                                    bannerList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x016, 100);
                                } else if (Contacts.GET_USER_INFORMATION.equals(url.split("/")[3])) {
                                    userList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x007, 100);
                                } else if (Contacts.GET_CHOICE_INFORMATION.equals(url.split("/")[3])) {
                                    choiceList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x008, 100);
                                } else if (Contacts.GET_WATCH_INFORMATION.equals(url.split("/")[3])) {
                                    watchList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x009, 100);
                                } else if (Contacts.GET_BARRAGE_INFORMATION.equals(url.split("/")[3])) {
                                    barrageList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x010, 100);
                                } else if (Contacts.GET_RELAX_INFORMATION.equals(url.split("/")[3])) {
                                    relaxList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x011, 100);
                                } else if (Contacts.GET_WORKPIECE_INFORMATION.equals(url.split("/")[3])) {
                                    workpieceList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x012, 100);
                                } else if (Contacts.GET_NEWS_INFORMATION.equals(url.split("/")[3])) {
                                    newsList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x013, 100);
                                } else if (Contacts.GET_READ_INFORMATION.equals(url.split("/")[3])) {
                                    readList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                            appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                    handler.sendEmptyMessageAtTime(0x014, 100);
                                }
                            }
                            if (Contacts.GET_BANNER_INFORMATION.equals(url.split("/")[3])) {
                                handler.sendEmptyMessageAtTime(0x003, 100);
                            }
//                            handler.sendEmptyMessageAtTime(0x002, 100);
                        }
                    }.start();
                    break;
                case 0x006 :
//                    downloadProgress();
                    break;
                case 0x007 :
                    userRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x008 :
                    choiceRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x009 :
                    watchRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x010 :
                    barrageRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x011 :
                    relaxRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x012 :
                    workpieceRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x013 :
                    newsRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x014 :
                    readRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x015 :
                    Toast.makeText(MyApplication.getInstance().getContext(), "服务器异常，请求数据失败", Toast.LENGTH_SHORT).show();
                    break;
                case 0x016 :
                    bannerAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

//    private Runnable initAppDataRunnable = new Runnable() {
//        @Override
//        public void run() {
//            handler.sendEmptyMessageAtTime(0x001, 100);
//        }
//    };


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
                userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                choiceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                watchRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 6));
                barrageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                relaxRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                workpieceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                newsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                readRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            } else {
                userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                choiceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                watchRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 3));
                barrageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                relaxRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                workpieceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                newsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                readRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }
        } else {
            userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            choiceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            watchRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 2));
            barrageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            relaxRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            workpieceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            newsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            readRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "saveInstanceState");
        outState.putParcelableArrayList("userList", (ArrayList<? extends Parcelable>) userList);
        outState.putParcelableArrayList("bannerList", (ArrayList<? extends Parcelable>) bannerList);
        outState.putParcelableArrayList("choiceList", (ArrayList<? extends Parcelable>) choiceList);
        outState.putParcelableArrayList("watchList", (ArrayList<? extends Parcelable>) watchList);
        outState.putParcelableArrayList("barrageList", (ArrayList<? extends Parcelable>) barrageList);
        outState.putParcelableArrayList("relaxList", (ArrayList<? extends Parcelable>) relaxList);
        outState.putParcelableArrayList("workpieceList", (ArrayList<? extends Parcelable>) workpieceList);
        outState.putParcelableArrayList("newsList", (ArrayList<? extends Parcelable>) newsList);
        outState.putParcelableArrayList("readList", (ArrayList<? extends Parcelable>) readList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        userList = new ArrayList<>();
        bannerList = new ArrayList<>();
        choiceList = new ArrayList<>();
        watchList = new ArrayList<>();
        barrageList = new ArrayList<>();
        relaxList = new ArrayList<>();
        workpieceList = new ArrayList<>();
        newsList = new ArrayList<>();
        readList = new ArrayList<>();

        View inflate = inflater.inflate(R.layout.fragment_recommend, null);

        bannerRecyclerView = inflate.findViewById(R.id.recommend_recycler);

        userRecyclerView = inflate.findViewById(R.id.user_recycler_view);

        choiceRecyclerView = inflate.findViewById(R.id.choice_recycler_view);
        watchRecyclerview = inflate.findViewById(R.id.watch_recycler_view);
        barrageRecyclerView = inflate.findViewById(R.id.barrage_recycler_view);

        relaxRecyclerView = inflate.findViewById(R.id.relax_recycler_view);

        workpieceRecyclerView = inflate.findViewById(R.id.workpiece_recycler_view);
        newsRecyclerView = inflate.findViewById(R.id.news_recycler_view);
        readRecyclerView = inflate.findViewById(R.id.read_recycler_view);
        llPoint = inflate.findViewById(R.id.ll_point);

        bannerAdapter = new TopRecyclerViewAdapter(getContext(), bannerList);
        userRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), userList);
        choiceRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), choiceList);
        watchRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), watchList);
        barrageRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), barrageList);
        relaxRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), relaxList);
        workpieceRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), workpieceList);
        newsRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), newsList);
        readRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), readList);

        linearLayoutManager = new SmoothLinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        bannerRecyclerView.setLayoutManager(linearLayoutManager);
        bannerRecyclerView.setAdapter(bannerAdapter);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(bannerRecyclerView);

        DisplayMetrics displayMetrics = MyApplication.getInstance().getContext().getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels == 1080 || heightPixels == 1080) {
            Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                choiceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                watchRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 6));
                barrageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                relaxRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                workpieceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                newsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                readRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
                userRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                relaxRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            } else {
                userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                choiceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                watchRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 3));
                barrageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                relaxRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                workpieceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                newsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                readRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                userRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                relaxRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            }
        } else {
            userRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            choiceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            watchRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 2));
            barrageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            relaxRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            workpieceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            newsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            readRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        userRecyclerView.setAdapter(userRecyclerViewAdapter);
        choiceRecyclerView.setAdapter(choiceRecyclerViewAdapter);
        watchRecyclerview.setAdapter(watchRecyclerViewAdapter);
        barrageRecyclerView.setAdapter(barrageRecyclerViewAdapter);
        relaxRecyclerView.setAdapter(relaxRecyclerViewAdapter);
        workpieceRecyclerView.setAdapter(workpieceRecyclerViewAdapter);
        newsRecyclerView.setAdapter(newsRecyclerViewAdapter);
        readRecyclerView.setAdapter(readRecyclerViewAdapter);

        if (savedInstanceState != null) {
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("bannerList")) {
                bannerList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("userList")) {
                userList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("choiceList")) {
                choiceList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("watchList")) {
                watchList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("barrageList")) {
                barrageList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("relaxList")) {
                relaxList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("workpieceList")) {
                workpieceList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("newsList")) {
                newsList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("readList")) {
                readList.add((APPLocalBean) parcelable);
            }
            handler.sendEmptyMessageAtTime(0x002, 100);
            handler.sendEmptyMessageAtTime(0x003, 100);
        } else {
            handler.sendEmptyMessageAtTime(0x001, 100);
        }
        onClickListener();
        return inflate;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        receiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter("js.download.progress");
        intentFilter.addAction("js.app.download.completed");
        intentFilter.addAction("js.app.install.completed");
        intentFilter.addAction("js.app.remove.completed");
        MyApplication.getInstance().getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        handler.sendEmptyMessageAtTime(0x004, 100);
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
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_BANNER_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_USER_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_CHOICE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_WATCH_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_BARRAGE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_RELAX_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_WORKPIECE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_NEWS_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_READ_INFORMATION);
    }

    private void onClickListener() {
        bannerAdapter.setOnItemClickListener(new TopRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, bannerList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) bannerList.get(position));
                startActivity(intent);
            }
        });

        userRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, userList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) userList.get(position));
                startActivity(intent);
            }
        });

        relaxRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, relaxList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) relaxList.get(position));
                startActivity(intent);
            }
        });

        choiceRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, choiceList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) choiceList.get(position));
                startActivity(intent);
            }
        });

        watchRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, watchList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) watchList.get(position));
                startActivity(intent);
            }
        });

        barrageRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, barrageList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) barrageList.get(position));
                startActivity(intent);
            }
        });

        workpieceRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, workpieceList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) workpieceList.get(position));
                startActivity(intent);
            }
        });

        newsRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, newsList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) newsList.get(position));
                startActivity(intent);
            }
        });

        readRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, readList.get(position).toString());
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) readList.get(position));
                startActivity(intent);
            }
        });

        userRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(userList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", userList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", userList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(userList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), userList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(userList.get(position).getAppState())) {
                    CustomUtil.openAPK(userList.get(position).getAppPackage());
                }
            }
        });

        choiceRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(choiceList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", choiceList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", choiceList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(choiceList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), choiceList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(choiceList.get(position).getAppState())) {
                    CustomUtil.openAPK(choiceList.get(position).getAppPackage());
                }
            }
        });

        watchRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(watchList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", watchList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", watchList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(watchList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), watchList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(watchList.get(position).getAppState())) {
                    CustomUtil.openAPK(watchList.get(position).getAppPackage());
                }
            }
        });

        barrageRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(barrageList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", barrageList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", barrageList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(barrageList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), barrageList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(barrageList.get(position).getAppState())) {
                    CustomUtil.openAPK(barrageList.get(position).getAppPackage());
                }
            }
        });

        relaxRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(relaxList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", relaxList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", relaxList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(relaxList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), relaxList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(relaxList.get(position).getAppState())) {
                    CustomUtil.openAPK(relaxList.get(position).getAppPackage());
                }
            }
        });

        workpieceRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(workpieceList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", workpieceList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", workpieceList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(workpieceList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), workpieceList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(workpieceList.get(position).getAppState())) {
                    CustomUtil.openAPK(workpieceList.get(position).getAppPackage());
                }
            }
        });

        newsRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(newsList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", newsList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", newsList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(newsList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), newsList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(newsList.get(position).getAppState())) {
                    CustomUtil.openAPK(newsList.get(position).getAppPackage());
                }
            }
        });

        readRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(readList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", readList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", readList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(readList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), readList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(readList.get(position).getAppState())) {
                    CustomUtil.openAPK(readList.get(position).getAppPackage());
                }
            }
        });
    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, intent.getAction());
            if ("js.download.progress".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : userList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        userRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : choiceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        choiceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : watchList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        watchRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : barrageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        barrageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : relaxList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        relaxRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : workpieceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        workpieceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : newsList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        newsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : readList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        readRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : userList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        userRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : choiceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        choiceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : watchList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        watchRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : barrageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        barrageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : relaxList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        relaxRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : workpieceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        workpieceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : newsList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        newsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : readList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        readRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : userList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        userRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : choiceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        choiceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : watchList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        watchRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : barrageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        barrageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : relaxList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        relaxRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : workpieceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        workpieceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : newsList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        newsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : readList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        readRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.remove.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : userList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        userRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : choiceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        choiceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : watchList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        watchRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : barrageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        barrageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : relaxList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        relaxRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : workpieceList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        workpieceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : newsList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        newsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : readList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        readRecyclerViewAdapter.notifyDataSetChanged();
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
                OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS).readTimeout(60000, TimeUnit.MILLISECONDS).build();
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
                        handler.sendEmptyMessageAtTime(0x015, 100);
                    }
                    //请求成功执行的方法
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e("TAG", response.body().string());
                        String text = response.body().string();
                        //ArrayList<APPHomeBean> list = new Gson().fromJson(text, new TypeToken<List<APPHomeBean>>() {}.getType());
                        Log.e("TAG", text);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("text", text);
                        bundle.putString("url", url);
                        message.what = 0x005;
                        message.obj = bundle;
                        handler.sendMessageAtTime(message, 100);
                    }
                });
            }
        }.start();
    }

    /**
     * 获取数据
     */
    private void getPointData() {
        try {
            pointViews = new ArrayList<>();
            View view;
            if (llPoint != null) {
                llPoint.removeAllViews();
            }
            pointViews.clear();
            for (int i = 0; i < bannerList.size(); i++) {
                //创建底部指示器(小圆点)
                view = new View(getContext());
                view.setBackgroundResource(R.drawable.background_point);
                view.setEnabled(false);
                //设置宽高
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, 30);
                //设置间隔
                layoutParams.leftMargin = 50;
                //添加到LinearLayout
                llPoint.addView(view, layoutParams);
                pointViews.add(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
