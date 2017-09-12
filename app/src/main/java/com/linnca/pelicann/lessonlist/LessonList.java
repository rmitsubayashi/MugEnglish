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
    public static final String LESSON_CATEGORY_ID = "lessonCategoryID";
    private RecyclerView listView;
    private int lessonCategoryID;

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
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer(getContext());
        listener.setToolbarState(
                new ToolbarState(lessonHierarchyViewer.getLessonCategory(lessonCategoryID).getTitle(),
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

    private void populateLessonList(int lessonCategoryID){
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer(getContext());
        LessonCategory lessonCategory = lessonHierarchyViewer.getLessonCategory(lessonCategoryID);
        List<LessonData> lessonDataList = lessonCategory.getLessons();
        LessonListAdapter adapter = new LessonListAdapter(lessonDataList, listener);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(adapter);
    }



}
