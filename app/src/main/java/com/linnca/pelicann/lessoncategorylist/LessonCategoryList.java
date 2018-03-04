package com.linnca.pelicann.lessoncategorylist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;

import java.util.List;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonCategory;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonListViewer;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonListViewerImplementation;

public class LessonCategoryList extends Fragment {
    public static final String TAG = "LessonList";
    public static final String LESSON_LEVEL = "lessonLevel";
    private final String SAVED_STATE_LIST_STATE = "listState";
    private Database db;
    private RecyclerView listView;
    private int lessonLevel;
    private RecyclerView.LayoutManager layoutManager;
    private LessonCategoryListAdapter adapter;

    private LessonCategoryListListener listener;

    public interface LessonCategoryListListener {
        void lessonCategoryListToLessonScript(LessonCategory lessonCategory);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            //hard code a new database instance
            db = new FirebaseDB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_lesson_category_list, container, false);
        listView = view.findViewById(R.id.lesson_category_list_list);
        layoutManager = new LinearLayoutManager(getContext());
        //restore the list to the state it was in before (better ux)
        if (savedInstanceState != null &&
                savedInstanceState.getParcelable(SAVED_STATE_LIST_STATE) != null){
            layoutManager.onRestoreInstanceState(
                    savedInstanceState.getParcelable(SAVED_STATE_LIST_STATE)
            );
        }
        listView.setLayoutManager(layoutManager);
        lessonLevel = getArguments().getInt(LESSON_LEVEL);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_lesson_list_title),
                        false)
        );
        populateLessonCategoryList(lessonLevel);
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
            listener = (LessonCategoryListListener)context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void populateLessonCategoryList(final int lessonLevel){
        LessonListViewer lessonListViewer = new LessonListViewerImplementation();
        List<LessonCategory> categories = lessonListViewer.getLessonsAtLevel(lessonLevel);
        if (adapter == null) {
            adapter = new LessonCategoryListAdapter(lessonLevel, categories, listener);
            listView.setAdapter(adapter);
        } else {
            if (listView.getAdapter() == null){
                listView.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Parcelable listState = layoutManager.onSaveInstanceState();
        outState.putParcelable(SAVED_STATE_LIST_STATE, listState);
    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
        adapter = null;
    }

}
