package com.linnca.pelicann.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

public class PreferencesDescriptionBeforeLessonWithExceptionRule extends Fragment {
    private final String TAG = "Preferences";
    private PreferencesListener listener;
    private Switch switchButton;
    private TextView titleTextView;
    private TextView functionalityTextView;
    private TextView reasonTitleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferences_subscreen_switch, container, false);
        switchButton = view.findViewById(R.id.preferences_subscreen_switch_button);
        titleTextView = view.findViewById(R.id.preferences_subscreen_switch_title);
        functionalityTextView = view.findViewById(R.id.preferences_subscreen_switch_functionality_title);
        reasonTitleTextView = view.findViewById(R.id.preferences_subscreen_switch_reason_title);
        setLayout();
        addActionListener();
        return view;
    }

        @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_label_short), false, false, null)
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

    private void setLayout(){
        titleTextView.setText(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_show);
        functionalityTextView.setText(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_description_functionality);
        reasonTitleTextView.setText(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_description_reason);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        //the preference is still stored as a string
        boolean checked = sharedPreferences.getBoolean
                (getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key), true);
        switchButton.setChecked(checked);
    }

    private void addActionListener(){
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key), checked);
                editor.apply();
            }
        });
    }
}
