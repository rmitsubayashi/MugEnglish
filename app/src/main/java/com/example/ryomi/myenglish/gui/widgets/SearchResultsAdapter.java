package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsAdapter extends BaseAdapter {
    private Context context;
    private List<WikiDataEntryData> results = new ArrayList<>();

    static class ViewHolder {
        TextView name;
        TextView description;
        Button addButton;
    }

    public SearchResultsAdapter(Context context){
        this.context = context;
        //we don't have a populated list of search results
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
            //get device width/height
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            //whole row wrapper
            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(new ListView.LayoutParams(
                    ListView.LayoutParams.MATCH_PARENT,
                    ListView.LayoutParams.WRAP_CONTENT
            ));
            row.setWeightSum(1f);

            //for left side
            LinearLayout text = new LinearLayout(context);
            text.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.8f));
            text.setOrientation(LinearLayout.VERTICAL);
            //same as the padding for the search bar
            int textPaddingHorizontal = (int)(width * 0.1);
            int textPaddingVertical = (int)(height * 0.03);
            text.setPadding(textPaddingHorizontal,textPaddingVertical,
                    textPaddingHorizontal, textPaddingVertical);

            //name
            TextView nameView = new TextView(context);
            //bold text for the name
            nameView.setTypeface(Typeface.DEFAULT_BOLD);

            text.addView(nameView);
            holder.name = nameView;

            //description
            TextView descriptionView = new TextView(context);
            int descriptionPaddingHorizontal = (int)(width * 0.05);
            int descriptionPaddingVertical = (int)(height * 0.01);
            descriptionView.setPadding(descriptionPaddingHorizontal,descriptionPaddingVertical,0,0);
            text.addView(descriptionView);
            holder.description = descriptionView;


            //right side
            Button button = new Button(context);
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT,
                    0.2f
            ));
            button.setText("+");
            button.setTextColor(Color.WHITE);
            //button.setBackgroundColor(Color.BLUE);
            holder.addButton = button;

            //add views
            row.addView(text);
            row.addView(button);

            convertView = row;
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
