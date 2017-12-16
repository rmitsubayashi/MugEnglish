package com.linnca.pelicann.searchinterests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

class SearchResultsOfflineAfterAddingViewHolder extends RecyclerView.ViewHolder{
    private final TextView messageTextView;

    SearchResultsOfflineAfterAddingViewHolder(View itemView){
        super(itemView);
        messageTextView = itemView.findViewById(R.id.search_interests_offline_after_adding_message);
    }

    public void setTitle(String itemLabel){
        messageTextView.setText(itemView.getContext().getString(R.string.search_interests_offline_after_adding_message, itemLabel));
    }

}
