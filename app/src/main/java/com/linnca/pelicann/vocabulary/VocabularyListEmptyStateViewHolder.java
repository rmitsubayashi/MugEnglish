package com.linnca.pelicann.vocabulary;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.linnca.pelicann.R;

class VocabularyListEmptyStateViewHolder extends RecyclerView.ViewHolder {
    private final Button goToLessonButton;

    VocabularyListEmptyStateViewHolder(View itemView){
        super(itemView);
        goToLessonButton = itemView.findViewById(R.id.vocabulary_list_empty_state_go_to_lesson_button);
    }

    void setListener(final VocabularyListAdapter.VocabularyListAdapterListener listener){
        goToLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.vocabularyListToLessonList();
            }
        });
    }
}
