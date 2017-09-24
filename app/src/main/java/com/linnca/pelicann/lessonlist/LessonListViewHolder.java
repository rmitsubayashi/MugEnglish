package com.linnca.pelicann.lessonlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.List;

//holder for user interest list cells
class LessonListViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;

    LessonListViewHolder(View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.lesson_list_item_text);

    }

    public void setText(String text) {
        this.text.setText(text);
    }
}