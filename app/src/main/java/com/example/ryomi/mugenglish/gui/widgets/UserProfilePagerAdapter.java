package com.example.ryomi.mugenglish.gui.widgets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ryomi.mugenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.mugenglish.gui.User_Profile_Hours_Studied;
import com.example.ryomi.mugenglish.gui.User_Profile_Report_Card;

import java.io.Serializable;
import java.util.List;

public class UserProfilePagerAdapter extends FragmentPagerAdapter {
    private int tabCount;
    private List<InstanceRecord> records;

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
                Fragment user_profile_hours_studied = new User_Profile_Hours_Studied();
                user_profile_hours_studied.setArguments(bundle);
                return user_profile_hours_studied;
            case 1:
                Fragment user_profile_report_card = new User_Profile_Report_Card();
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
