package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.View.GONE;

public class SearchResultsAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<WikiDataEntryData> results = new ArrayList<>();
    private Set<String> userInterestIDs;

    static class ViewHolder {
        TextView name;
        TextView description;
        Button addButton;
    }

    public SearchResultsAdapter(Context context, List<WikiDataEntryData> userInterests){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //store IDs to make it easier to match
        Set<String> userInterestIDs = new HashSet<>();
        for (WikiDataEntryData data : userInterests){
            userInterestIDs.add(data.getWikiDataID());
        }
        this.userInterestIDs = userInterestIDs;
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
            convertView = layoutInflater.inflate(R.layout.inflatable_search_interests_result_item, null);

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

        //hide the button if the user interest exists already
        if (userInterestIDs.contains(data.getWikiDataID())){
            holder.addButton.setVisibility(GONE);
        } else {
            holder.addButton.setVisibility(View.VISIBLE);
            final WikiDataEntryData finalData = data;
            holder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        String userID = auth.getCurrentUser().getUid();
                        DatabaseReference ref = db.getReference("userInterests/" + userID + "/" + finalData.getWikiDataID());
                        //just to make sure.
                        //handled when displaying the view
                        if (ref != null) {
                            ref.setValue(finalData);

                            //hide button and add interest to the list of user interests
                            userInterestIDs.add(finalData.getWikiDataID());
                            v.setVisibility(GONE);
                            v.invalidate();
                        }
                    }
                }
            });
        }

        return convertView;
    }

    public void updateEntries(List<WikiDataEntryData> newList){
        results = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

}
