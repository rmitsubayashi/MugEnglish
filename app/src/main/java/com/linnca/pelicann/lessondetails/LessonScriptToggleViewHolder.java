package com.linnca.pelicann.lessondetails;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

class LessonScriptToggleViewHolder extends RecyclerView.ViewHolder {
    private final TextView lessonNumberTextView;
    private final Button prevButton;
    private final Button nextButton;

    LessonScriptToggleViewHolder(View itemView){
        super(itemView);
        lessonNumberTextView = itemView.findViewById(R.id.lesson_script_lesson_number);
        prevButton = itemView.findViewById(R.id.lesson_script_to_prev);
        nextButton = itemView.findViewById(R.id.lesson_script_to_next);
    }

    void setLessonNumber(int number, int total){
        String text = Integer.toString(number) + " / " + Integer.toString(total);
        lessonNumberTextView.setText(text);
    }

    void setLessonToggleButton(boolean enabled, int lessonNumber, int totalLessonCt,
                               final LessonScriptAdapter.LessonScriptAdapterListener listener){
        Context context = itemView.getContext();
        if (!enabled){
            prevButton.setOnClickListener(null);
            prevButton.setTextColor(ContextCompat.getColor(context, R.color.gray500));
            nextButton.setOnClickListener(null);
            nextButton.setTextColor(ContextCompat.getColor(context, R.color.gray500));
            return;
        }

        if (lessonNumber > 1){
            prevButton.setEnabled(true);
            prevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.toPrev();
                }
            });
            prevButton.setTextColor(ThemeColorChanger.getColorFromAttribute(
                    R.attr.colorAccent500, context));
        } else {
            prevButton.setEnabled(false);
            prevButton.setTextColor(ContextCompat.getColor(context, R.color.gray500));
        }

        if (lessonNumber != totalLessonCt){
            nextButton.setEnabled(true);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.toNext();
                }
            });
            nextButton.setTextColor(ThemeColorChanger.getColorFromAttribute(
                    R.attr.colorAccent500, context));
        } else {
            nextButton.setEnabled(false);
            nextButton.setTextColor(ContextCompat.getColor(context, R.color.gray500));
        }
    }
}
