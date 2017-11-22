package com.linnca.pelicann.lessondetails;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessonlist.LessonListViewer;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.ArrayList;

public class LessonDescription extends Fragment {
    public static final String TAG = "LessonDescription";
    public static final String BUNDLE_LESSON_KEY = "bundleLessonKey";
    public static final String BUNDLE_SHOW_EXCEPTION = "bundleShowException";
    private LessonDescriptionListener listener;
    private boolean alwaysShowException = false;

    public interface LessonDescriptionListener {
        void setToolbarState(ToolbarState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        String lessonKey = getArguments().getString(BUNDLE_LESSON_KEY);
        alwaysShowException = getArguments().getBoolean(BUNDLE_SHOW_EXCEPTION);
        LessonListViewer helper = new LessonListViewer();
        LessonData lessonData = helper.getLessonData(lessonKey);
        Integer layoutID = lessonData.getDescriptionLayout();
        if (layoutID == null){
            //layout if we can't find one (we preemptively handle it by hiding the icon that links to this screen,
            // but just in case )
            return inflater.inflate(R.layout.fragment_lesson_description_not_found, container, false);
        }

        View view = inflater.inflate(layoutID, container, false);
        TextView toClearScoreTextView = view.findViewById(R.id.lesson_description_to_clear_score);
        //we might accidentally publish a lesson description xml file without the view
        if (toClearScoreTextView != null){
            toClearScoreTextView.setText(
                    getString(R.string.lesson_description_to_clear_score_template,
                            lessonData.getToClearScore())
            );
        }
        handleExceptionRules(view);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.lesson_description_title), false, false, null)
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
            listener = (LessonDescriptionListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    //in the settings, we have an option to hide parts of the descriptions containing
    // exception rules (for better retention).
    //do that here
    private void handleExceptionRules(View parentView){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        //the preference is still stored as a string
        boolean shouldShow = sharedPreferences.getBoolean
                (getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key), true);
        if (!shouldShow && !alwaysShowException) {
            ArrayList<View> viewsWithExceptionTag = getViewsByTag((ViewGroup) parentView, getString(R.string.lesson_description_exception_tag));
            for (View view : viewsWithExceptionTag) {
                view.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }
}
