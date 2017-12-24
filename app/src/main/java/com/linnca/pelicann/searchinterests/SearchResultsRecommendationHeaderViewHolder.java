package com.linnca.pelicann.searchinterests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

class SearchResultsRecommendationHeaderViewHolder extends RecyclerView.ViewHolder{
    private final TextView successTitle;
    private final TextView rankingsHeader;

    SearchResultsRecommendationHeaderViewHolder(View itemView){
        super(itemView);
        successTitle = itemView.findViewById(R.id.search_interests_recommendations_success_title);
        rankingsHeader = itemView.findViewById(R.id.search_interests_recommendations_ranking_header);
    }

    public void setTitle(String itemLabel){
        successTitle.setText(itemView.getContext().getString(R.string.search_interests_recommendations_success, itemLabel));
    }

    public void setRankingsHeaderVisibility(boolean isVisible){
        rankingsHeader.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

}
