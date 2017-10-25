package com.linnca.pelicann.userprofile;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.linnca.pelicann.R;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class CustomCalendarViewAdapter extends ArrayAdapter<DateTime> {
    private LayoutInflater inflater;
    //same instance as the calendar in calendar layout
    private DateTime currentMonth;
    private ArrayList<DateTime> calendarDates;
    private HashMap<String, Integer> usageData = null;
    private DateTime minDate;
    private DateTime maxDate;

    CustomCalendarViewAdapter(Context context, ArrayList<DateTime> days, DateTime currentMonth)
    {
        super(context, R.layout.inflatable_custom_calendar_day_cell, days);
        this.calendarDates = days;
        this.inflater = LayoutInflater.from(context);
        this.currentMonth = currentMonth;
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent)
    {
        // day in question
        DateTime date = getItem(position);

        // inflate item if it does not exist yet
        if (view == null)
            view = inflater.inflate(R.layout.inflatable_custom_calendar_day_cell, parent, false);

        if (date == null){
            return view;
        }

        // clear styling
        ((TextView)view).setTextColor(Color.BLACK);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        view.setEnabled(true);
        view.setOnClickListener(null);

        if (date.getMonthOfYear() != currentMonth.getMonthOfYear())
        {
            // if this day is outside current month, grey it out
            ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.gray500));
            view.setEnabled(false);
        }
        else if (minDate != null && maxDate != null && usageData != null &&
                minDate.withTimeAtStartOfDay().isBefore(date) &&
                maxDate.plusDays(1).withTimeAtStartOfDay().isAfter(date))
        {
            //set background if the user has spent time on the app
            String mapKey = formatUsageDataKey(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
            if (usageData.containsKey(mapKey)) {
                view.setBackgroundResource(R.drawable.calendar_view_cleared);
                ((TextView)view).setTextColor(ContextCompat.getColor(getContext(), R.color.lblue700));
                final int minutesSpent = usageData.get(mapKey);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTimeSpent(minutesSpent, view.getContext());
                    }
                });


            } else {
                view.setBackgroundResource(R.drawable.calendar_view_not_cleared);
                ((TextView)view).setTextColor(ContextCompat.getColor(getContext(), R.color.gray700));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTimeSpent(0, view.getContext());
                    }
                });
            }
        } else {
            view.setEnabled(false);
        }

        // set text
        ((TextView)view).setText(String.valueOf(date.getDayOfMonth()));

        return view;
    }

    void updateCurrentMonth(DateTime newMonth){
        currentMonth = newMonth;
    }

    void updateCalendarCells(ArrayList<DateTime> newCalendarCells){
        calendarDates.clear();
        calendarDates.addAll(newCalendarCells);
        notifyDataSetChanged();
    }

    void setUsageData(List<AppUsageLog> logs, boolean initial){
        if (usageData == null){
            usageData = new HashMap<>();
        } else {
            usageData.clear();
        }
        for (AppUsageLog log : logs){
            DateTime startDateTime = new DateTime(log.getStartTimeStamp());
            DateTime endDateTime = new DateTime(log.getEndTimeStamp());
            int minutesSpentOnApp = Minutes.minutesBetween(startDateTime, endDateTime).getMinutes();
            String mapKey = formatUsageDataKey(startDateTime.getYear(), startDateTime.getMonthOfYear(), startDateTime.getDayOfMonth());
            if (usageData.containsKey(mapKey)){
                int currentMinutes = usageData.get(mapKey);
                int newMinutes = currentMinutes + minutesSpentOnApp;
                usageData.put(mapKey, newMinutes);
            } else {
                usageData.put(mapKey, minutesSpentOnApp);
            }
        }
        //so we don't do any unnecessary redrawing.
        //animations are less clunky?? (maybe just me)
        if (initial) {
            notifyDataSetChanged();
        }
    }

    void setMin(DateTime date){
        this.minDate = date;
        notifyDataSetChanged();
    }

    void setMax(DateTime date){
        this.maxDate = date;
        notifyDataSetChanged();
    }

    private void showTimeSpent(int minutes, Context context){
        String displayString;
        if (minutes < 60){
            displayString = Integer.toString(minutes) + "分";
        } else {
            int hours = minutes / 60;
            displayString = Integer.toString(hours) + "時間";
        }
        Toast.makeText(context, displayString, Toast.LENGTH_SHORT).show();
    }

    private String formatUsageDataKey(int year, int month, int day){
        return year + "-" + month + "-" + day;
    }
}
