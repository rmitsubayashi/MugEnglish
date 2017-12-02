package com.linnca.pelicann.userprofile;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.List;

public class User_Profile_ReportCardHeaderViewHolder extends RecyclerView.ViewHolder{
    private final Spinner spinner;

    User_Profile_ReportCardHeaderViewHolder(View itemView){
        super(itemView);
        this.spinner = itemView.findViewById(R.id.user_profile_report_card_spinner);
        setSpinnerAdapter(itemView.getContext());
    }

    void setOnSpinnerItemChangeListener(AdapterView.OnItemSelectedListener listener){
        spinner.setOnItemSelectedListener(listener);
    }

    private void setSpinnerAdapter(Context context){
        List<String> items = new ArrayList<>(2);
        items.add(context.getString(R.string.lessons_level1));
        items.add(context.getString(R.string.lessons_level2));
        ReportCardLessonLevelSpinnerAdapter adapter = new ReportCardLessonLevelSpinnerAdapter((Activity)context, items);
        spinner.setAdapter(adapter);
    }
}
