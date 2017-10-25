package com.linnca.pelicann.vocabulary;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.List;

public class VocabularyListViewHolder extends RecyclerView.ViewHolder {
    private final TextView wordTextView;
    private final TextView meaningTextView;

    VocabularyListViewHolder(View itemView){
        super(itemView);
        wordTextView = itemView.findViewById(R.id.vocabulary_list_item_word);
        meaningTextView = itemView.findViewById(R.id.vocabulary_list_item_meaning);
    }

    void setWord(String word){
        wordTextView.setText(word);
    }

    void setMeaning(List<String> meanings){
        String meaningString = "";
        for (String meaning : meanings){
            meaningString += meaning + "、";
        }
        meaningString = meaningString.substring(0, meaningString.length()-1);

        meaningTextView.setText(meaningString);
    }
}
