package com.example.ryomi.myenglish.gui.widgets;


import android.view.View;

import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class UserInterestAdapter
        extends FirebaseRecyclerAdapter<WikiDataEntryData, UserInterestViewHolder> {

    public UserInterestAdapter(Class<WikiDataEntryData> dataClass, int layoutID,
                               Class<UserInterestViewHolder> viewHolderClass, DatabaseReference ref){
        super(dataClass, layoutID, viewHolderClass, ref);
    }

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
    public void onBindViewHolder(UserInterestViewHolder holder, final int position){
        super.onBindViewHolder(holder, position);
        final DatabaseReference ref = getRef(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.removeValue();
            }
        });

    }
}
