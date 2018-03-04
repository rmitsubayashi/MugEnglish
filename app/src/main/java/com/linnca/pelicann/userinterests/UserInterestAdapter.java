package com.linnca.pelicann.userinterests;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.GUIUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

class UserInterestAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final UserInterestAdapterListener listener;
    private HashSet<Integer> selectedDataPositions = new HashSet<>();
    private List<WikiDataEntity> userInterests = new ArrayList<>();
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
        return userInterests.size();
    }

    @Override
    public long getItemId(int position){
        return userInterests.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position){
        WikiDataEntity item = userInterests.get(position);
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
            final WikiDataEntity data = userInterests.get(position);
            ((UserInterestViewHolder)holder).setLabel(data.getLabel());
            ((UserInterestViewHolder)holder).setDescription(data.getDescription());
            boolean isSelected = isSelected(position);
            ((UserInterestViewHolder)holder).setWikiDataEntity(data);
            ((UserInterestViewHolder) holder).setSelected(isSelected);
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
            WikiDataEntity selectedItem = userInterests.get(selectedItemPosition);
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
            userInterests.clear();
            userInterests.add(emptyState);
            notifyDataSetChanged();
            return;
        }
        //just update if the displayed list is empty
        //(can be the initial call or just if there is nothing on
        // the screen now)
        if (userInterests.size() == 1 &&
                (userInterests.get(0).getWikiDataID().equals(EMPTY_STATE_TAG) ||
                userInterests.get(0).getWikiDataID().equals(NO_NETWORK_TAG) )
                ){
            userInterests.clear();
            userInterests.addAll(updatedList);
            notifyDataSetChanged();
            return;
        }

        //if there is something on the screen,
        // the list of user interests have changed
        // because the user removed / undo-ed a remove operation.
        // so animate the inserted/removed items.
        //save the previous list so we can animate
        List<WikiDataEntity> originalList = new ArrayList<>(
                userInterests);
        //updating the user interests takes care of filtering as well
        userInterests.clear();
        userInterests.addAll(updatedList);

        int prevListSize = originalList.size();
        int updatedListSize = userInterests.size();

        //we should remove
        if (prevListSize > updatedListSize){
            removeItemsAnimation(originalList, userInterests);
            return;
        }

        //we should add
        if (prevListSize < updatedListSize){
            addItemsAnimation(originalList, userInterests);
            return;
        }
        //this shouldn't happen because we are either
        // removing or adding, not both
        if (prevListSize == updatedListSize){
            notifyDataSetChanged();
        }

    }

    void setOffline(){
        WikiDataEntity noNetwork = new WikiDataEntity();
        noNetwork.setWikiDataID(NO_NETWORK_TAG);
        userInterests.clear();
        userInterests.add(noNetwork);
        notifyDataSetChanged();
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
