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
                                    if (Contacts.GET_CLASSROOM_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ClassroomInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ClassroomInformation", null, values);
                                            classroomList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x004, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ClassroomInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_TEXTUAL_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("TextualInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("TextualInformation", null, values);
                                            textualList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x005, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("TextualInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_ENGLISH_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("EnglishInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("EnglishInformation", null, values);
                                            englishList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x006, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("EnglishInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_TRANSLATE_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("TranslateInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("TranslateInformation", null, values);
                                            translateList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x007, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("TranslateInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_TEACHER_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("TeacherInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("TeacherInformation", null, values);
                                            teacherList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x008, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("TeacherInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_CHILDREN_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ChildrenInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ChildrenInformation", null, values);
                                            childrenList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x009, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ChildrenInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_PICTURE_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("PictureInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("PictureInformation", null, values);
                                            pictureList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x010, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("PictureInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_PAINT_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("PaintInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("PaintInformation", null, values);
                                            paintList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x011, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("PaintInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_ENLIGHTEN_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("EnlightenInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("EnlightenInformation", null, values);
                                            enlightenList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x012, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("EnlightenInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_PRESCHOOL_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("PreschoolInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("PreschoolInformation", null, values);
                                            preschoolList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x013, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("PreschoolInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_MIDDLE_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("MiddleInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("MiddleInformation", null, values);
                                            middleList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x014, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("MiddleInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
                                        }
                                        cursor.close();
                                    } else if (Contacts.GET_PROGRAMME_INFORMATION.equals(url.split("/")[3])) {
                                        Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query
                                                ("ProgrammeInformation", new String[]{"appId"}, "appId = ?", new String[]{appServerBean.getAppId() + ""}, null, null, null);
                                        if (cursor.getCount() == 0) {
                                            //将这一行数据存放到数据库的数据表中。参数：（表名，某些为空的列自动赋值 null，ContentValue 对象）
                                            MyApplication.getInstance().getSqLiteDatabase().insert("ProgrammeInformation", null, values);
                                            programmeList.add(new APPLocalBean(appServerBean.getAppId(), appServerBean.getAppIcon(), appServerBean.getAppName(), appServerBean.getAppPackage(),
                                                    appServerBean.getAppInformation(), appServerBean.getAppDownLoadURL(), appServerBean.getAppIntroduce(), appServerBean.getAppPicture(), appState));
                                            handler.sendEmptyMessageAtTime(0x015, 100);
                                        } else {
//                                        Log.d(TAG, "已添加数据:" + appServerBean.getAppId());
                                            MyApplication.getInstance().getSqLiteDatabase().update("ProgrammeInformation", values, "appId=?", new String[] {appServerBean.getAppId() + ""});
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
        } else {
            classroomRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            teacherRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            enlightenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            programmeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            textualRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            englishRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            translateRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            childrenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            pictureRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            paintRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            preschoolRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            middleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

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

        DisplayMetrics displayMetrics = MyApplication.getInstance().getContext().getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels == 1080 || heightPixels == 1080) {
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
                classroomRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                teacherRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                enlightenRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                programmeRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
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
                classroomRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                teacherRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                enlightenRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
                programmeRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
            }
        } else {
            classroomRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            teacherRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            enlightenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            programmeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            textualRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            englishRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            translateRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            childrenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            pictureRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            paintRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            preschoolRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            middleRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
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

        downloadManager = (DownloadManager) MyApplication.getInstance().getContext().getSystemService(Context.DOWNLOAD_SERVICE);

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

        new Thread() {
            @Override
            public void run() {
                super.run();
                Cursor cursor = MyApplication.getInstance().getSqLiteDatabase().query("ClassroomInformation", null, null, null, null, null, null);
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
                        classroomList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x004, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("TextualInformation", null, null, null, null, null, null);
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
                        textualList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x005, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("EnglishInformation", null, null, null, null, null, null);
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
                        englishList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x006, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("TranslateInformation", null, null, null, null, null, null);
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
                        translateList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x007, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("TeacherInformation", null, null, null, null, null, null);
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
                        teacherList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x008, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("ChildrenInformation", null, null, null, null, null, null);
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
                        childrenList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x009, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("PictureInformation", null, null, null, null, null, null);
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
                        pictureList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x010, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("PaintInformation", null, null, null, null, null, null);
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
                        paintList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x011, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("EnlightenInformation", null, null, null, null, null, null);
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
                        enlightenList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x012, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("PreschoolInformation", null, null, null, null, null, null);
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
                        preschoolList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x013, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("MiddleInformation", null, null, null, null, null, null);
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
                        middleList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x014, 100);
                    }
                }
                cursor.close();
                cursor = MyApplication.getInstance().getSqLiteDatabase().query("ProgrammeInformation", null, null, null, null, null, null);
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
                        programmeList.add(new APPLocalBean(appId, appIcon, appName, appPackage, appInformation, appDownLoadURL, appIntroduce, appPicture, appState));
                        handler.sendEmptyMessageAtTime(0x015, 100);
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
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", classroomList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", classroomList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), classroomList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(classroomList.get(position).getAppPackage());
                } else {
                    classroomList.get(position).setAppState("下载");
                    classroomRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (classroomList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        textualRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", textualList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", textualList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), textualList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(textualList.get(position).getAppPackage());
                } else {
                    textualList.get(position).setAppState("下载");
                    textualRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (textualList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        englishRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", englishList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", englishList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), englishList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(englishList.get(position).getAppPackage());
                } else {
                    englishList.get(position).setAppState("下载");
                    englishRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (englishList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        translateRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", translateList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", translateList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), translateList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(translateList.get(position).getAppPackage());
                } else {
                    translateList.get(position).setAppState("下载");
                    translateRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (translateList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        teacherRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", teacherList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", teacherList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), teacherList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(teacherList.get(position).getAppPackage());
                } else {
                    teacherList.get(position).setAppState("下载");
                    teacherRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (teacherList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        childrenRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", childrenList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", childrenList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), childrenList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(childrenList.get(position).getAppPackage());
                } else {
                    childrenList.get(position).setAppState("下载");
                    childrenRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (childrenList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        pictureRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", pictureList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", pictureList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), pictureList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(pictureList.get(position).getAppPackage());
                } else {
                    pictureList.get(position).setAppState("下载");
                    pictureRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (pictureList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        paintRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", paintList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", paintList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), paintList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(paintList.get(position).getAppPackage());
                } else {
                    paintList.get(position).setAppState("下载");
                    paintRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (paintList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        enlightenRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", enlightenList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", enlightenList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), enlightenList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(enlightenList.get(position).getAppPackage());
                } else {
                    enlightenList.get(position).setAppState("下载");
                    enlightenRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (enlightenList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        preschoolRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", preschoolList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", preschoolList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), preschoolList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(preschoolList.get(position).getAppPackage());
                } else {
                    preschoolList.get(position).setAppState("下载");
                    preschoolRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (preschoolList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        middleRecyclerViewAdapter.setStateOnClickListener(new ChoiceRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", middleList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", middleList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), middleList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(middleList.get(position).getAppPackage());
                } else {
                    middleList.get(position).setAppState("下载");
                    middleRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (middleList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
                            removeBean.getTimer().cancel();
                            downloadManager.remove(removeBean.getRemoveId());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        programmeRecyclerViewAdapter.setStateOnClickListener(new UserRecyclerViewAdapter.OnStateClickListener() {
            @Override
            public void OnClick(int position, TextView btnState) {
                Log.e(TAG, "setState");
                if ("下载".equals(btnState.getText().toString())) {
                    Intent intent = new Intent("js.download.app");
                    intent.putExtra("url", programmeList.get(position).getAppDownLoadURL());
                    intent.putExtra("packageName", programmeList.get(position).getAppPackage());
                    MyApplication.getInstance().getContext().sendBroadcast(intent);
                } else if ("安装".equals(btnState.getText().toString())) {
                    File saveFile = new File(MyApplication.getInstance().getContext().getExternalFilesDir(null), programmeList.get(position).getAppPackage() + ".apk");
                    CustomUtil.installAPK(MyApplication.getInstance().getContext(), saveFile);
                } else if ("打开".equals(btnState.getText().toString())) {
                    CustomUtil.openAPK(programmeList.get(position).getAppPackage());
                } else {
                    programmeList.get(position).setAppState("下载");
                    programmeRecyclerViewAdapter.notifyDataSetChanged();
                    Iterator<RemoveBean> iterator = MyService.downloadIds.iterator();
                    while (iterator.hasNext()) {
                        RemoveBean removeBean = iterator.next();
                        if (programmeList.get(position).getAppPackage().equals(removeBean.getPackageName())) {
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
            } else if ("js.app.again.download".equals(intent.getAction()) || "js.app.remove.completed".equals(intent.getAction())) {
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
