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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.activity.APPInformationActivity;
import com.js.appstore.adapter.ChoiceRecyclerViewAdapter;
import com.js.appstore.adapter.UserRecyclerViewAdapter;
import com.js.appstore.bean.APPLocalBean;
import com.js.appstore.bean.APPServerBean;
import com.js.appstore.manager.Contacts;
import com.js.appstore.utils.CustomUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

@SuppressLint({"NotifyDataSetChanged", "LongLogTag"})
public class RecreationFragment extends Fragment {

    private static final String TAG = "RecreationFragment==============>";

    private RecyclerView videoRecyclerView, cartoonRecyclerView, messageRecyclerView, hotspotRecyclerView, reelRecyclerView, broadcastRecyclerView, liveRecyclerView;
    private UserRecyclerViewAdapter videoRecyclerViewAdapter, cartoonRecyclerViewAdapter, hotspotRecyclerViewAdapter, liveRecyclerViewAdapter;
    private ChoiceRecyclerViewAdapter messageRecyclerViewAdapter, reelRecyclerViewAdapter, broadcastRecyclerViewAdapter;

    private List<APPLocalBean> videoList;
    private List<APPLocalBean> cartoonList;
    private List<APPLocalBean> messageList;
    private List<APPLocalBean> hotspotList;
    private List<APPLocalBean> reelList;
    private List<APPLocalBean> broadcastList;
    private List<APPLocalBean> liveList;

    private DownloadReceiver receiver;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0x001 :
                    initAPPData();
                    break;
                case 0x002 :
                    videoRecyclerViewAdapter.notifyDataSetChanged();
                    cartoonRecyclerViewAdapter.notifyDataSetChanged();
                    liveRecyclerViewAdapter.notifyDataSetChanged();
                    hotspotRecyclerViewAdapter.notifyDataSetChanged();
                    messageRecyclerViewAdapter.notifyDataSetChanged();
                    reelRecyclerViewAdapter.notifyDataSetChanged();
                    broadcastRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x003 :
                    try {
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
                                    if (Contacts.GET_VIDEO_INFORMATION.equals(url.split("/")[3])) {
                                        videoList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x004, 100);
                                    } else if (Contacts.GET_CARTOON_INFORMATION.equals(url.split("/")[3])) {
                                        cartoonList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x005, 100);
                                    } else if (Contacts.GET_MESSAGE_INFORMATION.equals(url.split("/")[3])) {
                                        messageList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x006, 100);
                                    } else if (Contacts.GET_HOTSPOT_INFORMATION.equals(url.split("/")[3])) {
                                        hotspotList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x007, 100);
                                    } else if (Contacts.GET_REEL_INFORMATION.equals(url.split("/")[3])) {
                                        reelList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x008, 100);
                                    } else if (Contacts.GET_BROADCAST_INFORMATION.equals(url.split("/")[3])) {
                                        broadcastList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x009, 100);
                                    } else if (Contacts.GET_LIVE_INFORMATION.equals(url.split("/")[3])) {
                                        liveList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                        handler.sendEmptyMessageAtTime(0x010, 100);
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
                    videoRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x005 :
                    cartoonRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x006 :
                    messageRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x007 :
                    hotspotRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x008 :
                    reelRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x009 :
                    broadcastRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x010 :
                    liveRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            videoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            cartoonRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            hotspotRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            liveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            messageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            reelRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            broadcastRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        } else {
            videoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            cartoonRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            hotspotRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            liveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            messageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            reelRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            broadcastRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }

