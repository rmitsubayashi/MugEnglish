package com.linnca.pelicann.vocabulary;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.GUIUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class VocabularyListAdapter
    extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    interface VocabularyListAdapterListener {
        void vocabularyListToLessonList();
        //should allow undo-ing
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }

    private VocabularyListAdapterListener listener;
    private List<VocabularyListWord> words = new ArrayList<>();
    private HashSet<Integer> selectedDataPositions = new HashSet<>();

    private final String EMPTY_STATE_ITEM = "empty state";
    private final int headerViewType = 1;
    private final int listItemViewType = 2;

    VocabularyListAdapter(VocabularyListAdapterListener listener){
        super();
        this.listener = listener;
    }

    @Override
    public int getItemCount(){
        return words == null ? 0 : words.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    VocabularyListWord getItemAt(int position){
        return words.get(position);
    }

    @Override
    public int getItemViewType(int position){
        VocabularyListWord word = getItemAt(position);
        if (word.getKey().equals(EMPTY_STATE_ITEM)){
            return headerViewType;
        } else {
            return listItemViewType;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if (viewType == headerViewType){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inflatable_vocabulary_list_empty_state, parent, false);
            VocabularyListEmptyStateViewHolder holder = new VocabularyListEmptyStateViewHolder(itemView);
            holder.setListener(listener);
            return holder;
        } else if (viewType == listItemViewType){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inflatable_vocabulary_list_item, parent, false);
            final VocabularyListViewHolder holder = new VocabularyListViewHolder(itemView);
            //what happens when we click/long click is the same for every item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClicked(holder.getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
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
        final VocabularyListWord data = words.get(position);
        if (holder instanceof VocabularyListViewHolder) {
            boolean isSelected = isSelected(position);
            if (isSelected) {
                holder.itemView.setBackgroundResource(R.drawable.gray_button);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.transparent_button);
            }

            ((VocabularyListViewHolder)holder).setWord(data.getWord());
            ((VocabularyListViewHolder)holder).setMeaning(data.getMeanings());
        }
        //nothing to do if it's an empty state

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

    List<VocabularyListWord> getSelectedItems(){
        List<VocabularyListWord> copyList = new ArrayList<>(selectedDataPositions.size());
        for (Integer selectedItemPosition : selectedDataPositions){
            VocabularyListWord selectedItem = words.get(selectedItemPosition);
            VocabularyListWord copy = new VocabularyListWord(selectedItem.getWord(), selectedItem.getMeanings(), selectedItem.getKey());
            copyList.add(copy);
        }
        return copyList;
    }

    void clearSelection(){
        List<Integer> selectedDataPositionsCopy = new ArrayList<>(selectedDataPositions);
        selectedDataPositions.clear();
        for (Integer i : selectedDataPositionsCopy){
            notifyItemChanged(i);
        }
    }

    void setVocabularyWords(List<VocabularyListWord> updatedList){
        List<VocabularyListWord> oldList = new ArrayList<>(words);
        //empty state
        if (updatedList.size() == 0){
            notifyItemRangeRemoved(0, oldList.size());
            VocabularyListWord emptyState = new VocabularyListWord();
            emptyState.setKey(EMPTY_STATE_ITEM);
            updatedList.add(emptyState);
            words = new ArrayList<>(updatedList);
            notifyDataSetChanged();
            return;
        }
        //check if we came from an empty state
        if (oldList.size() == 1 &&
                oldList.get(0).getKey().equals(EMPTY_STATE_ITEM)){
            oldList.remove(0);
            notifyItemRemoved(0);
        }

        words = new ArrayList<>(updatedList);
        if (oldList.size() > updatedList.size()){
            List<Integer> toRemove = GUIUtils.getItemIndexesToRemove(oldList, updatedList);
            for (Integer index : toRemove){
                notifyItemRemoved(index);
            }
        } else if (oldList.size() < updatedList.size()){
            List<Integer> toAdd = GUIUtils.getItemIndexesToAdd(oldList, updatedList);
            for (Integer index : toAdd){
                notifyItemInserted(index);
            }
        } //list size shouldn't be equal since we can only add or remove items
    }
}
