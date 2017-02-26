package com.example.ryomi.mugenglish.gui.widgets;


import android.view.View;

import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class UserInterestAdapter
        extends FirebaseRecyclerAdapter<WikiDataEntryData, UserInterestViewHolder> {

    //Query instead of reference so we can order the data alphabetically
    public UserInterestAdapter(Class<WikiDataEntryData> dataClass, int layoutID,
                               Class<UserInterestViewHolder> viewHolderClass, Query query){
        super(dataClass, layoutID, viewHolderClass, query);
    }

    @Override
    public void populateViewHolder(UserInterestViewHolder holder, WikiDataEntryData data, int position) {
        holder.setLabel(data.getLabel());
        holder.setDescription(data.getDescription());
    }


    @Override
    public void onBindViewHolder(UserInterestViewHolder holder, int position){
        super.onBindViewHolder(holder, position);
        final DatabaseReference ref = getRef(position);
        holder.setButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.removeValue();
            }
        });

    }
}
