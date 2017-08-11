package com.example.ryomi.mugenglish.userinterestcontrols;


import android.os.AsyncTask;

import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

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
            DatabaseReference userInterestRef = db.getReference(
                    FirebaseDBHeaders.USER_INTERESTS + "/" +
                            userID + "/" +
                            dataToAdd.getWikiDataID());
            dataToAdd.setPronunciation(pronunciation);
            userInterestRef.setValue(dataToAdd);

            //also add this to the recommendation map
            DatabaseReference allUserInterestsRef = db.getReference(
                    FirebaseDBHeaders.USER_INTERESTS + "/" +
                            userID
            );
            allUserInterestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        WikiDataEntryData childData = child.getValue(WikiDataEntryData.class);
                        //we don't want to add a recommendation path to the same interest
                        if (!childData.equals(dataToAdd)){
                            connectRecommendationEdge(childData.getWikiDataID(), dataToAdd);
                            connectRecommendationEdge(dataToAdd.getWikiDataID(), childData);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void connectRecommendationEdge(final String fromInterestID, final WikiDataEntryData toInterest){
        DatabaseReference edgeRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                        fromInterestID + "/" +
                        toInterest.getWikiDataID() + "/" +
                        FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT

        );
        edgeRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long edgeWeight = mutableData.getValue(Long.class);
                if (edgeWeight == null){
                    mutableData.setValue(1);
                    FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                                    fromInterestID + "/" +
                                    toInterest.getWikiDataID() + "/" +
                                    FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_DATA
                    ).setValue(toInterest);
                } else {
                    mutableData.setValue(edgeWeight + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }
}
