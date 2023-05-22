package com.js.appstore.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
public class EducationFragment extends Fragment {

    private static final String TAG = "EducationFragment==============>";

    private RecyclerView classroomRecyclerView, textualRecyclerView, englishRecyclerView, translateRecyclerView, teacherRecyclerView, childrenRecyclerView, pictureRecyclerView, paintRecyclerView, enlightenRecyclerView, preschoolRecyclerView, middleRecyclerView, programmeRecyclerView;
    private UserRecyclerViewAdapter classroomRecyclerViewAdapter, teacherRecyclerViewAdapter, enlightenRecyclerViewAdapter, programmeRecyclerViewAdapter;
    private ChoiceRecyclerViewAdapter textualRecyclerViewAdapter, englishRecyclerViewAdapter, translateRecyclerViewAdapter, childrenRecyclerViewAdapter, pictureRecyclerViewAdapter, paintRecyclerViewAdapter, preschoolRecyclerViewAdapter, middleRecyclerViewAdapter;

    private List<APPLocalBean> classroomList;
    private List<APPLocalBean> textualList;
    private List<APPLocalBean> englishList;
    private List<APPLocalBean> translateList;
    private List<APPLocalBean> teacherList;
    private List<APPLocalBean> childrenList;
    private List<APPLocalBean> pictureList;
    private List<APPLocalBean> paintList;
    private List<APPLocalBean> enlightenList;
    private List<APPLocalBean> preschoolList;
    private List<APPLocalBean> middleList;
    private List<APPLocalBean> programmeList;

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
                    classroomRecyclerViewAdapter.notifyDataSetChanged();
                    teacherRecyclerViewAdapter.notifyDataSetChanged();
                    enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    programmeRecyclerViewAdapter.notifyDataSetChanged();
                    textualRecyclerViewAdapter.notifyDataSetChanged();
                    englishRecyclerViewAdapter.notifyDataSetChanged();
                    translateRecyclerViewAdapter.notifyDataSetChanged();
                    childrenRecyclerViewAdapter.notifyDataSetChanged();
                    pictureRecyclerViewAdapter.notifyDataSetChanged();
                    paintRecyclerViewAdapter.notifyDataSetChanged();
                    preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    middleRecyclerViewAdapter.notifyDataSetChanged();
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
                                    if (Contacts.GET_CLASSROOM_INFORMATION.equals(url.split("/")[3])) {
                                        classroomList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x004, 100);
                                    } else if (Contacts.GET_TEXTUAL_INFORMATION.equals(url.split("/")[3])) {
                                        textualList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x005, 100);
                                    } else if (Contacts.GET_ENGLISH_INFORMATION.equals(url.split("/")[3])) {
                                        englishList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x006, 100);
                                    } else if (Contacts.GET_TRANSLATE_INFORMATION.equals(url.split("/")[3])) {
                                        translateList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x007, 100);
                                    } else if (Contacts.GET_TEACHER_INFORMATION.equals(url.split("/")[3])) {
                                        teacherList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x008, 100);
                                    } else if (Contacts.GET_CHILDREN_INFORMATION.equals(url.split("/")[3])) {
                                        childrenList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x009, 100);
                                    } else if (Contacts.GET_PICTURE_INFORMATION.equals(url.split("/")[3])) {
                                        pictureList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x010, 100);
                                    } else if (Contacts.GET_PAINT_INFORMATION.equals(url.split("/")[3])) {
                                        paintList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x011, 100);
                                    } else if (Contacts.GET_ENLIGHTEN_INFORMATION.equals(url.split("/")[3])) {
                                        enlightenList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x012, 100);
                                    } else if (Contacts.GET_PRESCHOOL_INFORMATION.equals(url.split("/")[3])) {
                                        preschoolList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x013, 100);
                                    } else if (Contacts.GET_MIDDLE_INFORMATION.equals(url.split("/")[3])) {
                                        middleList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x014, 100);
                                    } else if (Contacts.GET_PROGRAMME_INFORMATION.equals(url.split("/")[3])) {
                                        programmeList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(), appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appState));
                                        handler.sendEmptyMessageAtTime(0x015, 100);
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
                    classroomRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x005 :
                    textualRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x006 :
                    englishRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x007 :
                    translateRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x008 :
                    teacherRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x009 :
                    childrenRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x010 :
                    pictureRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x011 :
                    paintRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x012 :
                    enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x013 :
                    preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x014 :
                    middleRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case 0x015 :
                    programmeRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "saveInstanceState");
        outState.putParcelableArrayList("classroomList", (ArrayList<? extends Parcelable>) classroomList);
        outState.putParcelableArrayList("textualList", (ArrayList<? extends Parcelable>) textualList);
        outState.putParcelableArrayList("englishList", (ArrayList<? extends Parcelable>) englishList);
        outState.putParcelableArrayList("translateList", (ArrayList<? extends Parcelable>) translateList);
        outState.putParcelableArrayList("teacherList", (ArrayList<? extends Parcelable>) teacherList);
        outState.putParcelableArrayList("childrenList", (ArrayList<? extends Parcelable>) childrenList);
        outState.putParcelableArrayList("pictureList", (ArrayList<? extends Parcelable>) pictureList);
        outState.putParcelableArrayList("paintList", (ArrayList<? extends Parcelable>) paintList);
        outState.putParcelableArrayList("enlightenList", (ArrayList<? extends Parcelable>) enlightenList);
        outState.putParcelableArrayList("preschoolList", (ArrayList<? extends Parcelable>) preschoolList);
        outState.putParcelableArrayList("middleList", (ArrayList<? extends Parcelable>) middleList);
        outState.putParcelableArrayList("programmeList", (ArrayList<? extends Parcelable>) programmeList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_education, null);

        classroomList = new ArrayList<>();
        textualList = new ArrayList<>();
        englishList = new ArrayList<>();
        translateList = new ArrayList<>();
        teacherList = new ArrayList<>();
        childrenList = new ArrayList<>();
        pictureList = new ArrayList<>();
        paintList = new ArrayList<>();
        enlightenList = new ArrayList<>();
        preschoolList = new ArrayList<>();
        middleList = new ArrayList<>();
        programmeList = new ArrayList<>();

        classroomRecyclerView = inflate.findViewById(R.id.classroom_recycler_view);
        teacherRecyclerView = inflate.findViewById(R.id.teacher_recycler_view);
        enlightenRecyclerView = inflate.findViewById(R.id.enlighten_recycler_view);
        programmeRecyclerView = inflate.findViewById(R.id.programme_recycler_view);
        textualRecyclerView = inflate.findViewById(R.id.textual_recycler_view);
        englishRecyclerView = inflate.findViewById(R.id.english_recycler_view);
        translateRecyclerView = inflate.findViewById(R.id.translate_recycler_view);
        childrenRecyclerView = inflate.findViewById(R.id.children_recycler_view);
        pictureRecyclerView = inflate.findViewById(R.id.picture_recycler_view);
        paintRecyclerView = inflate.findViewById(R.id.paint_recycler_view);
        preschoolRecyclerView = inflate.findViewById(R.id.preschool_recycler_view);
        middleRecyclerView = inflate.findViewById(R.id.middle_recycler_view);

        classroomRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), classroomList);
        teacherRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), teacherList);
        enlightenRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), enlightenList);
        programmeRecyclerViewAdapter = new UserRecyclerViewAdapter(getContext(), programmeList);

        textualRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), textualList);
        englishRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), englishList);
        translateRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), translateList);
        childrenRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), childrenList);
        pictureRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), pictureList);
        paintRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), paintList);
        preschoolRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), preschoolList);
        middleRecyclerViewAdapter = new ChoiceRecyclerViewAdapter(getContext(), middleList);

        Configuration mConfiguration = MyApplication.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            classroomRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            teacherRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            enlightenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            programmeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            textualRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            englishRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            translateRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            childrenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            pictureRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            paintRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            preschoolRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
            middleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        } else {
            classroomRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            teacherRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            enlightenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            programmeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            textualRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            englishRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            translateRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            childrenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            pictureRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            paintRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            preschoolRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            middleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }


        classroomRecyclerView.setAdapter(classroomRecyclerViewAdapter);
        teacherRecyclerView.setAdapter(teacherRecyclerViewAdapter);
        enlightenRecyclerView.setAdapter(enlightenRecyclerViewAdapter);
        programmeRecyclerView.setAdapter(programmeRecyclerViewAdapter);
        textualRecyclerView.setAdapter(textualRecyclerViewAdapter);
        englishRecyclerView.setAdapter(englishRecyclerViewAdapter);
        translateRecyclerView.setAdapter(translateRecyclerViewAdapter);
        childrenRecyclerView.setAdapter(childrenRecyclerViewAdapter);
        pictureRecyclerView.setAdapter(pictureRecyclerViewAdapter);
        paintRecyclerView.setAdapter(paintRecyclerViewAdapter);
        preschoolRecyclerView.setAdapter(preschoolRecyclerViewAdapter);
        middleRecyclerView.setAdapter(middleRecyclerViewAdapter);

        if (savedInstanceState != null) {
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("classroomList")) {
                classroomList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("textualList")) {
                textualList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("englishList")) {
                englishList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("translateList")) {
                translateList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("teacherList")) {
                teacherList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("childrenList")) {
                childrenList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("pictureList")) {
                pictureList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("paintList")) {
                paintList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("enlightenList")) {
                enlightenList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("preschoolList")) {
                preschoolList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("middleList")) {
                middleList.add((APPLocalBean) parcelable);
            }
            for (Parcelable parcelable : savedInstanceState.getParcelableArrayList("programmeList")) {
                programmeList.add((APPLocalBean) parcelable);
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
//                for (APPLocalBean appLocalBean : classroomList) {
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
//                for (APPLocalBean appLocalBean : textualList) {
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
//                for (APPLocalBean appLocalBean : englishList) {
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
//                for (APPLocalBean appLocalBean : translateList) {
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
//                for (APPLocalBean appLocalBean : teacherList) {
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
//                for (APPLocalBean appLocalBean : childrenList) {
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
//                for (APPLocalBean appLocalBean : pictureList) {
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
//                for (APPLocalBean appLocalBean : paintList) {
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
//                for (APPLocalBean appLocalBean : enlightenList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x012, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x012, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x012, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : preschoolList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x013, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x013, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x013, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : middleList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x014, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x014, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x014, 100);
//                    }
//                }
//                for (APPLocalBean appLocalBean : programmeList) {
//                    boolean appExists = CustomUtil.isAppExists(appLocalBean.getAppPackage());
//                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), appLocalBean.getAppPackage() + ".apk");
//                    if (appExists) {
//                        appLocalBean.setAppState("打开");
//                        handler.sendEmptyMessageAtTime(0x015, 100);
//                    } else if (saveFile.exists()) {
//                        appLocalBean.setAppState("安装");
//                        handler.sendEmptyMessageAtTime(0x015, 100);
//                    } else {
//                        appLocalBean.setAppState("下载");
//                        handler.sendEmptyMessageAtTime(0x015, 100);
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
        if (receiver != null) {
            MyApplication.getInstance().getContext().unregisterReceiver(receiver);
        }
    }

    private void initAPPData() {
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_CLASSROOM_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_TEXTUAL_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_ENGLISH_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_TRANSLATE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_TEACHER_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_CHILDREN_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PICTURE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PAINT_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_ENLIGHTEN_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PRESCHOOL_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_MIDDLE_INFORMATION);
        getAPPData(Contacts.SERVER_URL + ":" + Contacts.SERVER_PORT + "/" + Contacts.GET_PROGRAMME_INFORMATION);
    }

    private void initOnClickListener() {

        classroomRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) classroomList.get(position));
                startActivity(intent);
            }
        });

        teacherRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) teacherList.get(position));
                startActivity(intent);
            }
        });

        enlightenRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) enlightenList.get(position));
                startActivity(intent);
            }
        });

        programmeRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) programmeList.get(position));
                startActivity(intent);
            }
        });

        textualRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) textualList.get(position));
                startActivity(intent);
            }
        });

        englishRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) englishList.get(position));
                startActivity(intent);
            }
        });

        translateRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) translateList.get(position));
                startActivity(intent);
            }
        });

        childrenRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) childrenList.get(position));
                startActivity(intent);
            }
        });

        pictureRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) pictureList.get(position));
                startActivity(intent);
            }
        });

        paintRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) paintList.get(position));
                startActivity(intent);
            }
        });

        preschoolRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) preschoolList.get(position));
                startActivity(intent);
            }
        });

        middleRecyclerViewAdapter.setOnItemClickListener(new ChoiceRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int position) {
                Intent intent = new Intent(getContext(), APPInformationActivity.class);
                intent.putExtra("appHomeBean", (Serializable) middleList.get(position));
                startActivity(intent);
            }
        });

        classroomRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(classroomList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", classroomList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", classroomList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(classroomList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), classroomList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(classroomList.get(position).getAppState())) {
                    CustomUtil.openAPK(classroomList.get(position).getAppPackage());
                }
            }
        });

        textualRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(textualList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", textualList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", textualList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(textualList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), textualList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(textualList.get(position).getAppState())) {
                    CustomUtil.openAPK(textualList.get(position).getAppPackage());
                }
            }
        });

        englishRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(englishList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", englishList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", englishList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(englishList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), englishList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(englishList.get(position).getAppState())) {
                    CustomUtil.openAPK(englishList.get(position).getAppPackage());
                }
            }
        });

        translateRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(translateList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", translateList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", translateList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(translateList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), translateList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(translateList.get(position).getAppState())) {
                    CustomUtil.openAPK(translateList.get(position).getAppPackage());
                }
            }
        });

        teacherRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(teacherList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", teacherList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", teacherList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(teacherList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), teacherList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(teacherList.get(position).getAppState())) {
                    CustomUtil.openAPK(teacherList.get(position).getAppPackage());
                }
            }
        });

        childrenRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(childrenList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", childrenList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", childrenList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(childrenList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), childrenList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(childrenList.get(position).getAppState())) {
                    CustomUtil.openAPK(childrenList.get(position).getAppPackage());
                }
            }
        });

        pictureRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(pictureList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", pictureList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", pictureList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(pictureList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), pictureList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(pictureList.get(position).getAppState())) {
                    CustomUtil.openAPK(pictureList.get(position).getAppPackage());
                }
            }
        });

        paintRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(paintList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", paintList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", paintList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(paintList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), paintList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(paintList.get(position).getAppState())) {
                    CustomUtil.openAPK(paintList.get(position).getAppPackage());
                }
            }
        });

        enlightenRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(enlightenList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", enlightenList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", enlightenList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(enlightenList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), enlightenList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(enlightenList.get(position).getAppState())) {
                    CustomUtil.openAPK(enlightenList.get(position).getAppPackage());
                }
            }
        });

        preschoolRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(preschoolList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", preschoolList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", preschoolList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(preschoolList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), preschoolList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(preschoolList.get(position).getAppState())) {
                    CustomUtil.openAPK(preschoolList.get(position).getAppPackage());
                }
            }
        });

        middleRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(middleList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", middleList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", middleList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(middleList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), middleList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(middleList.get(position).getAppState())) {
                    CustomUtil.openAPK(middleList.get(position).getAppPackage());
                }
            }
        });

        programmeRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position) {
                Log.e(TAG, "setState");
                if ("下载".equals(programmeList.get(position).getAppState())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", programmeList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", programmeList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(programmeList.get(position).getAppState())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), programmeList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(programmeList.get(position).getAppState())) {
                    CustomUtil.openAPK(programmeList.get(position).getAppPackage());
                }
            }
        });

    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, intent.getAction());
            if ("js.download.progress".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : classroomList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        classroomRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : textualList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        textualRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : englishList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        englishRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : translateList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        translateRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : teacherList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        teacherRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : childrenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        childrenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : pictureList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        pictureRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : paintList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        paintRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : enlightenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : preschoolList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : middleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        middleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : programmeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState(intent.getStringExtra("progress") + "%");
                        programmeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.download.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : classroomList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        classroomRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : textualList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        textualRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : englishList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        englishRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : translateList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        translateRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : teacherList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        teacherRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : childrenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        childrenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : pictureList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        pictureRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : paintList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        paintRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : enlightenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : preschoolList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : middleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        middleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : programmeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("安装");
                        programmeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.install.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : classroomList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        classroomRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : textualList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        textualRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : englishList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        englishRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : translateList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        translateRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : teacherList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        teacherRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : childrenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        childrenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : pictureList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        pictureRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : paintList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        paintRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : enlightenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : preschoolList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : middleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        middleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : programmeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("打开");
                        programmeRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if ("js.app.remove.completed".equals(intent.getAction())) {
                for (APPLocalBean appLocalBean : classroomList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        classroomRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : textualList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        textualRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : englishList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        englishRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : translateList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        translateRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : teacherList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        teacherRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : childrenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        childrenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : pictureList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        pictureRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : paintList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        paintRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : enlightenList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : preschoolList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : middleList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        middleRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                for (APPLocalBean appLocalBean : programmeList) {
                    if (appLocalBean.getAppPackage().equals(intent.getStringExtra("packageName"))) {
                        appLocalBean.setAppState("下载");
                        programmeRecyclerViewAdapter.notifyDataSetChanged();
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
                OkHttpClient okHttpClient = new OkHttpClient();
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

}
