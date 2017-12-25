package com.linnca.pelicann.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.linnca.pelicann.userinterests.WikiDataEntity;

import java.util.List;

class OnboardingPagerAdapter extends FragmentPagerAdapter {
    private Onboarding3 onboarding3;
    private int maxPageCt;

    OnboardingPagerAdapter(FragmentManager fm, int maxPageCt) {
        super(fm);
        this.maxPageCt = maxPageCt;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 :
                return new Onboarding1();
            case 1 :
                return new Onboarding2();
            case 2 :
                //save so we can access the user selection
                onboarding3 = new Onboarding3();
                return onboarding3;
                //return new Onboarding3v2();
            default :
                return null;
        }
    }

    @Override
    public int getCount() {
        return maxPageCt;
    }

    int getStarterPackSelection(){
        if (onboarding3 == null)
            return -1;
        return onboarding3.getCurrentSelection();
        //return 1;
    }

}