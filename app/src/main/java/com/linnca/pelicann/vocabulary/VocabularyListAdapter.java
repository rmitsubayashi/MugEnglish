package com.linnca.pelicann.vocabulary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class VocabularyListAdapter
    extends RecyclerView.Adapter<VocabularyListViewHolder>
{
    interface VocabularyListAdapterListener {
        //should allow undo-ing
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }

    private VocabularyListAdapterListener listener;
    private List<VocabularyListWord> words;
    private HashSet<Integer> selectedDataPositions = new HashSet<>();

    VocabularyListAdapter(VocabularyListAdapterListener listener, List<VocabularyListWord> words){
        super();
        this.listener = listener;
        this.words = words;
    }

    @Override
    public int getItemCount(){
        return words == null ? 0 : words.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public VocabularyListWord getItemAt(int position){
        return words.get(position);
    }

    @Override
    public VocabularyListViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inflatable_vocabulary_list_item, parent, false);
        return new VocabularyListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final VocabularyListViewHolder holder, int position){
        final VocabularyListWord data = words.get(position);
        boolean isSelected = isSelected(position);

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

        holder.setWord(data.getWord());
        holder.setMeaning(data.getMeanings());

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
        words = new ArrayList<>(updatedList);
        notifyDataSetChanged();
    }
}
