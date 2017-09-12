package com.linnca.pelicann.searchinterests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.linnca.pelicann.R;

class SearchResultsFooterViewHolder extends RecyclerView.ViewHolder {
    private final Button button;

    SearchResultsFooterViewHolder(View itemView){
        super(itemView);
        button = itemView.findViewById(R.id.search_interests_recommendations_more_button);
    }

    public void setButton(View.OnClickListener listener){
        button.setOnClickListener(listener);
    }
}
