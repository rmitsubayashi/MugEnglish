package com.linnca.pelicann.gui;

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
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.gui.widgets.ToolbarState;
import com.linnca.pelicann.questiongenerator.LessonHierarchyViewer;

import java.util.ArrayList;

public class LessonDescription extends Fragment {
    public static String BUNDLE_LESSON_KEY = "bundleLessonKey";
    private LessonDescriptionListener listener;

    interface LessonDescriptionListener {
        void setToolbarState(ToolbarState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        String lessonKey = getArguments().getString(BUNDLE_LESSON_KEY);
        LessonHierarchyViewer helper = new LessonHierarchyViewer(getContext());
        Integer layoutID = helper.getLessonData(lessonKey).getDescriptionLayout();
        if (layoutID == null){
            //layout if we can't find one (we preemptively handle it by hiding the icon that links to this screen,
            // but just in case )
            return inflater.inflate(R.layout.fragment_lesson_description_not_found, container, false);
        }

        View view = inflater.inflate(layoutID, container, false);
        TextView lessonTitleTextView = view.findViewById(R.id.description_lesson_title);
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer(getContext());
        LessonData lessonData = lessonHierarchyViewer.getLessonData(lessonKey);
        if (lessonData != null)
            lessonTitleTextView.setText(lessonData.getTitle());
        handleExceptionRules(view);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.lesson_description_title), false, null)
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

    //in the settings, we have an option to hide descriptions containing
    // exception rules (for better retention).
    //do that here
    private void handleExceptionRules(View parentView){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        //the preference is still stored as a string
        boolean shouldShow = sharedPreferences.getBoolean
                (getString(R.string.preferences_questions_descriptionBeforeLessonWithExceptionRule_screen_key), true);
        ArrayList<View> viewsWithExceptionTag = new ArrayList<>();
        parentView.findViewsWithText(viewsWithExceptionTag, "<exception>", View.FIND_VIEWS_WITH_TEXT);

        for (View view : viewsWithExceptionTag){
            if (view instanceof TextView){
                TextView textView = (TextView)view;
                String text = textView.getText().toString();
                if (!shouldShow){
                    //remove everything within the tags (there may be multiple)
                    text = text.replaceAll("<exception>([^<]*)</exception>", "");
                } else {
                    text = text.replaceAll("<exception>", "");
                    text = text.replaceAll("</exception>", "");
                }

                textView.setText(text);
            }
        }
    }
}
