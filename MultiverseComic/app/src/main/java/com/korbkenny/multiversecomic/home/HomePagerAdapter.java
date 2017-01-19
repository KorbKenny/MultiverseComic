package com.korbkenny.multiversecomic.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by KorbBookProReturns on 1/17/17.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {
    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new GroupsFragment();
            case 1:
                return new HomeFragment();
            case 2:
                return new UserFragment();
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
        switch(position){
            case 0:
                return "Groups";
            case 1:
                return "Home";
            case 2:
                return "User";
            default:
                return null;
        }
    }
}
