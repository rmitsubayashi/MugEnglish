package com.linnca.pelicann.lessonlist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.List;

public class LessonList extends Fragment {
    private final String TAG = "LessonList";
    public static final String LESSON_LEVEL = "lessonLevel";
    private RecyclerView listView;
    private int lessonLevel;

    private LessonListListener listener;

    public interface LessonListListener {
        void lessonListToLessonDetails(LessonData lessonData);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_lesson_list, container, false);
        listView = view.findViewById(R.id.lesson_list_list);
        lessonLevel = getArguments().getInt(LESSON_LEVEL);
        populateLessonList(lessonLevel);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_lesson_list_title),
                        false, null)
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
            listener = (LessonListListener)context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void populateLessonList(int lessonLevel){
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer();
        List<LessonListRow> lessonRows = lessonHierarchyViewer.getLessonsAtLevel(lessonLevel);
        LessonListAdapter adapter = new LessonListAdapter(lessonRows, listener);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(adapter);
    }



}
