package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.linnca.pelicann.R;
import com.linnca.pelicann.gui.widgets.ToolbarState;

public class Preferences extends PreferenceFragmentCompat {
    private PreferencesListener listener;
    interface PreferencesListener {
        void setToolbarState(ToolbarState state);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preference, rootKey);
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_settings_title), false, false, null)
        );
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

    private void implementListeners(Context context){
        try {
            listener = (PreferencesListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

}
