package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.linnca.pelicann.R;

public class Preferences extends PreferenceFragmentCompat {
    public LogoutListener logoutListener;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preference, rootKey);
        //set click listener for a couple of the buttons
        Preference logoutPreference = findPreference(
                getResources().getString(R.string.preferences_account_logout_key));
        logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                logoutListener.logout();
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //or else the background will be invisible
        getView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    //implements the listeners for some of the preferences
    private void implementListeners(Context context){
        try {
            logoutListener = (LogoutListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LogoutListener");
        }
    }

    public interface LogoutListener {
        void logout();
    }


}
