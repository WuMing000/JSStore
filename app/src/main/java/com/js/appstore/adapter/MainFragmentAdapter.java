package com.js.appstore.adapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;

    public MainFragmentAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.mFragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String topText = "推荐1";
        switch (position) {
            case 0 :
                topText = "推荐";
                break;
            case 1 :
                topText = "娱乐";
                break;
            case 2 :
                topText = "生活";
                break;
            case 3 :
                topText = "办公";
                break;
            case 4 :
                topText = "教育";
                break;
            case 5 :
                topText = "游戏";
                break;
            default:
                break;
        }
        return topText;
    }

}
