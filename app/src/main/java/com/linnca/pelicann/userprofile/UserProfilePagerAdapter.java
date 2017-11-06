package com.linnca.pelicann.userprofile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.linnca.pelicann.questions.InstanceRecord;

import java.io.Serializable;
import java.util.List;

class UserProfilePagerAdapter extends FragmentPagerAdapter {
    private final int tabCount;

    UserProfilePagerAdapter(FragmentManager manager, int tabCount){
        super(manager);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position){
        switch (position) {
            case 0:
                return new UserProfile_HoursStudied();
            case 1:
                return new UserProfile_ReportCard();
            default:
                return null;
        }
    }

    @Override
    public int getCount(){
        return tabCount;
    }
}
