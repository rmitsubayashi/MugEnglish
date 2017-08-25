package com.linnca.pelicann.gui.widgets;


import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;
import com.linnca.pelicann.userinterestcontrols.UserInterestRemover;

public class UserInterestAdapter
        extends FirebaseRecyclerAdapter<WikiDataEntryData, UserInterestViewHolder> {
    private String userID;
    private UserInterestAdapterListener listener;

    public interface UserInterestAdapterListener {
        //should allow undo-ing
        void onItemRemoved(WikiDataEntryData item);
    }

    //Query instead of reference so we can order the data alphabetically
    public UserInterestAdapter(Class<WikiDataEntryData> dataClass, int layoutID,
                               Class<UserInterestViewHolder> viewHolderClass, Query query, String userID,
                               UserInterestAdapterListener listener){
        super(dataClass, layoutID, viewHolderClass, query);
        this.userID = userID;
        this.listener = listener;
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
                listener.onItemRemoved(wikiDataEntryData);
            }
        });

    }
}
