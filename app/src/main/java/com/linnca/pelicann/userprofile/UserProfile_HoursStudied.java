package com.linnca.pelicann.userprofile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;

import org.joda.time.DateTime;

import java.util.List;

public class UserProfile_HoursStudied extends Fragment {
    private final String TAG = "UserProfileHoursStudied";
    private final Database db = new FirebaseDB();
    private CustomCalendarView calendarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_hours_studied, container, false);
        calendarView = view.findViewById(R.id.user_profile_hours_studied_calendar);
        setCalendarMinMax();
        setUsageDataForCurrentMonth();
        setCalendarListeners();
        return view;
    }

    private void setCalendarMinMax(){
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onFirstAppUsageDateQueried(DateTime date) {
                calendarView.setMin(date);
                DateTime now = DateTime.now();
                calendarView.setMax(now);
            }
        };
        db.getFirstAppUsageDate(onResultListener);
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

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onAppUsageForMonthsQueried(List<AppUsageLog> logs) {
                calendarView.setUsageData(logs, initial);
            }
        };
        db.getAppUsageForMonths(prevKey, nextKey, onResultListener);
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
