package com.example.ryomi.myenglish.gui;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.ryomi.myenglish.R;

public class Preferences extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(Color.WHITE);
        getView().setClickable(true);
    }


}
