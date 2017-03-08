package com.example.ryomi.mugenglish.userinterestcontrols;


import android.os.AsyncTask;

import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//search for relevant pronunciation and then add.
//we are sorting by pronunciation now, but later we might classify more generally like
//people, places, etc.
public class UserInterestAdder extends AsyncTask<WikiDataEntryData, Integer, String> {
    private WikiDataEntryData dataToAdd;
    private  PronunciationSearcher pronunciationSearcher = new PronunciationSearcher();

    @Override
    protected String doInBackground(WikiDataEntryData... dataList){
        dataToAdd = dataList[0];
        String pronunciation;
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
