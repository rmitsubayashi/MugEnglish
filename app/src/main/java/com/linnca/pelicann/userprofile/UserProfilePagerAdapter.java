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
    private final List<InstanceRecord> records;

    public UserProfilePagerAdapter(FragmentManager manager, int tabCount, List<InstanceRecord> records){
        super(manager);
        this.tabCount = tabCount;
        this.records = records;
    }

    @Override
    public Fragment getItem(int position){
        Bundle bundle = new Bundle();
        bundle.putSerializable(null, (Serializable)records);
        switch (position) {
            case 0:
                Fragment user_profile_hours_studied = new UserProfile_HoursStudied();
                user_profile_hours_studied.setArguments(bundle);
                return user_profile_hours_studied;
            case 1:
                Fragment user_profile_report_card = new UserProfile_ReportCard();
                user_profile_report_card.setArguments(bundle);
                return  user_profile_report_card;
            default:
                return null;
        }
    }

    @Override
    public int getCount(){
        return tabCount;
    }
}
