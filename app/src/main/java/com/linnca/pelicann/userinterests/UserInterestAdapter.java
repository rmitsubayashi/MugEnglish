package com.linnca.pelicann.userinterests;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.GUIUtils;
import com.linnca.pelicann.mainactivity.ToolbarSpinnerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

class UserInterestAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final UserInterestAdapterListener listener;
    private final UserInterestFilter userInterestFilter = new UserInterestFilter();
    private HashSet<Integer> selectedDataPositions = new HashSet<>();

    static final String EMPTY_STATE_TAG = "empty state";
    static final String NO_NETWORK_TAG = "no network";
    private final int emptyStateViewType = 1;
    private final int userInterestItemViewType = 2;
    private final int noNetworkViewType = 3;

    interface UserInterestAdapterListener {
        //should allow undo-ing
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }

    //Query instead of reference so we can order the data alphabetically
    UserInterestAdapter(UserInterestAdapterListener listener){
        super();
        this.listener = listener;
    }

    @Override
    public int getItemCount(){
        return userInterestFilter.size();
    }

    @Override
    public long getItemId(int position){
        return userInterestFilter.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position){
        WikiDataEntity item = userInterestFilter.get(position);
        switch (item.getWikiDataID()){
            case EMPTY_STATE_TAG :
                return emptyStateViewType;
            case NO_NETWORK_TAG :
                return noNetworkViewType;
            default:
                return userInterestItemViewType;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if (viewType == emptyStateViewType){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inflatable_user_interest_empty_state, parent, false);
            return new RecyclerView.ViewHolder(itemView){};
        } else if (viewType == noNetworkViewType){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inflatable_user_interests_offline, parent, false);
            return new RecyclerView.ViewHolder(itemView){};
        } else if (viewType == userInterestItemViewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inflatable_user_interests_list_item, parent, false);

            final UserInterestViewHolder holder = new UserInterestViewHolder(itemView);
            //since these aren't dependant on the item,
            //set the click and long click listeners here
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClicked(holder.getAdapterPosition());
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (listener != null) {
                        return listener.onItemLongClicked(holder.getAdapterPosition());
                    }
                    return false;
                }
            });
            return holder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
        if (holder instanceof UserInterestViewHolder) {
            final WikiDataEntity data = userInterestFilter.get(position);
            ((UserInterestViewHolder)holder).setLabel(data.getLabel());
            ((UserInterestViewHolder)holder).setDescription(data.getDescription());
            boolean isSelected = isSelected(position);
            ((UserInterestViewHolder)holder).setIcon(data.getClassification(), isSelected);
            ((UserInterestViewHolder)holder).setWikiDataEntity(data);

            if (isSelected) {
                holder.itemView.setBackgroundResource(R.drawable.gray_button);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.transparent_button);
            }
        }

    }

    private boolean isSelected(int position){
        return selectedDataPositions.contains(position);
    }

    int getSelectedItemCount(){
        return selectedDataPositions.size();
    }

    void toggleSelection(int position){
        if (selectedDataPositions.contains(position)){
            selectedDataPositions.remove(position);
        } else {
            selectedDataPositions.add(position);
        }
        //update selected/unselected item
        notifyItemChanged(position);
    }

    List<WikiDataEntity> getSelectedItems(){
        List<WikiDataEntity> copyList = new ArrayList<>(selectedDataPositions.size());
        for (Integer selectedItemPosition : selectedDataPositions){
            WikiDataEntity selectedItem = userInterestFilter.get(selectedItemPosition);
            WikiDataEntity copy = new WikiDataEntity(selectedItem);
            copyList.add(copy);
        }
        return copyList;
    }

    void clearSelection(){
        List<Integer> selectedDataPositionsCopy = new ArrayList<>(selectedDataPositions);
        selectedDataPositions.clear();
        for (Integer i : selectedDataPositionsCopy){
            //if we don't clear the list before notifying the item,
            //nothing will change because we reference the list
            //to see whether the item is selected or not
            notifyItemChanged(i);
        }
    }

    void setInterests(List<WikiDataEntity> updatedList){
        //empty state
        if (updatedList.size() == 0){
            WikiDataEntity emptyState = new WikiDataEntity();
            emptyState.setWikiDataID(EMPTY_STATE_TAG);
            updatedList.add(emptyState);
            userInterestFilter.setUserInterests(updatedList);
            notifyDataSetChanged();
            return;
        }
        //just update if the displayed list is empty
        //(can be the initial call or just if there is nothing on
        // the screen now)
        if (userInterestFilter.size() == 0){
            userInterestFilter.setUserInterests(updatedList);
            notifyDataSetChanged();
            return;
        }

        //if there is something on the screen,
        // the list of user interests have changed (!not filtered!)
        // so animate the inserted/removed items.
        //save the previous list so we can animate
        List<WikiDataEntity> originalList = new ArrayList<>(
                userInterestFilter.getFilteredList());
        //updating the user interests takes care of filtering as well
        userInterestFilter.setUserInterests(updatedList);

        int prevListSize = originalList.size();
        int updatedListSize = userInterestFilter.size();

        //we should remove
        if (prevListSize > updatedListSize){
            removeItemsAnimation(originalList, userInterestFilter.getFilteredList());
            return;
        }

        //we should add
        if (prevListSize < updatedListSize){
            addItemsAnimation(originalList, userInterestFilter.getFilteredList());
            return;
        }
        //this shouldn't happen because we are either
        // removing or adding, not both
        if (prevListSize == updatedListSize){
            notifyDataSetChanged();
        }

    }

    public void setFilter(int newFilter){
        //just making sure we have the right constant
        if (!ToolbarSpinnerAdapter.isSpinnerState(newFilter))
            return;

        //get the current state so we can
        //animate the changes
        int oldFilter = userInterestFilter.getFilter();
        List<WikiDataEntity> oldFilteredList = new ArrayList<>(
                userInterestFilter.getFilteredList());
        userInterestFilter.setFilter(newFilter);
        //should animate filtered list,
        //but only when it goes from all to something else or vice versa
        // since the rest is mutually exclusive??
        //i.e. if we go from people to places, since there will be no items in common,
        // animating will just remove all items and then add the new ones

        //we want to remove items
        if (oldFilter == ToolbarSpinnerAdapter.FILTER_ALL){
            removeItemsAnimation(oldFilteredList, userInterestFilter.getFilteredList());
            return;
        }
        //we want to add items
        if (newFilter == ToolbarSpinnerAdapter.FILTER_ALL){
            addItemsAnimation(oldFilteredList, userInterestFilter.getFilteredList());
            return;
        }

        //the rest
        notifyDataSetChanged();

    }

    void setOffline(){
        List<WikiDataEntity> noNetworkList = new ArrayList<>(1);
        WikiDataEntity noNetwork = new WikiDataEntity();
        noNetwork.setWikiDataID(NO_NETWORK_TAG);
        noNetworkList.add(noNetwork);
        setInterests(noNetworkList);
    }

    private void removeItemsAnimation(List<WikiDataEntity> oldList, List<WikiDataEntity> newList){
        List<Integer> toRemove = GUIUtils.getItemIndexesToRemove(oldList, newList);
        for (Integer index : toRemove){
            notifyItemRemoved(index);
        }
    }

    private void addItemsAnimation(List<WikiDataEntity> oldList, List<WikiDataEntity> newList){
        //remove the empty state if it's there first
        if (oldList.size() == 1 &&
                (oldList.get(0).getWikiDataID().equals(EMPTY_STATE_TAG) ||
                        oldList.get(0).getWikiDataID().equals(NO_NETWORK_TAG))
                ){
            oldList.remove(0);
            notifyItemRemoved(0);
        }
        List<Integer> toAdd = GUIUtils.getItemIndexesToAdd(oldList, newList);
        for (Integer index : toAdd){
            notifyItemInserted(index);
        }
    }
}
