package com.linnca.pelicann.userprofile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.linnca.pelicann.mainactivity.MainActivity;

import pelicann.linnca.com.corefunctionality.db.Database;

class UserProfilePagerAdapter extends FragmentPagerAdapter {
    private final int tabCount;
    private final Database db;

    UserProfilePagerAdapter(FragmentManager manager, int tabCount, Database db){
        super(manager);
        this.tabCount = tabCount;
        this.db = db;
    }

    @Override
    public Fragment getItem(int position){
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new UserProfile_HoursStudied();
                break;
            case 1:
                fragment = new UserProfile_ReportCard();
                break;
            default:
                return null;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.BUNDLE_DATABASE, db);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount(){
        return tabCount;
    }
}
