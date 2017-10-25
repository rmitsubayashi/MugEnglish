package com.linnca.pelicann.userprofile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class UserProfile_HoursStudied extends Fragment {
    private final String TAG = "UserProfileHoursStudied";
    private CustomCalendarView calendarView;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_hours_studied, container, false);
        calendarView = view.findViewById(R.id.user_profile_hours_studied_calendar);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setCalendarMinMax();
        setUsageDataForCurrentMonth();
        setCalendarListeners();
        return view;
    }

    private void setCalendarMinMax(){
        DatabaseReference usageRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                userID
        );
        Query minimumMonthRef = usageRef.orderByKey().limitToFirst(1);
        minimumMonthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //only loops once
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DateTime minDate = DateTime.now();
                    List<AppUsageLog> logs = new ArrayList<>((int) snapshot.getChildrenCount());
                    for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                        AppUsageLog log = logSnapshot.getValue(AppUsageLog.class);
                        DateTime startDateTime = new DateTime(log.getStartTimeStamp());
                        if (startDateTime.isBefore(minDate)){
                            minDate = startDateTime;
                        }
                    }
                    calendarView.setMin(minDate);
                    DateTime now = DateTime.now();
                    calendarView.setMax(now);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUsageDataForCurrentMonth(){
        DateTime today = DateTime.now();
        int month = today.getMonthOfYear();
        int year = today.getYear();
        setUsageDataForMonth(month, year, true);
    }

    private void setUsageDataForMonth(int month, int year, final boolean initial){
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;
        int nextMonth = month == 12 ? 1 : month + 1;
        int nextYear = month == 12 ? year + 1 : year;

        String prevKey = AppUsageLog.formatKey(prevMonth, prevYear);
        String nextKey = AppUsageLog.formatKey(nextMonth, nextYear);
        DatabaseReference usageRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                        userID + "/"
        );
        Query usageRefForThreeMonths = usageRef.orderByKey().startAt(prevKey).endAt(nextKey);
        usageRefForThreeMonths.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AppUsageLog> logsForMonth = new ArrayList<>();
                for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot logSnapshot : monthSnapshot.getChildren()) {
                        AppUsageLog log = logSnapshot.getValue(AppUsageLog.class);
                        logsForMonth.add(log);
                    }
                }

                calendarView.setUsageData(logsForMonth, initial);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setCalendarListeners(){
        CustomCalendarView.CustomCalendarViewListener listener = new CustomCalendarView.CustomCalendarViewListener() {
            @Override
            public void onUpdateMonth(int month, int year) {
                setUsageDataForMonth(month, year, false);
            }

            @Override
            public void onClickItem(DateTime clickedDate) {

            }
        };

        calendarView.setListener(listener);
    }
}
