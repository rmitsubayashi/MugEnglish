package com.linnca.pelicann.lessonlist;

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
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.List;
import java.util.Set;

public class LessonList extends Fragment {
    public static final String TAG = "LessonList";
    public static final String LESSON_LEVEL = "lessonLevel";
    private final String SAVED_STATE_LIST_STATE = "listState";
    private Database db;
    private RecyclerView listView;
    private int lessonLevel;
    private RecyclerView.LayoutManager layoutManager;
    private LessonListAdapter adapter;

    private LessonListListener listener;

    public interface LessonListListener {
        void lessonListToLessonDetails(LessonData lessonData);
        void lessonListToReview(int lessonLevel, String key);
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
        View view = inflater.inflate(R.layout.fragment_lesson_list, container, false);
        listView = view.findViewById(R.id.lesson_list_list);
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
                        false, false, null)
            );
        populateLessonList(lessonLevel);
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

    private void populateLessonList(final int lessonLevel){
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onClearedLessonsQueried(Set<String> clearedLessonKeys) {
                LessonListViewer lessonListViewer = new LessonListViewer();
                //lessonListViewer.debugUnlockAllLessons();
                List<LessonListRow> lessonRows = lessonListViewer.getLessonsAtLevel(lessonLevel);
                if (adapter == null) {
                    adapter = new LessonListAdapter(lessonLevel, lessonRows, listener, clearedLessonKeys);
                    listView.setAdapter(adapter);
                } else {
                    if (listView.getAdapter() == null){
                        listView.setAdapter(adapter);
                    } else {
                        adapter.setClearedLessonKeys(clearedLessonKeys);
                    }
                }
            }
        };
        db.getClearedLessons(lessonLevel, true, onResultListener);
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
    }

}
