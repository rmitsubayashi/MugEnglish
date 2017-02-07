package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.userinterestcontrols.PronunciationSearcher;
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
    private PronunciationSearcher pronunciationSearcher = new PronunciationSearcher();
    private List<WikiDataEntryData> results = new ArrayList<>();
    private Set<String> userInterestIDs;

    private static class ViewHolder {
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
        //this might be a disabled check button
        holder.addButton.setText(R.string.search_interests_add);
        holder.addButton.setEnabled(true);

        //hide the button if the user interest exists already
        if (userInterestIDs.contains(data.getWikiDataID())){
            holder.addButton.setVisibility(GONE);
        } else {
            holder.addButton.setVisibility(View.VISIBLE);
            final WikiDataEntryData fData = data;
            holder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addInterest(fData, v);
                }
            });
        }

        return convertView;
    }

    public void updateEntries(List<WikiDataEntryData> newList){
        results = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    private void addInterest(WikiDataEntryData dataToAdd, View viewToDisable){
        //disable button and add interest to the list of user interests
        userInterestIDs.add(dataToAdd.getWikiDataID());
        viewToDisable.setEnabled(false);
        //check mark
        ((Button)viewToDisable).setText("\u2713");
        //the button is disabled now but if the user searches again
        //it should be gone
        viewToDisable.invalidate();

        //disable button first for better ux (less lag).
        //then search for pronunciation
        //and add the wikiData entry
        try {
            PronunciationSearcherThread conn = new PronunciationSearcherThread();
            conn.execute(dataToAdd);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private class PronunciationSearcherThread extends AsyncTask<WikiDataEntryData, Integer, String>{
        private WikiDataEntryData dataToAdd;

        @Override
        protected String doInBackground(WikiDataEntryData... dataList){
            dataToAdd = dataList[0];
            String pronunciation = "";
            try {
                pronunciation = pronunciationSearcher.getPronunciationFromWikiBase(dataToAdd.getWikiDataID());
            } catch (Exception e){
                e.printStackTrace();
                pronunciation =  pronunciationSearcher.zenkakuKatakanaToZenkakuHiragana(dataToAdd.getLabel());
            }

            if (pronunciationSearcher.containsKanji(pronunciation)){
                try {
                    return pronunciationSearcher.getPronunciationFromMecap(pronunciation);
                } catch (Exception e){
                    e.printStackTrace();
                    return pronunciation;
                }
            } else {
                return pronunciation;
            }
        }

        @Override
        protected void onPostExecute(String pronunciation){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                String userID = auth.getCurrentUser().getUid();
                DatabaseReference ref = db.getReference("userInterests/" + userID + "/" + dataToAdd.getWikiDataID());
                //just to make sure.
                //handled when displaying the view
                if (ref != null) {
                    dataToAdd.setPronunciation(pronunciation);
                    ref.setValue(dataToAdd);
                }
            }
        }
    }

}
