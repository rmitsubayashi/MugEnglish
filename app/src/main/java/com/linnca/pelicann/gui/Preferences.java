package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.widget.EditText;


import com.linnca.pelicann.R;
import com.linnca.pelicann.gui.widgets.ToolbarState;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class Preferences extends PreferenceFragmentCompat {
    private final String TAG = "Preferences";
    private PreferencesListener listener;
    interface PreferencesListener {
        void setToolbarState(ToolbarState state);

    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preference, rootKey);

        EditTextPreference numberOfAttemptsPerQuestionPreference =
                (EditTextPreference)findPreference(getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_key));

        numberOfAttemptsPerQuestionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //since this is an editText the value should always be a string
                if (newValue instanceof String){
                    setNumberOfAttemptsPerQuestionSummary((EditTextPreference) preference, (String)newValue);
                } else {
                    Log.d(TAG, newValue.getClass().toString());
                }


                return true;
            }
        });

        setNumberOfAttemptsPerQuestionSummary(numberOfAttemptsPerQuestionPreference, numberOfAttemptsPerQuestionPreference.getText());
   }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_settings_title), false, null)
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

    private void setNumberOfAttemptsPerQuestionSummary(EditTextPreference preference, String newValue){
        int numberOfAttempts;
        try {
            numberOfAttempts = Integer.parseInt(newValue);
        } catch (NumberFormatException e){
            e.printStackTrace();
            String removedNonNumbers = newValue.replaceAll("[^0-9]","");
            //just in case we don't have a string with a letter
            numberOfAttempts = removedNonNumbers.length() != 0 ? Integer.parseInt(removedNonNumbers) : 0;
        }
        String title = getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_label, numberOfAttempts);
        preference.setTitle(title);
    }

}
