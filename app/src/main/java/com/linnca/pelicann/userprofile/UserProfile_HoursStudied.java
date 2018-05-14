package com.linnca.pelicann.userprofile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.AndroidNetworkConnectionChecker;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;

import org.joda.time.DateTime;

import java.util.List;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

public class UserProfile_HoursStudied extends Fragment {
    public static final String TAG = "UserProfileHoursStudied";
    private Database db;
    private CustomCalendarView calendarView;

    private UserProfile_HoursStudiedListener userProfileHoursStudiedListener;

    public interface UserProfile_HoursStudiedListener {
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        userProfileHoursStudiedListener.setToolbarState(
                new ToolbarState(getString(R.string.user_profile_hours_studied),
                false)
        );
    }

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
            userProfileHoursStudiedListener = (UserProfile_HoursStudiedListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void setCalendarMinMax(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onFirstAppUsageDateQueried(DateTime date) {
                calendarView.setMin(date);
                DateTime now = DateTime.now();
                calendarView.setMax(now);
            }
            @Override
            public void onNoConnection(){
                //notifying the user that there is no connection
                // is handled when we get the actual data
            }
        };
        db.getFirstAppUsageDate(onDBResultListener);
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

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onAppUsageForMonthsQueried(List<AppUsageLog> logs) {
                calendarView.setUsageData(logs, initial);
            }
            @Override
            public void onNoConnection(){
                //the empty calendar is enough to indicate no data.
                //just let the user know that this is because of no connection.
                //we do this ONCE here since
                // -this is the first-most fragment in the view pager
                // -neighboring fragments in the view pager are also trying to connect
                //  and will also have to handle no connection state, but we shouldn't
                //  display multiple toasts with the the same message
                Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                        .show();
            }
        };
        NetworkConnectionChecker networkConnectionChecker = new
                AndroidNetworkConnectionChecker(getContext());
        db.getAppUsageForMonths(networkConnectionChecker, prevKey, nextKey, onDBResultListener);
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
