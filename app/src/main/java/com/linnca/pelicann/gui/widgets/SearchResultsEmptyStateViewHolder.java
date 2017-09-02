package com.linnca.pelicann.gui.widgets;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

public class SearchResultsEmptyStateViewHolder extends RecyclerView.ViewHolder {
    private final TextView titleTextView;

    public SearchResultsEmptyStateViewHolder(View itemView){
        super(itemView);
        titleTextView = itemView.findViewById(R.id.search_interests_empty_state_title);
    }

    public void setQuery(String query){
        //shouldn't happen but just in case
        if (query == null || query.equals("")){
            titleTextView.setText(itemView.getContext().getString(R.string.search_interests_empty_state_title_backup));
        } else {
            SpannableString spannableString = new SpannableString(
                    itemView.getContext().getString(R.string.search_interests_empty_state_title, query)
            );
            //highlight the query to make it easier to see
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), R.color.orange500)),
                    1, query.length()+1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            titleTextView.setText(spannableString);

        }
    }
}
