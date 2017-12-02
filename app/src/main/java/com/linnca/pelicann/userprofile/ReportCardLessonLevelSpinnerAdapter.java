package com.linnca.pelicann.userprofile;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.List;

class ReportCardLessonLevelSpinnerAdapter extends ArrayAdapter<String> {

    private final List<String> list;
    private final LayoutInflater layoutInflater;

    ReportCardLessonLevelSpinnerAdapter(Activity context, List<String> list){
        super(context, R.layout.inflatable_user_profile_report_card_lesson_level_spinner_item, list);
        layoutInflater = context.getLayoutInflater();
        this.list = list;

    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.inflatable_user_profile_report_card_lesson_level_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.user_profile_report_card_spinner_item_text);
        String item = list.get(position);
        textView.setText(item);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.inflatable_user_profile_report_card_lesson_level_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.user_profile_report_card_spinner_item_text);
        String item = list.get(position);
        textView.setText(item);

        return convertView;
    }
}
