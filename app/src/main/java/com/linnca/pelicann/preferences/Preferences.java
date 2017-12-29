package com.linnca.pelicann.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;
import com.linnca.pelicann.mainactivity.ToolbarState;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class Preferences extends PreferenceFragmentCompat {
    public static final String TAG = "Preferences";
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
                new ToolbarState(getString(R.string.fragment_settings_title), false, false, null)
        );

        //do these in onStart so we can handle when we change the value
        //in a secondary fragment (while this is not destroyed)
        setNumberOfAttemptsPerQuestionPreference();
        setLessonsPerDayPreference();
        setDescriptionBeforeLessonWithExceptionRulePreference();
        setThemePreference();
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

        EditTextPreference lessonsPerDayPreference =
                (EditTextPreference)findPreference(getString(R.string.preferences_general_lessons_per_day_key));

        lessonsPerDayPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //since this is an editText the value should always be a string
                if (newValue instanceof String){
                    setLessonsPerDaySummary((String)newValue);
                } else {
                    Log.d(TAG, newValue.getClass().toString());
                }


                return true;
            }
        });

        ListPreference listPreference =
                (ListPreference)findPreference(getString(R.string.preferences_general_themeColor_key));
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listener.updateTheme();
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

    private void setLessonsPerDayPreference(){
        EditTextPreference lessonsPerDayPreference =
                (EditTextPreference)findPreference(getString(R.string.preferences_general_lessons_per_day_key));
        setLessonsPerDaySummary(lessonsPerDayPreference.getText());

    }

    private void setLessonsPerDaySummary(String newValue){
        EditTextPreference preference =
                (EditTextPreference)findPreference(getString(R.string.preferences_general_lessons_per_day_key));

        int lessonsPerDay;
        if (newValue == null){
            lessonsPerDay = 0;
        } else {
            try {
                lessonsPerDay = Integer.parseInt(newValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                String removedNonNumbers = newValue.replaceAll("[^0-9]", "");
                //just in case we don't have a string with a letter
                lessonsPerDay = removedNonNumbers.length() != 0 ? Integer.parseInt(removedNonNumbers) : 0;
            }
        }
        String title = lessonsPerDay > 0 ?
                getString(R.string.preferences_general_lessons_per_day_label, lessonsPerDay) :
                getString(R.string.preferences_general_lessons_per_day_label_empty);
        preference.setTitle(title);
    }

    private void setThemePreference(){
        ListPreference listPreference =
                (ListPreference)findPreference(getString(R.string.preferences_general_themeColor_key));
        String[] listEntries = new String[3];
        listEntries[0] = getString(R.string.preferences_general_themeColor_blue);
        listEntries[1] = getString(R.string.preferences_general_themeColor_green);
        listEntries[2] = getString(R.string.preferences_general_themeColor_yellow);
        listPreference.setEntries(listEntries);

        String[] listValues = new String[3];
        listValues[0] = Integer.toString(ThemeColorChanger.BLUE);
        listValues[1] = Integer.toString(ThemeColorChanger.GREEN);
        listValues[2] = Integer.toString(ThemeColorChanger.YELLOW);
        listPreference.setEntryValues(listValues);

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
