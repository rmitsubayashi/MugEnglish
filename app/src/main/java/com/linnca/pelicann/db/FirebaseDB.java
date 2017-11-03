package com.linnca.pelicann.db;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.results.NewVocabularyWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.userprofile.UserProfile_ReportCardDataWrapper;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseDB extends Database {
    //Firebase requires both the reference and the listener attached to it
    // to remove the listener.
    private class EventListenerPair {
        private Query databaseReference;
        private ValueEventListener valueEventListener;

        private EventListenerPair(Query databaseReference, ValueEventListener valueEventListener) {
            this.databaseReference = databaseReference;
            this.valueEventListener = valueEventListener;
        }
    }
    private List<EventListenerPair> pairs = new ArrayList<>();

    @Override
    public String getUserID(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void cleanup(){
        //Firebase keeps connections open so it can update changes in real time.
        //when we do not need the data anymore,
        //we should remove the connections
        for (EventListenerPair pair : pairs){
            pair.databaseReference.removeEventListener(pair.valueEventListener);
        }
    }

    @Override
    public void getVocabularyDetails(String vocabularyItemID, final OnResultListener onResultListener){
        DatabaseReference detailsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_DETAILS + "/" +
                        getUserID() + "/" +
                        vocabularyItemID
        );
        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VocabularyWord> allClusters = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    VocabularyWord word = childSnapshot.getValue(VocabularyWord.class);
                    allClusters.add(word);
                }
                onResultListener.onVocabularyWordQueried(allClusters);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void getVocabularyList(final OnResultListener onResultListener){
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                        getUserID()
        );

        ValueEventListener vocabularyEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VocabularyListWord> words = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    VocabularyListWord word = childSnapshot.getValue(VocabularyListWord.class);
                    words.add(word);
                }
                onResultListener.onVocabularyListQueried(words);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        vocabularyRef.addValueEventListener(vocabularyEventListener);
        pairs.add(new EventListenerPair(vocabularyRef, vocabularyEventListener));
    }

    @Override
    public void addVocabularyWord(final VocabularyWord word, final OnResultListener onResultListener){
        //for displaying a list of words
        DatabaseReference listRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                        getUserID()
        );
        Query listQuery = listRef.orderByChild(FirebaseDBHeaders.VOCABULARY_LIST_WORD_WORD)
                .equalTo(word.getWord()).limitToFirst(1);
        listQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key;
                if (!dataSnapshot.exists()){
                    //this is a new word
                    DatabaseReference newItemRef = FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                                    getUserID()
                    );
                    key = newItemRef.push().getKey();
                    VocabularyListWord toSave = new VocabularyListWord(word, key);
                    newItemRef.child(key).setValue(toSave);
                    addVocabularyWordPt2(key, word, onResultListener);
                    return;
                }
                //only loops once
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    VocabularyListWord listWord = childSnapshot.getValue(VocabularyListWord.class);
                    if (childSnapshot.exists() && listWord != null) {
                        listWord.addMeaning(word.getMeaning());
                        key = childSnapshot.getKey();
                        FirebaseDatabase.getInstance().getReference(
                                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                                        getUserID() + "/" +
                                        key
                        ).setValue(listWord);
                    } else {
                        //this is a new meaning
                        DatabaseReference newItemRef = FirebaseDatabase.getInstance().getReference(
                                FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                                        getUserID()
                        );
                        key = newItemRef.push().getKey();
                        VocabularyListWord toSave = new VocabularyListWord(word, key);
                        newItemRef.child(key).setValue(toSave);
                    }

                    addVocabularyWordPt2(key, word, onResultListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //we are going to add the whole word to the details list,
    //using the key used/created when adding the item to the list before this
    private void addVocabularyWordPt2(String key, VocabularyWord word, final OnResultListener onResultListener){
        DatabaseReference detailsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_DETAILS + "/" +
                        getUserID() + "/" +
                        key
        );
        detailsRef.push().setValue(word).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onResultListener.onVocabularyWordAdded();
            }
        });
    }

    @Override
    public void removeVocabularyListItems(List<String> vocabularyListItemKeys, final OnResultListener onResultListener){
        //we have two locations where we store vocabulary words.
        //one is the list node which only contains info needed to show a list of vocabulary items.
        //the other is the details node which contains more information on each vocabulary item.
        //we have to remove both.

        //remove all items in one call
        HashMap<String, Object> allReferences = new HashMap<>(vocabularyListItemKeys.size());
        for (String key : vocabularyListItemKeys){
            String toRemoveDetailsRef = FirebaseDBHeaders.VOCABULARY_DETAILS + "/" +
                    getUserID() + "/" +
                    key;
            //setting the value to null is the same as removing it
            allReferences.put(toRemoveDetailsRef, null);

            String toRemoveListRef = FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                    getUserID() + "/" +
                    key;
            allReferences.put(toRemoveListRef, null);
        }
        FirebaseDatabase.getInstance().getReference().updateChildren(allReferences).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onResultListener.onVocabularyListItemsRemoved();
                    }
                }
        );

    }

    @Override
    public void getLessonVocabulary(String lessonInstanceKey, final OnResultListener onResultListener){
        //first get all vocabulary for the lesson instance.
        //second, clump all duplicate vocabulary (i.e. dynamic sentences w/ the same word)
        //third, check if the user already has the word added to his list.
        //last, show the list to the user
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_INSTANCE_VOCABULARY + "/" +
                        lessonInstanceKey
        );

        vocabularyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> type =
                        new GenericTypeIndicator<List<String>>() {};
                List<String> vocabularyIDs = dataSnapshot.getValue(type);
                //the lesson has no new vocabulary words
                if (vocabularyIDs == null) {
                    onResultListener.onLessonVocabularyQueried(null);
                    return;
                }
                int vocabularyCt = vocabularyIDs.size();
                AtomicInteger vocabularyFetched = new AtomicInteger(0);
                List<VocabularyWord> words = Collections.synchronizedList(new ArrayList<VocabularyWord>(vocabularyCt));
                for (String id : vocabularyIDs){
                    //we only have the IDs so we need to get all information for these words
                    fetchVocabularyWord(id, vocabularyCt, vocabularyFetched, words, onResultListener);
                    //after we'v gotten all the word info,
                    //proceed to filtering all the words the user has added already
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchVocabularyWord(String id, final int vocabularyToFetch, final AtomicInteger vocabularyFetched, final List<VocabularyWord> allWords, final OnResultListener onResultListener){
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY + "/" +
                        id
        );
        vocabularyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VocabularyWord word = dataSnapshot.getValue(VocabularyWord.class);
                allWords.add(word);

                if (vocabularyFetched.incrementAndGet() == vocabularyToFetch){
                    clusterDuplicateVocabularyItems(allWords, onResultListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class VocabularyComparator implements Comparator<VocabularyWord> {
        public int compare(VocabularyWord word1, VocabularyWord word2){
            return word1.getWord().toLowerCase().compareTo(word2.getWord().toLowerCase());
        }
    }

    private void clusterDuplicateVocabularyItems(List<VocabularyWord> allWords, OnResultListener onResultListener){
        //cluster duplicates (same words but different example sentences)
        List<List<VocabularyWord>> filteredWords = new ArrayList<>(allWords.size());
        //sort alphabetically
        Collections.sort(allWords, new VocabularyComparator());
        for (VocabularyWord word : allWords){
            if (filteredWords.size() == 0){
                //first item
                List<VocabularyWord> cluster = new ArrayList<>(allWords.size());
                cluster.add(word);
                filteredWords.add(cluster);
                continue;
            }
            boolean matched = false;
            for (List<VocabularyWord> toCompareCluster : filteredWords){
                VocabularyWord toCompare = toCompareCluster.get(0);
                if (word.getWord().equals(toCompare.getWord()) &&
                        word.getMeaning().equals(toCompare.getMeaning())){
                    toCompareCluster.add(word);
                    matched = true;
                    break;
                }
            }
            if (!matched){
                //new word
                List<VocabularyWord> cluster = new ArrayList<>(allWords.size());
                cluster.add(word);
                filteredWords.add(cluster);
            }
        }

        checkIfAlreadyAdded(filteredWords, onResultListener);
    }

    private void checkIfAlreadyAdded(List<List<VocabularyWord>> list, final OnResultListener onResultListener){
        final AtomicInteger vocabularyCheckedCt = new AtomicInteger(0);
        final int vocabularyToCheckCt = list.size();
        final List<NewVocabularyWrapper> checkedVocabulary = Collections.synchronizedList(new ArrayList<NewVocabularyWrapper>());
        final Random random = new Random();
        for (final List<VocabularyWord> words : list){
            //get a random example sentence
            int index = random.nextInt(words.size());
            final VocabularyWord wordToAdd = words.get(index);
            DatabaseReference wordRef = FirebaseDatabase.getInstance().getReference(
                    FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                            getUserID()
            );
            Query wordQuery = wordRef.orderByChild(FirebaseDBHeaders.VOCABULARY_LIST_WORD_WORD)
                    .equalTo(wordToAdd.getWord()).limitToFirst(1);
            wordQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean newWord = true;
                    //there is no word so it's new
                    if (!dataSnapshot.exists()){
                        newWord = true;
                    } else {
                        //only one to loop
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            VocabularyListWord word = child.getValue(VocabularyListWord.class);
                            //check if an item exists with the same word/meaning pair.
                            //if there is a word with different meanings,
                            //we should be able to add both meanings
                            if (word != null &&
                                    word.getWord().equals(wordToAdd.getWord()) &&
                                    word.getMeanings().contains(wordToAdd.getMeaning())) {
                                newWord = false;
                            } else {
                                newWord = true;
                            }
                        }
                    }
                    checkedVocabulary.add(new NewVocabularyWrapper(wordToAdd, newWord));

                    if (vocabularyCheckedCt.incrementAndGet() == vocabularyToCheckCt){
                        onResultListener.onLessonVocabularyQueried(checkedVocabulary);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void getUserInterests(final OnResultListener onResultListener){
        DatabaseReference userInterestRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseDBHeaders.USER_INTERESTS + "/" +
                        getUserID());
        //order alphabetically.
        //'pronunciation' is a variable in the WikiDataEntryData class
        Query userInterestQuery = userInterestRef.orderByChild("pronunciation");

        ValueEventListener userInterestQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> userInterests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    WikiDataEntryData interest = snapshot.getValue(WikiDataEntryData.class);
                    userInterests.add(interest);
                }

                onResultListener.onUserInterestsQueried(userInterests);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userInterestQuery.addValueEventListener(userInterestQueryListener);
        pairs.add(new EventListenerPair(userInterestQuery, userInterestQueryListener));
    }

    @Override
    public void addUserInterests(final List<WikiDataEntryData> userInterestsToAdd, final OnResultListener onResultListener){
        //add the interests here so we don't have to worry about whether
        // we grabbed the updated user interest list or the old one
        Map<String, Object> toAdd = new HashMap<>(userInterestsToAdd.size());
        for (WikiDataEntryData data : userInterestsToAdd){
            String ref = FirebaseDBHeaders.USER_INTERESTS + "/" +
                    getUserID() + "/" +
                    data.getWikiDataID();
            toAdd.put(ref, data);
        }
        FirebaseDatabase.getInstance().getReference().updateChildren(toAdd).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onResultListener.onUserInterestsAdded();
            }
        });
        /*
        //we need to add to the user's interest list and update the recommendation edges
        //grab the current user interests so we can update the recommendation edges
        DatabaseReference allUserInterestsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                        getUserID()
        );
        allUserInterestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //now update the recommendation edges
                List<WikiDataEntryData> allInterests = new ArrayList<>((int)dataSnapshot.getChildrenCount() +
                        userInterestsToAdd.size());
                //we need to make sure we connect edges between interests we are adding
                allInterests.addAll(userInterestsToAdd);
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData childData = child.getValue(WikiDataEntryData.class);
                    allInterests.add(childData);
                }
                for (WikiDataEntryData toAddInterest : userInterestsToAdd) {
                    for (WikiDataEntryData toConnectInterest : allInterests) {
                        //we don't want to add a recommendation path to the same interest
                        if (!toAddInterest.equals(toConnectInterest)) {
                            connectRecommendationEdge(toConnectInterest, toAddInterest);
                            connectRecommendationEdge(toAddInterest, toConnectInterest);
                        }
                    }
                    //make sure we don't connect a to-add interest to another to-add interest
                    allInterests.remove(toAddInterest);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    /*
    @Override
    protected void connectRecommendationEdge(final WikiDataEntryData fromInterest, final WikiDataEntryData toInterest){
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
    }*/

    @Override
    public void removeUserInterests(final List<WikiDataEntryData> userInterestsToRemove, final OnResultListener onResultListener){
        //remove all the interests first so the UI updates as soon as the items are deleted
        Map<String, Object> toRemove = new HashMap<>(userInterestsToRemove.size());
        for (WikiDataEntryData data : userInterestsToRemove){
            String ref = FirebaseDBHeaders.USER_INTERESTS + "/" +
                    getUserID() + "/" +
                    data.getWikiDataID();
            toRemove.put(ref, null);
        }
        FirebaseDatabase.getInstance().getReference().updateChildren(toRemove)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onResultListener.onUserInterestsRemoved();
                    }
                });

        /*
        //fetch all of the user's current interests first.
        //we need them so we can remove the recommendation edges
        DatabaseReference allUserInterestsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                        getUserID()
        );

        allUserInterestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //now update the edges one by one.
                //each of the current user interests has an edge to
                //each of the user interests to remove.
                //we need to decrement each of these edges.
                List<WikiDataEntryData> allUserInterests = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    WikiDataEntryData childData = child.getValue(WikiDataEntryData.class);
                    if (childData != null) {
                        allUserInterests.add(childData);
                    }
                }
                //we don't know if the dataSnapshot comes before or after we added,
                //so just add it if it doesn't exist yet
                for (WikiDataEntryData data : userInterestsToRemove){
                    if (!allUserInterests.contains(data))
                        allUserInterests.add(data);
                }

                for (WikiDataEntryData data : userInterestsToRemove) {
                    //don't want to remove a recommendation edge to itself. (it doesn't exist)
                    //removing the data to remove here also prevents removing an edge
                    // from an interest we are removing to another interest we are removing
                    // twice.
                    allUserInterests.remove(data);
                    for (WikiDataEntryData userInterest : allUserInterests) {
                        disconnectRecommendationEdge(userInterest, data);
                        disconnectRecommendationEdge(data, userInterest);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    /*
    @Override
    protected void disconnectRecommendationEdge(final WikiDataEntryData fromInterest, final WikiDataEntryData toInterest){
        //remove from two maps
        DatabaseReference edgeRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                        fromInterest.getWikiDataID() + "/" +
                        toInterest.getWikiDataID() + "/" +
                        FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT
        );
        //since we are decrementing,
        //we don't want to overwrite existing data.
        //so we are running the update in a transaction
        edgeRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long edgeWeight = mutableData.getValue(Long.class);
                if (edgeWeight != null){
                    if (edgeWeight == 1){
                        mutableData.setValue(null);
                        FirebaseDatabase.getInstance().getReference(
                                FirebaseDBHeaders.RECOMMENDATION_MAP + "/" +
                                        fromInterest.getWikiDataID() + "/" +
                                        toInterest.getWikiDataID()
                        ).removeValue();
                    } else {
                        mutableData.setValue(edgeWeight - 1);
                    }
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
                if (edgeWeight != null){
                    if (edgeWeight == 1){
                        mutableData.setValue(null);
                        FirebaseDatabase.getInstance().getReference(
                                FirebaseDBHeaders.RECOMMENDATION_MAP_FOR_LESSON_GENERATION + "/" +
                                        fromInterest.getWikiDataID() + "/" +
                                        Integer.toString(toInterest.getClassification()) + "/" +
                                        toInterest.getWikiDataID()
                        ).removeValue();
                    } else {
                        mutableData.setValue(edgeWeight - 1);
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }*/

    @Override
    public void getClearedLessons(int lessonLevel, final OnResultListener onResultListener){
        DatabaseReference clearedLessonsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                        getUserID() + "/" +
                        lessonLevel
        );
        ValueEventListener clearedLessonsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> clearedLessonKeys = new HashSet<>((int)dataSnapshot.getChildrenCount()+1);
                for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()){
                    String lessonKey = lessonSnapshot.getKey();
                    clearedLessonKeys.add(lessonKey);
                }
                onResultListener.onClearedLessonsQueried(clearedLessonKeys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        clearedLessonsRef.addValueEventListener(clearedLessonsListener);
        pairs.add(new EventListenerPair(clearedLessonsRef, clearedLessonsListener));
    }

    @Override
    public void addClearedLesson(int lessonLevel, String lessonKey, final OnResultListener onResultListener){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                        getUserID() + "/" +
                        Integer.toString(lessonLevel) + "/" +
                        lessonKey
        );
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    ref.setValue(true);
                    onResultListener.onClearedLessonAdded(true);
                } else {
                    onResultListener.onClearedLessonAdded(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void addInstanceRecord(InstanceRecord record, final OnResultListener onResultListener){
        final String recordKey = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" +
                        getUserID() + "/" +
                        record.getLessonId() + "/" +
                        record.getInstanceId())
                .push().getKey();
        record.setId(recordKey);
        FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" +
                        getUserID() + "/" +
                        record.getLessonId() + "/" +
                        record.getInstanceId() + "/" +
                        recordKey
        ).setValue(record).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onResultListener.onInstanceRecordAdded(recordKey);
            }
        });
    }

    @Override
    public void getReportCard(int level, final OnResultListener onResultListener){
        DatabaseReference reportCardRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REPORT_CARD + "/" +
                        getUserID() + "/" +
                        Integer.toString(level)
        );
        ValueEventListener reportCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserProfile_ReportCardDataWrapper> reportCardData =
                        new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()){
                    String lessonKey = lessonSnapshot.getKey();
                    Integer correctCt = lessonSnapshot.child(FirebaseDBHeaders.REPORT_CARD_CORRECT)
                            .getValue(Integer.class);
                    if (correctCt == null)
                        correctCt = 0;
                    Integer totalCt = lessonSnapshot.child(FirebaseDBHeaders.REPORT_CARD_TOTAL)
                            .getValue(Integer.class);
                    if (totalCt == null)
                        totalCt = 0;
                    UserProfile_ReportCardDataWrapper wrapper =
                            new UserProfile_ReportCardDataWrapper(lessonKey, correctCt, totalCt);
                    reportCardData.add(wrapper);
                }

                onResultListener.onReportCardQueried(reportCardData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        reportCardRef.addValueEventListener(reportCardListener);
        pairs.add(new EventListenerPair(reportCardRef, reportCardListener));

    }

    @Override
    public void addReportCard(int level, String lessonKey, final int correctCt, final int totalCt, final OnResultListener onResultListener){
        final AtomicInteger finishedCt = new AtomicInteger(0);
        final int updateLocationCt = 2;
        final DatabaseReference correctCtRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REPORT_CARD + "/" +
                        getUserID() + "/" +
                        Integer.toString(level) + "/" +
                        lessonKey + "/" +
                        FirebaseDBHeaders.REPORT_CARD_CORRECT
        );
        correctCtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentVal = dataSnapshot.getValue(Integer.class);
                if (currentVal == null){
                    currentVal = 0;
                }
                int newVal = currentVal + correctCt;
                correctCtRef.setValue(newVal).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //only do this if both updates are complete
                        if (finishedCt.incrementAndGet() == updateLocationCt){
                            onResultListener.onReportCardAdded();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final DatabaseReference totalCtRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REPORT_CARD + "/" +
                        getUserID() + "/" +
                        Integer.toString(level) + "/" +
                        lessonKey + "/" +
                        FirebaseDBHeaders.REPORT_CARD_TOTAL
        );
        totalCtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentVal = dataSnapshot.getValue(Long.class);
                if (currentVal == null){
                    currentVal = 0L;
                }
                long newVal = currentVal + totalCt;
                totalCtRef.setValue(newVal).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //only do this if both updates are finished
                        if (finishedCt.incrementAndGet() == updateLocationCt){
                            onResultListener.onReportCardAdded();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getFirstAppUsageDate(final OnResultListener onResultListener){
        DatabaseReference usageRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                        getUserID()
        );
        //key is in alphabetical order
        Query minimumMonthRef = usageRef.orderByKey().limitToFirst(1);
        minimumMonthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //only loops once
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DateTime minDate = DateTime.now();
                    for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                        AppUsageLog log = logSnapshot.getValue(AppUsageLog.class);
                        if (log == null)
                            continue;
                        DateTime startDateTime = new DateTime(log.getStartTimeStamp());
                        if (startDateTime.isBefore(minDate)){
                            minDate = startDateTime;
                        }
                    }
                    onResultListener.onFirstAppUsageDateQueried(minDate);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getAppUsageForMonths(String startMonthKey, String endMonthKey, final OnResultListener onResultListener){
        DatabaseReference usageRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                        getUserID() + "/"
        );
        Query usageRefForMonths;
        if (startMonthKey.equals(endMonthKey)){
            usageRefForMonths = usageRef.orderByKey().equalTo(startMonthKey);
        } else {
            usageRefForMonths = usageRef.orderByKey().startAt(startMonthKey).endAt(endMonthKey);
        }
        usageRefForMonths.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AppUsageLog> logsForMonth = new ArrayList<>();
                for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot logSnapshot : monthSnapshot.getChildren()) {
                        AppUsageLog log = logSnapshot.getValue(AppUsageLog.class);
                        logsForMonth.add(log);
                    }
                }
                onResultListener.onAppUsageForMonthsQueried(logsForMonth);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
