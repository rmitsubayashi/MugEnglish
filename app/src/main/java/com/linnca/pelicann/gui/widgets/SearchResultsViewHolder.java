package com.linnca.pelicann.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;

public class SearchResultsViewHolder extends RecyclerView.ViewHolder {
    private final TextView name;
    private final TextView description;
    private final Button addButton;

    public SearchResultsViewHolder(View itemView){
        super(itemView);
        name = itemView.findViewById(R.id.search_interests_result_label);
        description = itemView.findViewById(R.id.search_interests_result_description);
        addButton = itemView.findViewById(R.id.search_interests_result_add_button);
    }

    public void setLabel(String label){ this.name.setText(label);}

    public void setDescription(String description){this.description.setText(description);}

    public void setButtonListener(View.OnClickListener listener){
        this.addButton.setOnClickListener(listener);
    }
}
