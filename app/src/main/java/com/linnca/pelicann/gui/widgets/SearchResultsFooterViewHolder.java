package com.linnca.pelicann.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.linnca.pelicann.R;

public class SearchResultsFooterViewHolder extends RecyclerView.ViewHolder {
    private final Button button;

    public SearchResultsFooterViewHolder(View itemView){
        super(itemView);
        button = itemView.findViewById(R.id.search_interests_recommendations_more_button);
    };

    public void setButton(View.OnClickListener listener){
        button.setOnClickListener(listener);
    }
}
