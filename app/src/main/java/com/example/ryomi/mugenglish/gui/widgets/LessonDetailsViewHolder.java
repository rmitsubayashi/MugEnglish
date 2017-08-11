package com.example.ryomi.mugenglish.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;

//holder for user interest list cells
class LessonDetailsViewHolder extends RecyclerView.ViewHolder{
    private final TextView topics;
    private final TextView created;

    public LessonDetailsViewHolder(View itemView) {
        super(itemView);
        topics = (TextView) itemView.findViewById(R.id.lesson_details_item_topics);
        created = (TextView) itemView.findViewById(R.id.lesson_details_item_date_created);

    }

    public void setTopics(String text){
        topics.setText(text);
    }

    void setCreated(String text){
        created.setText(text);
    }


}