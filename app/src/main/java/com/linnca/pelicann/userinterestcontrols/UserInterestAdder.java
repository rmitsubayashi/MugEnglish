package com.linnca.pelicann.userinterestcontrols;

import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;

//search for relevant pronunciation and then add.
//we are sorting by pronunciation now, but later we might classify more generally like
//people, places, etc.
public class UserInterestAdder {
    private WikiDataEntryData dataToAdd;
    private String pronunciation;
    private  PronunciationSearcher pronunciationSearcher = new PronunciationSearcher();

    public void findPronunciationAndAdd(WikiDataEntryData dataToAdd){
        this.dataToAdd = dataToAdd;
        PronunciationSearchThread thread = new PronunciationSearchThread();
        thread.start();
    }

    //we don't need to find the pronunciation.
    //for example recommended entries, undo entries
    //already have the right pronunciations
    public void justAdd(WikiDataEntryData dataToAdd){
        this.dataToAdd = dataToAdd;
        saveUserInterest();
    }

    private class PronunciationSearchThread extends Thread {
        @Override
        public void run(){
            try {
                pronunciation = pronunciationSearcher.getPronunciationFromWikiBase(dataToAdd.getWikiDataID());
            } catch (Exception e){
                e.printStackTrace();
                pronunciation =  pronunciationSearcher.zenkakuKatakanaToZenkakuHiragana(dataToAdd.getLabel());
            }

            if (pronunciationSearcher.containsKanji(pronunciation)){
                try {
                    pronunciation = pronunciationSearcher.getPronunciationFromMecap(pronunciation);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            saveUserInterest();
        }
    }



    private void saveUserInterest(){
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
