package com.linnca.pelicann.lessonlist;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;
import pelicann.linnca.com.corefunctionality.lessonlist.UserLessonListViewer;

class LessonListReviewRowViewHolder extends RecyclerView.ViewHolder {
    private final Button button;
    LessonListReviewRowViewHolder(View itemView){
        super(itemView);
        button = itemView.findViewById(R.id.lesson_list_list_review_item_button);
    }

    void populateRow(final LessonData data, final LessonList.LessonListListener listener, int status){
        button.setText(data.getTitle());
        if (status == UserLessonListViewer.STATUS_ACTIVE){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.lessonListToReviewLesson(data.getKey());
                }
            });
            int color = ThemeColorChanger.getColorFromAttribute(R.attr.colorAccent500, itemView.getContext());
            button.setTextColor(color);
        } else {
            button.setOnClickListener(null);
            button.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray500));
        }

        if (status == UserLessonListViewer.STATUS_CLEARED){
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
        } else {
            button.setOnTouchListener(null);
        }
    }
}
