package com.linnca.pelicann.searchinterests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

class SearchResultsRecommendationHeaderViewHolder extends RecyclerView.ViewHolder{
    private final TextView title;

    SearchResultsRecommendationHeaderViewHolder(View itemView){
        super(itemView);
        title = itemView.findViewById(R.id.search_interests_recommendations_title);
    }

    public void setTitle(String itemLabel, boolean recommendationsAvailable){
        if (recommendationsAvailable) {
            title.setText(itemView.getContext().getString(R.string.search_interests_recommendations_title, itemLabel));
        } else {
            title.setText(itemView.getContext().getString(R.string.search_interests_recommendations_title_no_recommendations));
        }
    }

}
