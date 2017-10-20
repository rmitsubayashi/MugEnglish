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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LessonList extends Fragment {
    private final String TAG = "LessonList";
    public static final String LESSON_LEVEL = "lessonLevel";
    private final String SAVED_STATE_LIST_STATE = "listState";
    private RecyclerView listView;
    private int lessonLevel;
    private RecyclerView.LayoutManager layoutManager;
    private LessonListAdapter adapter;
    private DatabaseReference clearedLessonsRef;
    private ValueEventListener clearedLessonsListener;

    private LessonListListener listener;

    public interface LessonListListener {
        void lessonListToLessonDetails(LessonData lessonData);
        void lessonListToReview(String key);
        void setToolbarState(ToolbarState state);
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
        clearedLessonsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                        FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" +
                        lessonLevel
        );
        clearedLessonsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> clearedLessons = new HashSet<>((int)dataSnapshot.getChildrenCount()+1);
                for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()){
                    String lessonKey = lessonSnapshot.getKey();
                    clearedLessons.add(lessonKey);
                }
                LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer();
                //lessonHierarchyViewer.debugUnlockAllLessons();
                List<LessonListRow> lessonRows = lessonHierarchyViewer.getLessonsAtLevel(lessonLevel);
                if (adapter == null) {
                    adapter = new LessonListAdapter(lessonRows, listener, clearedLessons);
                    listView.setAdapter(adapter);
                } else {
                    if (listView.getAdapter() == null){
                        listView.setAdapter(adapter);
                    } else {
                        adapter.setClearedLessonKeys(clearedLessons);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        clearedLessonsRef.addValueEventListener(clearedLessonsListener);

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
        if (clearedLessonsListener != null && clearedLessonsRef != null){
            clearedLessonsRef.removeEventListener(clearedLessonsListener);
        }
    }

}
