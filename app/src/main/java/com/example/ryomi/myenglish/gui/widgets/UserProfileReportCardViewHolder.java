package com.example.ryomi.myenglish.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;


public class UserProfileReportCardViewHolder extends RecyclerView.ViewHolder {
    private final TextView themeTextView;
    private final TextView instanceCtTextView;
    private final TextView recordCtTextView;
    private final TextView accuracyTextView;

    public UserProfileReportCardViewHolder(View view){
        super(view);
        themeTextView = (TextView) view.findViewById(R.id.user_profile_report_card_item_theme);
        instanceCtTextView = (TextView) view.findViewById(R.id.user_profile_report_card_item_number_instances);
        recordCtTextView = (TextView) view.findViewById(R.id.user_profile_report_card_item_number_records);
        accuracyTextView = (TextView) view.findViewById(R.id.user_profile_report_card_item_accuracy);

    }

    public void setTheme(String theme){
        themeTextView.setText(theme);
    }

    public void setInstanceCtTextView(String instanceCt){
        instanceCtTextView.setText(instanceCt);
    }

    public void setRecordCtTextView(String recordCt){
        recordCtTextView.setText(recordCt);
    }

    public void setAccuracyTextView(String accuracy){
        accuracyTextView.setText(accuracy);
    }
}
