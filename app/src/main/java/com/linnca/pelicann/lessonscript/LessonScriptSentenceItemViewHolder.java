package com.linnca.pelicann.lessonscript;

import android.content.res.ColorStateList;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

class LessonScriptSentenceItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView iconTextView;
    private final TextView sentenceTextView;

    LessonScriptSentenceItemViewHolder(View itemView){
        super(itemView);
        iconTextView = itemView.findViewById(R.id.lesson_script_sentence_item_icon);
        sentenceTextView = itemView.findViewById(R.id.lesson_script_sentence_item_sentence);
    }

    void setSentence(String sentenceEN){
        sentenceTextView.setText(sentenceEN);
    }

    void setIcon(String speaker, int attrResID){
        if (speaker.equals(ScriptSpeaker.SPEAKER_NONE)){
            iconTextView.setVisibility(View.INVISIBLE);
            //placeholder
            iconTextView.setText("I");
        } else {
            //in case it was invisible
            iconTextView.setVisibility(View.VISIBLE);
            char iconLetter = speaker.charAt(0);
            iconLetter = Character.toUpperCase(iconLetter);
            iconTextView.setText(Character.toString(iconLetter));
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{}
                    },
                    new int[] {
                            ThemeColorChanger.getColorFromAttribute(attrResID, itemView.getContext())
                    }
            );
            ViewCompat.setBackgroundTintList(iconTextView, colorStateList);
        }
    }
}
