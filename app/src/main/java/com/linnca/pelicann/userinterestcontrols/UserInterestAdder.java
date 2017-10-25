package com.linnca.pelicann.userinterestcontrols;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Collection;

//search for relevant pronunciation and then add.
//we are sorting by pronunciation now, but later we might classify more generally like
//people, places, etc.
public class UserInterestAdder {
    private WikiDataEntryData dataToAdd;
    private final PronunciationSearcher pronunciationSearcher = new PronunciationSearcher();
    private final WikiBaseEndpointConnector wikiBaseEndpointConnector = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);

    public void findPronunciationAndCategoryThenAdd(WikiDataEntryData dataToAdd){
        this.dataToAdd = dataToAdd;
        PronunciationCategorySearchThread thread = new PronunciationCategorySearchThread();
        thread.start();
    }

    public void findPronunciationAndCategoryThenAdd(Collection<WikiDataEntryData> collection){
        for (WikiDataEntryData data : collection){
            findPronunciationAndCategoryThenAdd(data);
        }
    }

    //we don't need to find the pronunciation.
    //for example recommended entries, undo entries
    //already have the right pronunciations
    public void justAdd(WikiDataEntryData dataToAdd){
        this.dataToAdd = dataToAdd;
        saveUserInterest();
    }

    public void justAdd(Collection<WikiDataEntryData> collection){
        for (WikiDataEntryData data : collection){
            justAdd(data);
        }
    }

    private class PronunciationCategorySearchThread extends Thread {
        @Override
        public void run(){
            if (dataToAdd == null)
                return;
            String pronunciation;
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
            dataToAdd.setPronunciation(pronunciation);

            boolean classificationSet = false;
            try {
                Document resultDOM = wikiBaseEndpointConnector.fetchDOMFromGetRequest(getPersonSearchQuery(dataToAdd.getWikiDataID()));
                NodeList allResults = resultDOM.getElementsByTagName(
                        WikiDataSPARQLConnector.RESULT_TAG
                );
                int resultLength = allResults.getLength();
                if (resultLength > 0){
                    dataToAdd.setClassification(WikiDataEntryData.CLASSIFICATION_PERSON);
                    classificationSet = true;
                }

                if (!classificationSet){
                    resultDOM = wikiBaseEndpointConnector.fetchDOMFromGetRequest(getPlaceSearchQuery(dataToAdd.getWikiDataID()));
                    allResults = resultDOM.getElementsByTagName(
                            WikiDataSPARQLConnector.RESULT_TAG
                    );
                    resultLength = allResults.getLength();
                    if (resultLength > 0){
                        dataToAdd.setClassification(WikiDataEntryData.CLASSIFICATION_PLACE);
                        classificationSet = true;
                    }
                }
                if (!classificationSet){
                    dataToAdd.setClassification(WikiDataEntryData.CLASSIFICATION_OTHER);
                }
            } catch (Exception e){
                e.printStackTrace();
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
                            connectRecommendationEdge(childData, dataToAdd);
                            connectRecommendationEdge(dataToAdd, childData);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void connectRecommendationEdge(final WikiDataEntryData fromInterest, final WikiDataEntryData toInterest){
        //we have two references.
        //one is for recommending interests to users.
        //the other is for searching related interests for lesson generation.
        //(we need to query by category type and count for lesson generation).
        //consistency between the two maps isn't of too much importance so
        //update them separately
        DatabaseReference edgeRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                        fromInterest.getWikiDataID() + "/" +
                        toInterest.getWikiDataID() + "/" +
                        FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT

        );
        edgeRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long edgeWeight = mutableData.getValue(Long.class);
                //first edge
                if (edgeWeight == null){
                    mutableData.setValue(1);
                    FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                                    fromInterest.getWikiDataID() + "/" +
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

        DatabaseReference edge2Ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP_FOR_LESSON_GENERATION + "/" +
                        fromInterest.getWikiDataID() + "/" +
                        Integer.toString(toInterest.getClassification()) + "/" +
                        toInterest.getWikiDataID() + "/" +
                        FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT

        );
        edge2Ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long edgeWeight = mutableData.getValue(Long.class);
                //first edge
                if (edgeWeight == null){
                    mutableData.setValue(1);
                    FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.RECOMMENDATION_MAP_FOR_LESSON_GENERATION + "/" +
                                    fromInterest.getWikiDataID() + "/" +
                                    Integer.toString(toInterest.getClassification()) + "/" +
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

    private String getPersonSearchQuery(String wikidataID){
        return "SELECT ?person " +
                "WHERE " +
                "{" +
                "  {?person wdt:P31 wd:Q5} " +
                "  UNION {?person wdt:P31/wdt:P279* wd:Q15632617} " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en','ja'. } . " +
                "  BIND (wd:" + wikidataID + " as ?person)" +
                "}";
    }

    private String getPlaceSearchQuery(String wikidataID){
        return "SELECT DISTINCT ?place " +
                "WHERE " +
                "{" +
                "  {?place wdt:P31/wdt:P279* wd:Q2221906} " +
                "  UNION {?place wdt:P31/wdt:P279* wd:Q3895768} . " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en,ja' . } " +
                "  BIND (wd:" + wikidataID + " as ?place) " +
                "}";
    }
}
