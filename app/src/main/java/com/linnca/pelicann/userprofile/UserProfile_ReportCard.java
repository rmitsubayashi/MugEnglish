package com.linnca.pelicann.userprofile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.lessonlist.LessonHierarchyViewer;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserProfile_ReportCard extends Fragment {
    private RecyclerView list;
    private UserProfile_ReportCardAdapter adapter;
    private String userID;
    private int lessonLevel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        lessonLevel = preferences.getInt(getString(R.string.preferences_report_card_last_selected_lesson_level), 1);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_report_card, container, false);
        list = view.findViewById(R.id.user_profile_report_card_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        populateReportCard();
        return view;
    }

    private void populateReportCard(){
        if (adapter != null){
            if (list.getAdapter() == null){
                list.setAdapter(adapter);
            }
        } else {
            adapter = new UserProfile_ReportCardAdapter(lessonLevel, userID, new UserProfile_ReportCardAdapter.ReportCardListener() {
                @Override
                public void onItemClicked() {

                }
            });
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if (adapter != null){
            adapter.removeValueEventListeners();
        }
    }



}
