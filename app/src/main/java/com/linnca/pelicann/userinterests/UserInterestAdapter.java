package com.linnca.pelicann.userinterests;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ToolbarSpinnerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class UserInterestAdapter
        extends RecyclerView.Adapter<UserInterestViewHolder>
{
    private final UserInterestAdapterListener listener;
    private final UserInterestFilter userInterestFilter = new UserInterestFilter();
    private HashSet<Integer> selectedDataPositions = new HashSet<>();

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
    public UserInterestViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inflatable_user_interests_list_item, parent, false);
        return new UserInterestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final UserInterestViewHolder holder, int position){
        final WikiDataEntity data = userInterestFilter.get(position);
        holder.setLabel(data.getLabel());
        holder.setDescription(data.getDescription());
        boolean isSelected = isSelected(position);
        holder.setIcon(data.getClassification(), isSelected);
        holder.setWikiDataEntity(data);

        if (isSelected){
            holder.itemView.setBackgroundResource(R.drawable.gray_button);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.transparent_button);
        }

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

    private void removeItemsAnimation(List<WikiDataEntity> oldList, List<WikiDataEntity> newList){
        //make the list a set to make it easier to search
        Set<WikiDataEntity> newSet = new HashSet<>(newList);
        int itemsRemoved = 0;
        for (int i=0; i<oldList.size(); i++){
            WikiDataEntity oldItem = oldList.get(i);
            if (!newSet.contains(oldItem)){
                notifyItemRemoved(i-itemsRemoved);
                itemsRemoved++;
            }
        }
    }

    private void addItemsAnimation(List<WikiDataEntity> oldList, List<WikiDataEntity> newList){
        //make the list a set to make it easier to search
        Set<WikiDataEntity> oldSet = new HashSet<>(oldList);
        int tempToAdd = 0;
        int insertItemStartIndex = 0;
        for (int i=0; i<newList.size(); i++){
            WikiDataEntity newData = newList.get(i);
            if (!oldSet.contains(newData)){
                //in case we are adding multiple items in a row,
                // don't notify as soon as we find an item to add,
                // but keep track and add it when we find an item
                // that we shouldn't add
                tempToAdd++;
            } else if (tempToAdd > 0){
                notifyItemRangeInserted(insertItemStartIndex, tempToAdd);
                insertItemStartIndex += tempToAdd;
                tempToAdd = 0;
                //if the item is not to be added, we should increment
                insertItemStartIndex++;
            } else {
                //if the item is not to be added, we should increment
                insertItemStartIndex++;
            }
        }
        //to handle when the last item should be added
        if (tempToAdd > 0){
            notifyItemRangeInserted(insertItemStartIndex, tempToAdd);
        }
    }
}
