package com.js.appstore.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.js.appstore.MyApplication;
import com.js.appstore.R;
import com.js.appstore.adapter.MainFragmentAdapter;
import com.js.appstore.bean.DownBean;
import com.js.appstore.bean.DownProgressBean;
import com.js.appstore.fragment.EducationFragment;
import com.js.appstore.fragment.GameFragment;
import com.js.appstore.fragment.LifeFragment;
import com.js.appstore.fragment.OfficeFragment;
import com.js.appstore.fragment.RecommendFragment;
import com.js.appstore.fragment.RecreationFragment;
import com.js.appstore.manager.Contacts;
import com.js.appstore.service.MyService;
import com.js.appstore.utils.CustomUtil;
import com.js.appstore.view.UpdateDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity=========>";

    private MainFragmentAdapter mainFragmentAdapter;
    private List<Fragment> fragmentList;
    private List<String> topTitle;

    private ViewPager viewPager;

    private TabLayout tabLayout;

    private EditText etSource;

    private UpdateDialog updateDialog;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0x001 :
                    Log.e(TAG, "版本号不一致");
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
//                    dialog = alertDialog.setMessage("有新版本")
//                            .setNegativeButton("退出", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    CustomUtil.exitAPP();
//                                }
//                            })
//                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    DownBean downBean = CustomUtil.updateAPK("http://114.132.220.67:8080/test/js_project/store/js_store_1.0.apk");
//                                    Timer timer = new Timer();
//                                    timer.schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            DownProgressBean downProgressBean = CustomUtil.updateProgress(downBean.getDownloadId(), timer);
//                                            Log.e(TAG, downProgressBean.getProgress());
//                                        }
//                                    }, 0, 1000);
//                                }
//                            }).setCancelable(false).create();
//                    dialog.show();
                    updateDialog = new UpdateDialog(MainActivity.this);
                    updateDialog.setMessage("应用商店有新版本！！！");
                    updateDialog.setTitleVisible(View.GONE);
                    updateDialog.setExitOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CustomUtil.exitAPP();
                        }
                    });
                    updateDialog.setUpdateOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateDialog.setProgressVisible(View.VISIBLE);
                            updateDialog.setButtonVisible(View.GONE);
                            DownBean downBean = CustomUtil.updateAPK(Contacts.SERVER_URL + ":8080/test/js_project/store/js_store.apk");
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    DownProgressBean downProgressBean = CustomUtil.updateProgress(downBean.getDownloadId(), timer);
                                    Log.e(TAG, downProgressBean.getProgress());
                                    float progress = Float.parseFloat(downProgressBean.getProgress());
                                    updateDialog.setPbProgress((int) progress);
                                    updateDialog.setTvProgress(downProgressBean.getProgress());
                                }
                            }, 0, 1000);
                        }
                    });
                    updateDialog.setCancelable(false);
                    updateDialog.show();
                    break;
                case 0x002 :
                    Log.e(TAG, "版本号一致");
                    break;
            }
        }
    };

//    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        CustomUtil.setStatusBar(this);
        setContentView(R.layout.activity_main);
        CustomUtil.hideNavigationBar(this);
//        intent = new Intent(this, MyService.class);
//        startService(intent);
        viewPager = findViewById(R.id.main_view_pager);
        tabLayout = findViewById(R.id.main_tablayout);
        etSource = findViewById(R.id.et_source);
        fragmentList = new ArrayList<>();
        topTitle = new ArrayList<>();
        fragmentList.add(new RecommendFragment());
        fragmentList.add(new RecreationFragment());
        fragmentList.add(new LifeFragment());
        fragmentList.add(new OfficeFragment());
        fragmentList.add(new EducationFragment());
        fragmentList.add(new GameFragment());
        mainFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(mainFragmentAdapter);

        //绑定两个控件
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e(TAG, tab.getPosition() + "");
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    // 改变 tab 选择状态下的字体大小
                    ((TextView) view).setTextSize(26);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.black));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    // 改变 tab 选择状态下的字体大小
                    ((TextView) view).setTextSize(20);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

//        viewPager.setCurrentItem(2);
//        mainFragmentAdapter.notifyDataSetChanged();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(getTabView(i));
            }
        }
        setDefaultSelected(0);
        viewPager.setOffscreenPageLimit(5);

        // 浏览器搜索跳转
        etSource.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    String sourceText = etSource.getText().toString().trim();
                    Uri uri = null;
                    try {
                        uri = Uri.parse("http://www.baidu.com/s?&ie=utf-8&oe=UTF-8&wd=" + URLEncoder.encode(sourceText, "UTF-8"));
                        Log.d(TAG, "source content is" + sourceText);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                    final Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(uri);
                    startActivity(intent);
                }
                return false;
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int position = extras.getInt("position");
            setDefaultSelected(position);
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                String serverFile = CustomUtil.getServerFile(Contacts.SERVER_URL + ":8080/test/js_project/store/Version.txt");
                String localVersionName = CustomUtil.getLocalVersionName();
                if (localVersionName.equals(serverFile)) {
                    handler.sendEmptyMessageAtTime(0x002, 100);
                } else {
                    handler.sendEmptyMessageAtTime(0x001, 100);
                }
            }
        }.start();
    }

    private void setDefaultSelected(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        View view = Objects.requireNonNull(tab).getCustomView();
        if (view instanceof TextView) {
            // 改变 tab 选择状态下的字体大小
            ((TextView) view).setTextSize(26);
            ((TextView) view).setTextColor(getResources().getColor(R.color.black));
        }
        viewPager.setCurrentItem(position);
    }

    /**
     * 自定义Tab的View
     * @param currentPosition
     * @return
     */
    private View getTabView(int currentPosition) {
        topTitle.add("推荐");
        topTitle.add("娱乐");
        topTitle.add("生活");
        topTitle.add("办公");
        topTitle.add("教育");
        topTitle.add("游戏");
        View view = LayoutInflater.from(this).inflate(R.layout.tab_text_view, null);
        TextView textView = view.findViewById(R.id.tab_item_textview);
        textView.setText(topTitle.get(currentPosition));
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        etSource.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(intent);
        if (updateDialog != null) {
            updateDialog.dismiss();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            // 获取当前聚焦
            View v = getCurrentFocus();

            if (CustomUtil.isShouldHideInput(v, ev)) {
                //点击editText控件外部
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    assert v != null;
                    //软键盘工具类关闭软键盘
                    CustomUtil.hideKeyBoard(MainActivity.this);
                    //使输入框失去焦点
                    v.clearFocus();
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }
}