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

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.mainactivity.MainActivity;

import java.util.List;

public class UserProfile_ReportCard extends Fragment {
    private RecyclerView list;
    private UserProfile_ReportCardAdapter adapter;
    private int lessonLevel;
    private Database db;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        lessonLevel = preferences.getInt(getString(R.string.preferences_report_card_last_selected_lesson_level), 1);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_report_card, container, false);
        list = view.findViewById(R.id.user_profile_report_card_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        populateReportCard();
    }

    private void populateReportCard(){
        //for when the user changes a level
        // and already has an adapter
        if (adapter != null){
            if (list.getAdapter() == null){
                list.setAdapter(adapter);
            } else {
                //if this is the same lesson,
                //we don't need to re-populate anything
                if (adapter.getLessonLevel() == lessonLevel) {
                    return;
                } else {
                    //if the user was viewing another level,
                    //remove the listeners to that level
                    //before listening to this level
                    db.cleanup();
                }
            }
        } else {
            adapter = new UserProfile_ReportCardAdapter(lessonLevel, new UserProfile_ReportCardAdapter.ReportCardListener() {
                @Override
                public void onItemClicked() {

                }
            });
            list.setAdapter(adapter);
        }
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onReportCardQueried(List<UserProfile_ReportCardDataWrapper> reportCardInfo) {
                adapter.setData(reportCardInfo);
            }
        };

        db.getReportCard(lessonLevel, onResultListener);

    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
    }

}
