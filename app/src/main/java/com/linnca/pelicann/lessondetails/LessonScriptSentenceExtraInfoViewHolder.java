package com.linnca.pelicann.lessondetails;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

public class LessonScriptSentenceExtraInfoViewHolder extends RecyclerView.ViewHolder  {
    private final TextView contentTextView;
    private final TextView translationTextView;

    LessonScriptSentenceExtraInfoViewHolder(View itemView){
        super(itemView);
        contentTextView = itemView.findViewById(R.id.lesson_script_sentence_extra_info_content);
        translationTextView = itemView.findViewById(R.id.lesson_script_sentence_extra_info_translation);
    }

    void setBackgroundColor(int attrResID){
        itemView.setBackgroundColor(ThemeColorChanger.getColorFromAttribute(attrResID, itemView.getContext()));
    }

    void setContent(String text){
        if (text != null && !text.equals("")) {
            contentTextView.setVisibility(View.VISIBLE);
            contentTextView.setText(text);
        } else {
            contentTextView.setVisibility(View.GONE);
        }
    }

    void setTranslation(String translationJP){
        translationTextView.setText(translationJP);
    }
}
