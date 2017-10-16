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
    private RecyclerView listView;
    private int lessonLevel;
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
        lessonLevel = getArguments().getInt(LESSON_LEVEL);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(
                new ToolbarState(getString(R.string.fragment_lesson_list_title),
                        false, null)
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

    private void populateLessonList(int lessonLevel){
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer();
        //lessonHierarchyViewer.debugUnlockAllLessons();
        List<LessonListRow> lessonRows = lessonHierarchyViewer.getLessonsAtLevel(lessonLevel);
        final LessonListAdapter adapter = new LessonListAdapter(lessonRows, listener);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(adapter);
        clearedLessonsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                        FirebaseAuth.getInstance().getCurrentUser().getUid() + "/"
        );
        clearedLessonsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> clearedLessons = new HashSet<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()){
                    String lessonKey = lessonSnapshot.getKey();
                    clearedLessons.add(lessonKey);
                }
                adapter.setClearedLessonKeys(clearedLessons);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        clearedLessonsRef.addValueEventListener(clearedLessonsListener);

    }

    @Override
    public void onStop(){
        super.onStop();
        if (clearedLessonsListener != null && clearedLessonsRef != null){
            clearedLessonsRef.removeEventListener(clearedLessonsListener);
        }
    }

}
