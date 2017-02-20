package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.myenglish.db.datawrappers.QuestionAttempt;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Seconds;

import java.util.ArrayList;
import java.util.List;

public class User_Profile_Hours_Studied extends Fragment {

    public User_Profile_Hours_Studied() {
    }


    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_hours_studied, container, false);

        Bundle dataBundle = getArguments();
        List<InstanceRecord> records = (List<InstanceRecord>)dataBundle.getSerializable(null);
        //if we fail to read the records
        if (records == null){
            return view;
        }

        //Mon ~ Sun
        final String[] daysOfTheWeek = getDaysOfTheWeek();
        float[] lastWeekMinutes = lastWeekMinutes(records);
;
        BarChart chart = (BarChart)view.findViewById(R.id.user_profile_hours_studied_chart);
        //set x-axis label
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return daysOfTheWeek[(int) value];
            }
        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);


        List<BarEntry> entries = new ArrayList<>();
        for (int i=0; i<7; i++){
            entries.add(new BarEntry(i,lastWeekMinutes[i]));
        }

        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        chart.setData(data);
        chart.setFitBars(true); // make the x-axis fit exactly all bars
        chart.invalidate(); // refresh

        return view;
    }

    private String[] getDaysOfTheWeek(){
        String[] MonToSun = getResources().getStringArray(R.array.daysOfTheWeek);
        //adjust so that the last day is today
        DateTime dt = new DateTime();

        int startIndex = -1;
        switch (dt.getDayOfWeek()) {
            case DateTimeConstants.MONDAY:
                startIndex = 1;
                break;
            case DateTimeConstants.TUESDAY:
                startIndex = 2;
                break;
            case DateTimeConstants.WEDNESDAY:
                startIndex = 3;
                break;
            case DateTimeConstants.THURSDAY:
                startIndex = 4;
                break;
            case DateTimeConstants.FRIDAY:
                startIndex = 5;
                break;
            case DateTimeConstants.SATURDAY:
                startIndex = 6;
                break;
            case DateTimeConstants.SUNDAY:
                startIndex = 0;
                break;
            default:
                break;
        }
        String[] result = new String[7];
        for (int i = 0; i < 6; i++){
            result[i] = MonToSun[startIndex];
            startIndex = (startIndex + 1) % 7;
        }
        result[6] = getResources().getString(R.string.user_profile_hours_studied_today);

        return result;
    }

    //add up all instance records from the last week
    private float[] lastWeekMinutes(List<InstanceRecord> records){
        DateTime today = new DateTime();
        float[] result = new float[7];
        for (int i=0; i<7; i++){
            result[i] = 0f;
        }

        for (InstanceRecord record : records){
            List<QuestionAttempt> attempts = record.getAttempts();
            DateTime firstAttemptTime = new DateTime(attempts.get(0).getStartTime());
            int daysBetween = Days.daysBetween(firstAttemptTime.toLocalDate(), today.toLocalDate()).getDays();
            //if within last week
            if (daysBetween < 7){
                int indexToUpdate = 6 - daysBetween;
                DateTime lastAttemptTime = new DateTime(attempts.get(attempts.size()-1).getEndTime());
                Log.d("TAG", "DaysBetween Called"+lastAttemptTime.toString());
                int instanceTimeInSeconds = Seconds.secondsBetween(firstAttemptTime, lastAttemptTime).getSeconds();
                result[indexToUpdate] = result[indexToUpdate] + instanceTimeInSeconds;
            }
        }

        //fix seconds to minutes
        for (int i=0; i<7; i++){
            float seconds = result[i];
            float minutes = 0;
            //minimum should be one minute unless the user did nothing that day (seconds = 0)
            minutes = seconds / 60;

            result[i] = minutes;
        }

        return result;
    }
}
