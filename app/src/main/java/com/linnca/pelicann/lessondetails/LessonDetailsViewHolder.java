package com.linnca.pelicann.lessondetails;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

//holder for user interest list cells
class LessonDetailsViewHolder extends RecyclerView.ViewHolder{
    private final TextView interestsLabel;
    private final TextView created;

    public LessonDetailsViewHolder(View itemView) {
        super(itemView);
        interestsLabel = itemView.findViewById(R.id.lesson_details_item_topics);
        created = itemView.findViewById(R.id.lesson_details_item_date_created);

    }

    void setInterestsLabel(String text){
        interestsLabel.setText(text);
    }

    void setCreated(String text){
        created.setText(text);
    }


}