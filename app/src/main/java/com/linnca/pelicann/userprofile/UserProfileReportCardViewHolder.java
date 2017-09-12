package com.linnca.pelicann.userprofile;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;


class UserProfileReportCardViewHolder extends RecyclerView.ViewHolder {
    private final TextView themeTextView;
    private final TextView instanceCtTextView;
    private final TextView recordCtTextView;
    private final TextView accuracyTextView;

    UserProfileReportCardViewHolder(View view){
        super(view);
        themeTextView = view.findViewById(R.id.user_profile_report_card_item_theme);
        instanceCtTextView = view.findViewById(R.id.user_profile_report_card_item_number_instances);
        recordCtTextView = view.findViewById(R.id.user_profile_report_card_item_number_records);
        accuracyTextView = view.findViewById(R.id.user_profile_report_card_item_accuracy);

    }

    public void setTheme(String theme){
        themeTextView.setText(theme);
    }

    void setInstanceCtTextView(String instanceCt){
        instanceCtTextView.setText(instanceCt);
    }

    void setRecordCtTextView(String recordCt){
        recordCtTextView.setText(recordCt);
    }

    void setAccuracyTextView(String accuracy){
        accuracyTextView.setText(accuracy);
    }
}
