package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.LessonCategory;
import com.linnca.pelicann.db.datawrappers.LessonData;
import com.linnca.pelicann.gui.widgets.LessonListAdapter;
import com.linnca.pelicann.gui.widgets.ToolbarState;
import com.linnca.pelicann.questiongenerator.LessonHierarchyViewer;

import java.util.List;

public class LessonList extends Fragment {
    private final String TAG = "LessonList";
    public static final String LESSON_CATEGORY_ID = "lessonCategoryID";
    private RecyclerView listView;
    int lessonCategoryID;

    private LessonListListener listener;

    public interface LessonListListener {
        void lessonListToLessonDetails(LessonData lessonData, int backgroundColor);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_lesson_list, container, false);
        listView = view.findViewById(R.id.lesson_list_list);
        lessonCategoryID = getArguments().getInt(LESSON_CATEGORY_ID);
        populateLessonList(lessonCategoryID);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart() : " + lessonCategoryID);
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer(getContext());
        listener.setToolbarState(
                new ToolbarState(lessonHierarchyViewer.getLessonCategory(lessonCategoryID).getTitle(),
                        false, false, null)
            );
    }


    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop()");
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
            listener = (LessonListListener)context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void populateLessonList(int lessonCategoryID){
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer(getContext());
        LessonCategory lessonCategory = lessonHierarchyViewer.getLessonCategory(lessonCategoryID);
        List<LessonData> lessonDataList = lessonCategory.getLessons();
        LessonListAdapter adapter = new LessonListAdapter(lessonDataList, listener);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(adapter);
    }



}
