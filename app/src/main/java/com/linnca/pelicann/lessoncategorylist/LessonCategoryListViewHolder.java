package com.linnca.pelicann.lessoncategorylist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

public class LessonCategoryListViewHolder extends RecyclerView.ViewHolder {
    final TextView titleTextView;

    LessonCategoryListViewHolder(View itemView){
        super(itemView);
        titleTextView = itemView.findViewById(R.id.lesson_category_list_item_title);
    }

    void setTitle(String title){
        titleTextView.setText(title);
    }
}
