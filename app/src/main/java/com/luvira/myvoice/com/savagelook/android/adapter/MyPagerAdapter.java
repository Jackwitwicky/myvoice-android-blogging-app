package com.luvira.myvoice.com.savagelook.android.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.luvira.myvoice.fragments.NewFragment;
import com.luvira.myvoice.fragments.PopularFragment;
import com.luvira.myvoice.fragments.TrendingFragment;

/**
 * Created by witwicky on 13/01/17.
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    //constructor
    public MyPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new NewFragment();
            case 1:
                return new PopularFragment();
            case 2:
                return new TrendingFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "New";
            case 1:
                return "Popular";
            case 2:
                return "Trending";
            default:
                return null;
        }
    }
}
