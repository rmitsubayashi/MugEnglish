package com.linnca.pelicann.userprofile;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.widgets.GUIUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.util.ArrayList;
import java.util.List;

public class CustomCalendarView extends LinearLayout
{
    // maximum days to show on the calendar
    private static final int MAXIMUM_DAYS_COUNT = 42;
    //first day of currently displayed month
    private DateTime currentMonth;
    //adapter
    private CustomCalendarViewAdapter adapter;
    //max/min days/months/years
    //(days needed for the adapter)
    private DateTime minDate;
    private DateTime maxDate;

    private CustomCalendarViewListener listener;

    // internal components
    private Button btnPrev;
    private Button btnNext;
    private TextView txtDate;
    private GridView grid;

    interface CustomCalendarViewListener{
        void onUpdateMonth(int month, int year);
        void onClickItem(DateTime clickedDate);
    }

    public CustomCalendarView(Context context)
    {
        super(context);
    }

    public CustomCalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    private void initControl(Context context, AttributeSet attrs)
    {
        //make sure we initialize with the first day of the month
        currentMonth = DateTime.now().withDayOfMonth(1);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.inflatable_custom_calendar, this);

        assignUiElements();
        assignClickHandlers();

        setAdapter();
    }

    private void assignUiElements()
    {
        btnPrev = findViewById(R.id.calendar_prev_button);
        btnNext = findViewById(R.id.calendar_next_button);
        txtDate = findViewById(R.id.calendar_date_display);
        grid = findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers()
    {
        // add one month and refresh UI.
        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //we disable the button if we can't,
                //but just in case
                if (canDisplayNextMonth()){
                    currentMonth = currentMonth.plusMonths(1).withDayOfMonth(1);
                    adapter.updateCurrentMonth(currentMonth);
                    setTitle();
                    toggleNextButtonVisibility();
                    togglePreviousButtonVisibility();

                    Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
                    slideOut.setInterpolator(new AccelerateInterpolator());
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            adapter.updateCalendarCells(getCalendarCells());
                            if (listener != null)
                                listener.onUpdateMonth(getDisplayedMonth(), getDisplayedYear());
                            Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
                            slideIn.setInterpolator(new DecelerateInterpolator());
                            grid.startAnimation(slideIn);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    grid.startAnimation(slideOut);

                }
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (canDisplayPreviousMonth()){
                    currentMonth = currentMonth.minusMonths(1).withDayOfMonth(1);
                    adapter.updateCurrentMonth(currentMonth);
                    setTitle();
                    togglePreviousButtonVisibility();
                    toggleNextButtonVisibility();
                    Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
                    slideOut.setInterpolator(new AccelerateInterpolator());
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            adapter.updateCalendarCells(getCalendarCells());
                            if (listener != null)
                                listener.onUpdateMonth(getDisplayedMonth(), getDisplayedYear());
                            Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
                            slideIn.setInterpolator(new DecelerateInterpolator());
                            grid.startAnimation(slideIn);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    grid.startAnimation(slideOut);
                }
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> view, View cell, int position, long id)
            {
                if (listener == null)
                    return;
                if (!cell.isEnabled()){
                    return;
                }
                try {
                    DateTime clickedDate = (DateTime) view.getItemAtPosition(position);
                    listener.onClickItem(clickedDate);
                } catch (ClassCastException e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setListener(CustomCalendarViewListener listener){
        this.listener = listener;
    }

    public void setMin(DateTime date){
        this.minDate = date;
        togglePreviousButtonVisibility();
        //this should be the first time
        btnPrev.setVisibility(VISIBLE);
        adapter.setMin(date);
    }

    public void setMax(DateTime date){
        this.maxDate = date;
        toggleNextButtonVisibility();
        //this should be the first time
        btnNext.setVisibility(VISIBLE);
        adapter.setMax(date);
    }

    private int getDisplayedMonth(){
        return currentMonth.getMonthOfYear();
    }

    private int getDisplayedYear(){
        return currentMonth.getYear();
    }

    public void setUsageData(List<AppUsageLog> logs, boolean initial){
        adapter.setUsageData(logs, initial);
    }

    private void setAdapter(){
        setTitle();
        ArrayList<DateTime> calendarCells = getCalendarCells();
        adapter = new CustomCalendarViewAdapter(getContext(), calendarCells, currentMonth);
        grid.setAdapter(adapter);
    }

    private ArrayList<DateTime> getCalendarCells()
    {
        ArrayList<DateTime> cells = new ArrayList<>();
        DateTime day = currentMonth.withDayOfWeek(DateTimeConstants.SUNDAY);
        if (day.isAfter(currentMonth)){
            day = day.minusWeeks(1);
        }

        // fill cells
        while (cells.size() < MAXIMUM_DAYS_COUNT)
        {
            /*
            //I don't think the extra row (if all that row is dates from another month)
            //is necessary, but Google has the extra row in its calendars for PC and Android.
            //maybe it's better UX??
            if (    day.isAfter(currentMonth) &&
                    day.monthOfYear().get() != currentMonth.monthOfYear().get() &&
                    day.dayOfWeek().get() == DateTimeConstants.SUNDAY){
                break;
            }*/
            cells.add(day);
            day = day.plusDays(1);
        }

        return cells;
    }

    private void setTitle(){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy年 MM月");
        txtDate.setText(dtf.print(currentMonth));
    }

    private boolean canDisplayNextMonth(){
        if (maxDate == null){
            return false;
        }

        if (maxDate.getYear() == currentMonth.getYear()) {
            if (maxDate.getMonthOfYear() > currentMonth.getMonthOfYear()) {
                return true;
            }
        } else if (maxDate.getYear() > currentMonth.getYear()){
            return true;
        }

        return false;
    }

    private boolean canDisplayPreviousMonth(){
        if (minDate == null){
            return false;
        }
        if (minDate.getYear() == currentMonth.getYear()) {
            if (minDate.getMonthOfYear() < currentMonth.getMonthOfYear()) {
                return true;
            }
        } else if (minDate.getYear() < currentMonth.getYear()){
            return true;
        }

        return false;
    }

    private void togglePreviousButtonVisibility(){
        if (canDisplayPreviousMonth()){
            btnPrev.setEnabled(true);
            btnPrev.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            btnPrev.setEnabled(false);
            btnPrev.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
        }
    }

    private void toggleNextButtonVisibility(){
        if (canDisplayNextMonth()){
            btnNext.setEnabled(true);
            btnNext.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            btnNext.setEnabled(false);
            btnNext.setTextColor(ContextCompat.getColor(getContext(), R.color.gray500));
        }
    }
}