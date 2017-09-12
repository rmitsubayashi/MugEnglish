package com.linnca.pelicann.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;


import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class Preferences extends PreferenceFragmentCompat {
    private final String TAG = "Preferences";
    private PreferencesListener listener;

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preference, rootKey);
        attachListeners();
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_settings_title), false, null)
        );

        //do these in onStart so we can handle when we change the value
        //in a secondary fragment (while this is not destroyed)
        setNumberOfAttemptsPerQuestionPreference();
        setDescriptionBeforeLessonWithExceptionRulePreference();
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

    private void attachListeners(){
        EditTextPreference numberOfAttemptsPerQuestionPreference =
                (EditTextPreference)findPreference(getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_key));

        numberOfAttemptsPerQuestionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //since this is an editText the value should always be a string
                if (newValue instanceof String){
                    setNumberOfAttemptsPerQuestionSummary((String)newValue);
                } else {
                    Log.d(TAG, newValue.getClass().toString());
                }


                return true;
            }
        });
    }
    private void setNumberOfAttemptsPerQuestionPreference(){
        //default value
        EditTextPreference numberOfAttemptsPerQuestionPreference =
                (EditTextPreference)findPreference(getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_key));
        setNumberOfAttemptsPerQuestionSummary(numberOfAttemptsPerQuestionPreference.getText());

    }

    private void setNumberOfAttemptsPerQuestionSummary(String newValue){
        EditTextPreference preference =
                (EditTextPreference)findPreference(getString(R.string.preferences_questions_numberOfAttemptsPerQuestion_key));

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

    private void setDescriptionBeforeLessonWithExceptionRulePreference(){
        //we can't access the preference directly from the fragment
        // unless it's visible on the screen
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        //the preference is still stored as a string
        boolean checked = sharedPreferences.getBoolean
                (getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key), true);

        PreferenceScreen preferenceScreen =
                (PreferenceScreen)findPreference(getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key));
        if (checked){
            preferenceScreen.setSummary(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_true);
        } else {
            preferenceScreen.setSummary(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_false);
        }
    }

}
