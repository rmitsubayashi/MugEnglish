package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.gui.widgets.LessonDescriptionLayoutHelper;
import com.linnca.pelicann.gui.widgets.ToolbarState;
import com.linnca.pelicann.questiongenerator.LessonHierarchyViewer;

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
        LessonDescriptionLayoutHelper helper = new LessonDescriptionLayoutHelper();
        Integer layoutID = helper.getLayoutID(lessonKey);
        if (layoutID == null){
            //layout if we can't find one (we should preemptively handle it either way)
            return inflater.inflate(R.layout.fragment_lesson_description_not_found, container, false);
        }

        View view = inflater.inflate(layoutID, container, false);
        TextView lessonTitleTextView = view.findViewById(R.id.description_lesson_title);
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer(getContext());
        LessonData lessonData = lessonHierarchyViewer.getLessonData(lessonKey);
        if (lessonData != null)
            lessonTitleTextView.setText(lessonData.getTitle());
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
}
