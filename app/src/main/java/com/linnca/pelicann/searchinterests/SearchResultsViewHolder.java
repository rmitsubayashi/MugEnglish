package com.linnca.pelicann.searchinterests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;

class SearchResultsViewHolder extends RecyclerView.ViewHolder {
    private final TextView name;
    private final TextView description;

    public SearchResultsViewHolder(View itemView){
        super(itemView);
        name = itemView.findViewById(R.id.search_interests_result_label);
        description = itemView.findViewById(R.id.search_interests_result_description);
    }

    public void setLabel(String label){ this.name.setText(label);}

    public void setDescription(String description){this.description.setText(description);}

    public void setButtonListener(View.OnClickListener listener){
        this.itemView.setOnClickListener(listener);
    }
}
