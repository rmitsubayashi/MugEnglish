package com.linnca.pelicann.gui.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<WikiDataEntryData> results = new ArrayList<>();
    private List<WikiDataEntryData> userInterests;
    private OnAddInterestListener onAddInterestListener;

    public interface OnAddInterestListener {
        void onAddInterest(WikiDataEntryData data);
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
        Button addButton;
    }

    public SearchResultsAdapter(Context context, List<WikiDataEntryData> userInterests, OnAddInterestListener listener){
        onAddInterestListener = listener;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.userInterests = new ArrayList<>(userInterests);
    }

    @Override
    public int getCount(){ return results.size(); }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public WikiDataEntryData getItem(int position){
        return results.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.inflatable_search_interests_result_item, parent, false);

            holder.name = (TextView)convertView.findViewById(R.id.search_interests_result_label);
            holder.description = (TextView)convertView.findViewById(R.id.search_interests_result_description);
            holder.addButton = (Button)convertView.findViewById(R.id.search_interests_result_add_button);

            convertView.setTag(holder);
        } else{
            holder = (ViewHolder)convertView.getTag();
        }

        WikiDataEntryData data = results.get(position);
        holder.name.setText(data.getLabel());
        String description = data.getDescription();
        if (description.equals("")){
            description = "説明なし";
        }
        holder.description.setText(description);


        final WikiDataEntryData fData = data;
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddInterestListener.onAddInterest(fData);
                userInterests.add(fData);
            }
        });

        return convertView;
    }

    public void updateEntries(List<WikiDataEntryData> newList){
        //so we don't keep the same reference
        results = new ArrayList<>(newList);
        //remove all entries that the user already has
        results.removeAll(userInterests);
        notifyDataSetChanged();
    }



}