//        videoRecyclerView.setAdapter(videoRecyclerViewAdapter);
//        cartoonRecyclerView.setAdapter(cartoonRecyclerViewAdapter);
//        hotspotRecyclerView.setAdapter(hotspotRecyclerViewAdapter);
//        liveRecyclerView.setAdapter(liveRecyclerViewAdapter);
//        messageRecyclerView.setAdapter(messageRecyclerViewAdapter);
//        reelRecyclerView.setAdapter(reelRecyclerViewAdapter);
//        broadcastRecyclerView.setAdapter(broadcastRecyclerViewAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "saveInstanceState");
        outState.putParcelableArrayList("videoList", (ArrayList<? extends Parcelable>) videoList);
        outState.putParcelableArrayList("cartoonList", (ArrayList<? extends Parcelable>) cartoonList);
        outState.putParcelableArrayList("messageList", (ArrayList<? extends Parcelable>) messageList);
        outState.putParcelableArrayList("hotspotList", (ArrayList<? extends Parcelable>) hotspotList);
        outState.putParcelableArrayList("reelList", (ArrayList<? extends Parcelable>) reelList);
        outState.putParcelableArrayList("broadcastList", (ArrayList<? extends Parcelable>) broadcastList);
        outState.putParcelableArrayList("liveList", (ArrayList<? extends Parcelable>) liveList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_recreation, null);

        videoList = new ArrayList<>();
        cartoonList = new ArrayList<>();
        messageList = new ArrayList<>();
        hotspotList = new ArrayList<>();
        reelList = new ArrayList<>();
        broadcastList = new ArrayList<>();
        liveList = new ArrayList<>();

        videoRecyclerView = inflate.findViewById(R.id.video_recycler_view);
        videoRecyclerView.addItemDecoration(new SpacesItemDecoration(10));

        cartoonRecyclerView = inflate.findViewById(R.id.cartoon_recycler_view);
        cartoonRecyclerView.addItemDecoration(new SpacesItemDecoration(10));

        messageRecyclerView = inflate.findViewById(R.id.message_recycler_view);

        hotspotRecyclerView = inflate.findViewById(R.id.hotspot_recycler_view);
        hotspotRecyclerView.addItemDecoration(new SpacesItemDecoration(10));

        reelRecyclerView = inflate.findViewById(R.id.reel_recycler_view);
        broadcastRecyclerView = inflate.findViewById(R.id.broadcast_recycler_view);

        liveRecyclerView = inflate.findViewById(R.id.live_recycler_view);
        liveRecyclerView.addItemDecoration(new SpacesItemDecoration(10));

        videoRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), videoList);
        cartoonRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), cartoonList);
        hotspotRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), hotspotList);
        liveRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), liveList);

        messageRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), messageList);
        reelRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), reelList);
        broadcastRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), broadcastList);

        Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            videoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            cartoonRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            hotspotRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            liveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            messageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            reelRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            broadcastRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        } else {
            videoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            cartoonRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            hotspotRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            liveRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            messageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            reelRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            broadcastRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }

        videoRecyclerView.setAdapter(videoRecyclerViewAdapter);
        cartoonRecyclerView.setAdapter(cartoonRecyclerViewAdapter);
        hotspotRecyclerView.setAdapter(hotspotRecyclerViewAdapter);
        liveRecyclerView.setAdapter(liveRecyclerViewAdapter);
        messageRecyclerView.setAdapter(messageRecyclerViewAdapter);
        reelRecyclerView.setAdapter(reelRecyclerViewAdapter);
        broadcastRecyclerView.setAdapter(broadcastRecyclerViewAdapter);

        if (savedInstanceState != null) {
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("videoList")) {
                videoList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("cartoonList")) {
                cartoonList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("messageList")) {
                messageList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("hotspotList")) {
                hotspotList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("reelList")) {
                reelList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("broadcastList")) {
                broadcastList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("liveList")) {
                liveList.add((APPLocalBean) parcelable);
            }
            handler.sendEmptyMessageAtTime(0x002, 100);
        } else {
            handler.sendEmptyMessageAtTime(0x001, 100);
        }
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
//                for (APPLocalBean appLocalBean : videoList) {
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
//                for (APPLocalBean appLocalBean : cartoonList) {
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
//                for (APPLocalBean appLocalBean : messageList) {
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
//                for (APPLocalBean appLocalBean : hotspotList) {
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
//                for (APPLocalBean appLocalBean : reelList) {
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
//                for (APPLocalBean appLocalBean : broadcastList) {
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
//                for (APPLocalBean appLocalBean : liveList) {
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
//            }
//        }.start();

        receiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter("js.download.progress");
        intentFilter.addAction("js.app.download.completed");
        intentFilter.addAction("js.app.install.completed");
        intentFilter.addAction("js.app.remove.completed");
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
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_VIDEO_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_CARTOON_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_MESSAGE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_HOTSPOT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_REEL_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_BROADCAST_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_LIVE_INFORMATION);
    }

    private void initOnClickListener() {

        videoRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) videoList.get(position));
                startActivity(intent);
            }
        });

        cartoonRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) cartoonList.get(position));
                startActivity(intent);
            }
        });

        hotspotRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) hotspotList.get(position));
                startActivity(intent);
            }
        });

        liveRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) liveList.get(position));
                startActivity(intent);
            }
        });

        messageRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) messageList.get(position));
                startActivity(intent);
            }
        });

        reelRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) reelList.get(position));
                startActivity(intent);
            }
        });

        broadcastRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) broadcastList.get(position));
                startActivity(intent);
            }
        });

        videoRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(videoList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", videoList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", videoList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(videoList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), videoList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(videoList.get(position).getAppState())) {
                    CustomUtil.openAPK(videoList.get(position).getAppPackage());
                }
            }
        });

        cartoonRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(cartoonList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", cartoonList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", cartoonList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(cartoonList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), cartoonList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(cartoonList.get(position).getAppState())) {
                    CustomUtil.openAPK(cartoonList.get(position).getAppPackage());
                }
            }
        });

        messageRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(messageList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", messageList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", messageList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(messageList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), messageList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(messageList.get(position).getAppState())) {
                    CustomUtil.openAPK(messageList.get(position).getAppPackage());
                }
            }
        });

        hotspotRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(hotspotList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", hotspotList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", hotspotList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(hotspotList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), hotspotList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(hotspotList.get(position).getAppState())) {
                    CustomUtil.openAPK(hotspotList.get(position).getAppPackage());
                }
            }
        });

        reelRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(reelList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", reelList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", reelList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(reelList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), reelList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(reelList.get(position).getAppState())) {
                    CustomUtil.openAPK(reelList.get(position).getAppPackage());
                }
            }
        });

        broadcastRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(broadcastList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", broadcastList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", broadcastList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(broadcastList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), broadcastList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(broadcastList.get(position).getAppState())) {
                    CustomUtil.openAPK(broadcastList.get(position).getAppPackage());
                }
            }
        });

        liveRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(liveList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", liveList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", liveList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(liveList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), liveList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(liveList.get(position).getAppState())) {
                    CustomUtil.openAPK(liveList.get(position).getAppPackage());
                }
            }
        });

    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, intent.getAction());
            if ("js.download.progress".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : videoList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        videoRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cartoonList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        cartoonRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : messageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        messageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : hotspotList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        hotspotRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : reelList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        reelRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : broadcastList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        broadcastRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : liveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        liveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : videoList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        videoRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cartoonList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        cartoonRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : messageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        messageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : hotspotList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        hotspotRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : reelList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        reelRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : broadcastList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        broadcastRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : liveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        liveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : videoList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        videoRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cartoonList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        cartoonRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : messageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        messageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : hotspotList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        hotspotRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : reelList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        reelRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : broadcastList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        broadcastRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : liveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        liveRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.remove.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : videoList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        videoRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : cartoonList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        cartoonRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : messageList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        messageRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : hotspotList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        hotspotRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : reelList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        reelRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : broadcastList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        broadcastRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : liveList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        liveRecyclerViewAdapter.notifyDataSetChanged();
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

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) % 3 == 1) {
                outRect.left = space;
                outRect.right = space;
            }
        }
    }

}
