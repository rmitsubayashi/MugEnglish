package com.example.ryomi.myenglish.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;

//holder for user interest list cells
public class UserInterestViewHolder  extends RecyclerView.ViewHolder {
    private final TextView label;
    private final TextView description;

    public UserInterestViewHolder(View itemView) {
        super(itemView);
        label = (TextView) itemView.findViewById(R.id.user_interests_list_item_label);
        description = (TextView) itemView.findViewById(R.id.user_interests_list_item_description);
    }


    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

}