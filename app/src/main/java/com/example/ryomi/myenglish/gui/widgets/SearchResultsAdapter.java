package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflator;
    private List<WikiDataEntryData> results = new ArrayList<>();

    static class ViewHolder {
        TextView name;
        TextView description;
        Button addButton;
    }

    public SearchResultsAdapter(Context context){
        this.context = context;
        layoutInflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            convertView = layoutInflator.inflate(R.layout.inflatable_search_interests_result_item, null);

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

        final WikiDataEntryData finalData = data;
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    String userID = auth.getCurrentUser().getUid();
                    DatabaseReference ref = db.getReference("userInterests/"+userID+"/"+finalData.getWikiDataID());
                    if (ref != null){
                        ref.setValue(finalData);
                    }
                }
            }
        });

        return convertView;
    }

    public void updateEntries(List<WikiDataEntryData> newList){
        results = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

}
