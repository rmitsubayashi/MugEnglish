package com.example.ryomi.mugenglish.gui.widgets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ryomi.mugenglish.gui.OnboardingIntroduction1;
import com.example.ryomi.mugenglish.gui.OnboardingIntroduction2;
import com.example.ryomi.mugenglish.gui.OnboardingIntroduction3;
import com.example.ryomi.mugenglish.gui.OnboardingIntroduction4;

public class OnboardingPagerAdapter extends FragmentPagerAdapter {
    private int maxPageCt = 1;
    private Bundle bundle;

    public OnboardingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 :
                return new OnboardingIntroduction1();
            case 1 :
                return new OnboardingIntroduction2();
            case 2 :
                Fragment onboardingIntroduction3 = new OnboardingIntroduction3();
                onboardingIntroduction3.setArguments(bundle);
                return onboardingIntroduction3;
            case 3 :
                return new OnboardingIntroduction4();
            default :
                return null;
        }
    }

    @Override
    public int getCount() {
        return maxPageCt;
    }

    public void updateMaxPageCount(int pageIndex){
        int pageCt = pageIndex + 1;
        if (pageCt > maxPageCt){
            maxPageCt = pageCt;
            notifyDataSetChanged();
        }
    }

    public void updateBundle(Bundle bundle){
        this.bundle = bundle;
    }


}