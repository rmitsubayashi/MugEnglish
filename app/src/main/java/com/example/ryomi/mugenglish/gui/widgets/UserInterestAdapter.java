package com.example.ryomi.mugenglish.gui.widgets;


import android.view.View;

import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.mugenglish.userinterestcontrols.UserInterestRemover;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class UserInterestAdapter
        extends FirebaseRecyclerAdapter<WikiDataEntryData, UserInterestViewHolder> {
    private String userID;

    //Query instead of reference so we can order the data alphabetically
    public UserInterestAdapter(Class<WikiDataEntryData> dataClass, int layoutID,
                               Class<UserInterestViewHolder> viewHolderClass, Query query, String userID){
        super(dataClass, layoutID, viewHolderClass, query);
        this.userID = userID;
    }

    @Override
    public void populateViewHolder(UserInterestViewHolder holder, WikiDataEntryData data, int position) {
        holder.setLabel(data.getLabel());
        holder.setDescription(data.getDescription());
        holder.setWikiDataEntryData(data);
    }


    @Override
    public void onBindViewHolder(UserInterestViewHolder holder, int position){
        super.onBindViewHolder(holder, position);
        final WikiDataEntryData wikiDataEntryData = holder.getWikiDataEntryData();
        holder.setButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInterestRemover.removeUserInterest(wikiDataEntryData, userID);
            }
        });

    }
}
