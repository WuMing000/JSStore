package com.js.appstore.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.js.appstore.R;
import com.js.appstore.adapter.MainFragmentAdapter;
import com.js.appstore.fragment.EducationFragment;
import com.js.appstore.fragment.GameFragment;
import com.js.appstore.fragment.LifeFragment;
import com.js.appstore.fragment.OfficeFragment;
import com.js.appstore.fragment.RecommendFragment;
import com.js.appstore.fragment.RecreationFragment;
import com.js.appstore.service.MyService;
import com.js.appstore.utils.CustomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity=========>";

    private MainFragmentAdapter mainFragmentAdapter;
    private List<Fragment> fragmentList;
    private List<String> topTitle;

    private ViewPager viewPager;

    private TabLayout tabLayout;

//    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomUtil.hideBottomUIMenu(this);
        setContentView(R.layout.activity_main);
//        intent = new Intent(this, MyService.class);
//        startService(intent);
        viewPager = findViewById(R.id.main_view_pager);
        tabLayout = findViewById(R.id.main_tablayout);
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
                    ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    // 改变 tab 选择状态下的字体大小
                    ((TextView) view).setTextSize(20);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.black));
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
    }

    private void setDefaultSelected(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        View view = Objects.requireNonNull(tab).getCustomView();
        if (view instanceof TextView) {
            // 改变 tab 选择状态下的字体大小
            ((TextView) view).setTextSize(26);
            ((TextView) view).setTextColor(getResources().getColor(R.color.white));
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
        TextView textView = (TextView) view.findViewById(R.id.tab_item_textview);
        textView.setText(topTitle.get(currentPosition));
        return view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(intent);
    }
}