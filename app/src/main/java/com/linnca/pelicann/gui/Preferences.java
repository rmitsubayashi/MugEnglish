package com.linnca.pelicann.gui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.linnca.pelicann.R;

public class Preferences extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preference, rootKey);
    }

}
