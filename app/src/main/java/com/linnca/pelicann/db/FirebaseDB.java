package com.linnca.pelicann.db;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessondetails.LessonInstanceDataQuestionSet;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.results.NewVocabularyWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.userprofile.UserProfile_ReportCardDataWrapper;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

//make sure the behavior replicates the behavior in the mock database
// (we user the mock database to test methods that would otherwise
//  connect to an actual database)

public class FirebaseDB extends Database{
    //we can't save an instance of the database because it isn't serializable
    //FirebaseDatabase db = FirebaseDatabase.getInstance();
    //Firebase requires both the reference and the listener attached to it
    // to remove the listener.
    private class RefListenerPair {
        private Query ref;
        private ValueEventListener eventListener;

        private RefListenerPair(Query ref, ValueEventListener eventListener) {
            this.ref = ref;
            this.eventListener = eventListener;
        }
        
        private void removeListener(){
            ref.removeEventListener(eventListener);
            ref = null;
            eventListener = null;
        }
    }
    //this should never be serialized (can't serialize)
    private List<RefListenerPair> refListenerPairs = Collections.synchronizedList(
            new ArrayList<RefListenerPair>());

    @Override
    public String getUserID(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void cleanup(){
        //Firebase keeps connections open so it can update changes in real time.
        //when we do not need the data anymore,
        //we should remove the connections
        for (RefListenerPair pair : refListenerPairs){
            pair.removeListener();
        }
        refListenerPairs.clear();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        //if we have any value event listeners left when we try to
        // serialize the database instance, the program will crash
        // because the value event listeners are not serializable
        cleanup();
        out.defaultWriteObject();
    }

    //not for client
    @Override
    public void addGenericQuestions(List<QuestionData> questions, List<VocabularyWord> vocabularyWords){
        HashMap<String, Object> toUpdate = new HashMap<>(questions.size() + vocabularyWords.size());
        for (QuestionData data : questions){
            String id = data.getId();
            if (id == null){
                continue;
            }
            String questionRef =
                    FirebaseDBHeaders.QUESTIONS + "/" +
                            id;
            toUpdate.put(questionRef, data);
        }

        for (VocabularyWord word : vocabularyWords){
            String id = word.getId();
            if (id == null){
                continue;
            }
            String vocabularyRef =
                    FirebaseDBHeaders.VOCABULARY + "/" +
                            id;
            toUpdate.put(vocabularyRef, word);
        }

        FirebaseDatabase.getInstance().getReference().updateChildren(toUpdate);
    }

    @Override
    public void searchQuestions(String lessonKey, List<WikiDataEntryData> userInterests, int toPopulate,
                                         final List<String> questionSetIDsToAvoid,
                                         final OnResultListener onResultListener){
        final List<QuestionSet> questionSetsToReturn = Collections.synchronizedList(
                new ArrayList<QuestionSet>(toPopulate)
        );
        final List<WikiDataEntryData> userInterestsAlreadyChecked = Collections.synchronizedList(
                new ArrayList<WikiDataEntryData>(userInterests.size())
        );
        final AtomicInteger questionSetsToPopulateAtomicInt = new AtomicInteger(toPopulate);
        final AtomicInteger userInterestsLooped = new AtomicInteger(0);
        final int userInterestsToLoop = userInterests.size();
        DatabaseReference questionSetRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey);

        for (final WikiDataEntryData userInterest : userInterests){
            //we might be finished before looping through the interests.
            //in that case, we shouldn't send a request to the database
            if (questionSetsToPopulateAtomicInt.get() == 0){
                break;
            }

            Query userInterestQuestionSetRef = questionSetRef
                    .orderByChild(FirebaseDBHeaders.QUESTION_SET_INTEREST_ID)
                    .equalTo(userInterest.getWikiDataID());

            userInterestQuestionSetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //if we don't need to get any more
                    if (questionSetsToPopulateAtomicInt.get() == 0){
                        return;
                    }

                    //check the question sets to see if we have one the user hasn't had yet.
                    //we can't just check for the user interest ID because the user might have
                    //covered one question set for a uer interest but not another
                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null){
                        userInterestsAlreadyChecked.add(userInterest);

                        for (DataSnapshot questionSetSnapshot : dataSnapshot.getChildren()){
                            QuestionSet questionSet = questionSetSnapshot.getValue(QuestionSet.class);
                            if (!questionSetIDsToAvoid.contains(questionSet.getKey())) {
                                questionSetsToReturn.add(questionSet);
                                if (questionSetsToPopulateAtomicInt.decrementAndGet() == 0) break;
                            }
                        }

                        //we have found enough questions from the database alone
                        if (questionSetsToPopulateAtomicInt.get() == 0) {
                            onResultListener.onQuestionsQueried(questionSetsToReturn,
                                    userInterestsAlreadyChecked);
                            return;
                        }
                    }

                    //if this is the last one, we should finish
                    if (userInterestsLooped.incrementAndGet() == userInterestsToLoop){
                        onResultListener.onQuestionsQueried(questionSetsToReturn,
                                userInterestsAlreadyChecked);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public void addQuestions(String lessonKey, List<QuestionDataWrapper> questions, OnResultListener onResultListener){
        DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference(FirebaseDBHeaders.QUESTIONS);
        //this is the actual question information for when we want to show
        // questions to the user
        DatabaseReference questionSetRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey
        );

        //any new vocabulary examples
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY
        );
        //we are looping through each question set.
        // (questionDataWrapper has an extra field to store the wikiData ID
        // associated with the question set)
        for (QuestionDataWrapper questionDataWrapper : questions){
            List<List<String>> questionIDs = new ArrayList<>();
            List<String> vocabularyIDs = new ArrayList<>();
            //save each question in the database
            for (List<QuestionData> question : questionDataWrapper.getQuestionSet()) {
                List<String> questionIDsForEachVariation = new ArrayList<>();
                //each question may have multiple variations.
                //save all variations as individual questions
                for (QuestionData data : question) {
                    String questionKey = questionRef.push().getKey();
                    //set ID in data
                    data.setId(questionKey);
                    //save in db
                    DatabaseReference singleQuestionRef = questionRef.child(questionKey);
                    singleQuestionRef.setValue(data);
                    questionIDsForEachVariation.add(questionKey);
                }

                questionIDs.add(questionIDsForEachVariation);

                List<VocabularyWord> questionSetVocabulary = questionDataWrapper.getVocabulary();
                if (questionSetVocabulary != null) {
                    for (VocabularyWord word : questionSetVocabulary) {
                        String vocabularyKey = vocabularyRef.push().getKey();
                        word.setId(vocabularyKey);
                        vocabularyRef.child(vocabularyKey).setValue(word);
                        vocabularyIDs.add(vocabularyKey);
                    }
                }
            }

            //save the question set (pretty much the same thing as the
            //questionDataWrapper but just the IDs
            String questionSetKey = questionSetRef.push().getKey();
            //the initial count should be 0 since we don't know if the user calling this
            //is adding this to his lesson instance
            // (we may be creating extra question sets not needed by the current user)
            QuestionSet questionSet = new QuestionSet(questionSetKey, questionDataWrapper.getWikiDataID(),
                    questionDataWrapper.getInterestLabel(),
                    questionIDs, vocabularyIDs, 0);
            questionSetRef.child(questionSetKey).setValue(questionSet);

            onResultListener.onQuestionSetAdded(questionSet);
        }

        onResultListener.onQuestionsAdded();
    }

    @Override
    public void getQuestionSets(String lessonKey, List<String> questionSetIDs, final OnResultListener onResultListener){
        //to check each listener to see if all listeners have completed
        final AtomicInteger questionSetsRetrievedCt = new AtomicInteger(0);
        final List<QuestionSet> questionSetsRetrieved = Collections.synchronizedList(
                new ArrayList<QuestionSet>(questionSetIDs.size())
        );
        final int questionSetsToRetrieve = questionSetIDs.size();
        DatabaseReference questionSetsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey
        );
        for (final String questionSetID : questionSetIDs){
            DatabaseReference questionSetRef = questionSetsRef.child(questionSetID);
            questionSetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    QuestionSet questionSet = dataSnapshot.getValue(QuestionSet.class);
                    questionSetsRetrieved.add(questionSet);
                    if (questionSetsRetrievedCt.incrementAndGet() == questionSetsToRetrieve){
                        //all listeners have completed so continue
                        onResultListener.onQuestionSetsQueried(questionSetsRetrieved);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public void changeQuestionSetCount(String lessonKey, String questionSetID, final int amount, OnResultListener onResultListener){
        DatabaseReference questionSetCountRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey + "/" +
                        questionSetID + "/" +
                        FirebaseDBHeaders.QUESTION_SET_COUNT
        );
        questionSetCountRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                int newValue;
                newValue = currentValue == null ? amount : currentValue + amount;
                mutableData.setValue(newValue);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    @Override
    public void getPopularQuestionSets(String lessonKey, final List<String> questionSetsToAvoid,
                                       final int questionSetsToPopulate,
                                       final OnResultListener onResultListener){
        //by pigeon hole, we can guarantee that we get enough question sets
        // (only if there are enough questions sets available)
        int toGet = questionSetsToAvoid.size() + questionSetsToPopulate;
        DatabaseReference questionSetsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey
        );
        //since ordering goes 1, 2, ..., 10
        // we want the last ones because they are the most popular
        Query questionSetPopularityQuery = questionSetsRef.orderByChild(FirebaseDBHeaders.QUESTION_SET_COUNT)
                .limitToLast(toGet);
        questionSetPopularityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<QuestionSet> questionSets = new LinkedList<>();
                for (DataSnapshot questionSetSnapshot : dataSnapshot.getChildren()){
                    QuestionSet questionSet = questionSetSnapshot.getValue(QuestionSet.class);
                    //first filter out every question set that we should avoid
                    if (questionSet != null &&
                            !questionSetsToAvoid.contains(questionSet.getKey())){
                        questionSets.add(questionSet);
                    }
                }
                //then adjust the size so we only get enough to fill the lesson instance
                if (questionSets.size() > questionSetsToPopulate){
                    //the last ones are the most popular so cut everything in the front of the list
                    questionSets = new LinkedList<>(
                            questionSets.subList(questionSets.size()-questionSetsToPopulate,
                            questionSets.size())
                    );
                }
                onResultListener.onPopularQuestionSetsQueried(questionSets);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getQuestion(String questionID, final OnResultListener onResultListener){
        DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTIONS + "/" +
                        questionID
        );
        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                QuestionData questionData = dataSnapshot.getValue(QuestionData.class);
                onResultListener.onQuestionQueried(questionData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void addLessonInstance(String lessonKey, LessonInstanceData lessonInstanceData,
                                  List<String> lessonInstanceVocabularyIDs,
                                  final OnResultListener onResultListener){
        DatabaseReference lessonInstanceRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_INSTANCES + "/" + getUserID() + "/" + lessonKey);
        String key = lessonInstanceRef.push().getKey();
        lessonInstanceData.setId(key);
        Map<String, Object> consistentUpdate = new HashMap<>();
        consistentUpdate.put(FirebaseDBHeaders.LESSON_INSTANCES + "/" + getUserID() + "/" + lessonKey + "/" + key, lessonInstanceData);
        consistentUpdate.put(FirebaseDBHeaders.LESSON_INSTANCE_VOCABULARY + "/" + key, lessonInstanceVocabularyIDs);

        FirebaseDatabase.getInstance().getReference().updateChildren(consistentUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onResultListener.onLessonInstanceAdded();
            }
        });
    }

    @Override
    public void getLessonInstances(String lessonKey, boolean persistentConnection, final OnResultListener onResultListener){
        DatabaseReference lessonInstancesRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_INSTANCES + "/"+
                        getUserID()+"/"+
                        lessonKey
        );

        ValueEventListener lessonInstancesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LessonInstanceData> lessonInstances = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    LessonInstanceData instance =childSnapshot.getValue(LessonInstanceData.class);
                    lessonInstances.add(instance);
                }
                onResultListener.onLessonInstancesQueried(lessonInstances);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (persistentConnection) {
            lessonInstancesRef.addValueEventListener(lessonInstancesListener);
        } else {
            lessonInstancesRef.addListenerForSingleValueEvent(lessonInstancesListener);
        }
        refListenerPairs.add(new RefListenerPair(lessonInstancesRef, lessonInstancesListener));
    }

    @Override
    public void getLessonInstanceDetails(String lessonKey, String instanceID, final OnResultListener onResultListener){
        DatabaseReference recordsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" +
                        getUserID() + "/" +
                        lessonKey + "/" +
                        instanceID
        );
        recordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<InstanceRecord> allRecords = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()){
                    InstanceRecord record = recordSnapshot.getValue(InstanceRecord.class);
                    allRecords.add(record);
                }

                onResultListener.onLessonInstanceDetailsQueried(allRecords);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void removeLessonInstance(String lessonKey, LessonInstanceData instance, final OnResultListener onResultListener){
        String instancesPath = FirebaseDBHeaders.LESSON_INSTANCES + "/"+
                getUserID()+"/"+
                lessonKey + "/" +
                instance.getId();
        DatabaseReference instanceRef = FirebaseDatabase.getInstance().getReference(
                instancesPath
        );
        instanceRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onResultListener.onLessonInstanceRemoved();
            }
        });
        //if any of the question sets were part of the popularity rankings,
        //decrement the ranking count
        for (LessonInstanceDataQuestionSet set : instance.getQuestionSets()){
            if (set.isPartOfPopularityRating()){
                this.changeQuestionSetCount(lessonKey, set.getId(), -1,
                        new OnResultListener() {
                            @Override
                            public void onQuestionSetCountChanged() {
                                super.onQuestionSetCountChanged();
                            }
                        });
            }
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
        String vocabularyPath = FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                getUserID();
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                vocabularyPath
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
        refListenerPairs.add(new RefListenerPair(vocabularyRef, vocabularyEventListener));
    }

    //when the user adds a word to his list,
    // not when lesson generation adds vocabulary words
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
        ValueEventListener vocabularyListener = new ValueEventListener() {
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
        };
        vocabularyRef.addListenerForSingleValueEvent(vocabularyListener);
        refListenerPairs.add(new RefListenerPair(vocabularyRef, vocabularyListener));
    }

    private void fetchVocabularyWord(String id, final int vocabularyToFetch, final AtomicInteger vocabularyFetched, final List<VocabularyWord> allWords, final OnResultListener onResultListener){
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY + "/" +
                        id
        );
        ValueEventListener vocabularyListener = new ValueEventListener() {
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
        };
        vocabularyRef.addListenerForSingleValueEvent(vocabularyListener);
        refListenerPairs.add(new RefListenerPair(vocabularyRef, vocabularyListener));
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
            ValueEventListener wordListener = new ValueEventListener() {
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
            };
            wordQuery.addListenerForSingleValueEvent(wordListener);
            refListenerPairs.add(new RefListenerPair(wordQuery, wordListener));
        }
    }

    @Override
    public void getUserInterests(final OnResultListener onResultListener){
        String userInterestPath = FirebaseDBHeaders.USER_INTERESTS + "/" +
                getUserID();
        DatabaseReference userInterestRef = FirebaseDatabase.getInstance()
                .getReference(userInterestPath);
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
        refListenerPairs.add(new RefListenerPair(userInterestQuery, userInterestQueryListener));
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

        //add to rankings
        for (WikiDataEntryData data : userInterestsToAdd) {
            this.changeUserInterestRanking(data, 1);
        }
    }

    @Override
    public void setPronunciation(String userInterestID, String pronunciation){
        DatabaseReference pronunciationRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                getUserID() + "/" +
                userInterestID + "/" +
                FirebaseDBHeaders.USER_INTERESTS_PRONUNCIATION
        );
        pronunciationRef.setValue(pronunciation);
        //also update the pronunciation in the user interest rankings
        DatabaseReference rankingPronunciationRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTEREST_RANKINGS + "/" +
                        userInterestID + "/" +
                        FirebaseDBHeaders.USER_INTEREST_RANKINGS_DATA + "/" +
                        FirebaseDBHeaders.USER_INTERESTS_PRONUNCIATION
        );
        rankingPronunciationRef.setValue(pronunciation);
    }

    @Override
    public void setClassification(String userInterestID, int classification){
        DatabaseReference classificationRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTERESTS + "/" +
                        getUserID() + "/" +
                        userInterestID + "/" +
                        FirebaseDBHeaders.USER_INTERESTS_CLASSIFICATION
        );
        classificationRef.setValue(classification);
        //also update the classification in the user interest rankings
        DatabaseReference rankingClassificationRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTEREST_RANKINGS + "/" +
                        userInterestID + "/" +
                        FirebaseDBHeaders.USER_INTEREST_RANKINGS_DATA + "/" +
                        FirebaseDBHeaders.USER_INTERESTS_CLASSIFICATION
        );
        rankingClassificationRef.setValue(classification);
    }

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
        for (WikiDataEntryData data : userInterestsToRemove){
            this.changeUserInterestRanking(data, -1);
        }
    }

    @Override
    public void changeUserInterestRanking(final WikiDataEntryData data, final int count){
        DatabaseReference rankingRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTEREST_RANKINGS + "/" +
                        data.getWikiDataID()
        );
        rankingRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentCt = mutableData
                        .child(FirebaseDBHeaders.USER_INTEREST_RANKINGS_COUNT)
                        .getValue(Integer.class);
                if (currentCt == null){
                    //this is a new interest no one else has ranked
                    mutableData.child(FirebaseDBHeaders.USER_INTEREST_RANKINGS_COUNT)
                            .setValue(count);
                    mutableData.child(FirebaseDBHeaders.USER_INTEREST_RANKINGS_DATA)
                            .setValue(data);
                } else {
                    int newCt = currentCt + count;
                    if (newCt <= 0){
                        //remove if the count goes below 0
                        mutableData.setValue(null);
                    } else {
                        mutableData.child(FirebaseDBHeaders.USER_INTEREST_RANKINGS_COUNT)
                                .setValue(newCt);
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }
    
    @Override
    public void getPopularUserInterests(int count, final OnResultListener onResultListener){
        DatabaseReference popularUserInterestRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTEREST_RANKINGS
        );
        Query popularUserInterestQuery = popularUserInterestRef
                .orderByChild(FirebaseDBHeaders.USER_INTEREST_RANKINGS_COUNT)
                .limitToLast(count);
        popularUserInterestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntryData> userInterests = new LinkedList<>();
                //since the ordering is 1, 2, ... , 10,
                //reverse the order
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userInterests.add(0, 
                            snapshot.child(FirebaseDBHeaders.USER_INTEREST_RANKINGS_DATA)
                                    .getValue(WikiDataEntryData.class)
                    );
                }
                onResultListener.onUserInterestRankingsQueried(userInterests);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getClearedLessons(int lessonLevel, boolean persistentConnection, final OnResultListener onResultListener){
        String clearedLessonsPath = FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                getUserID() + "/" +
                lessonLevel;
        DatabaseReference clearedLessonsRef = FirebaseDatabase.getInstance().getReference(
                clearedLessonsPath
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
        if (persistentConnection)
            clearedLessonsRef.addValueEventListener(clearedLessonsListener);
        else
            clearedLessonsRef.addListenerForSingleValueEvent(clearedLessonsListener);
        refListenerPairs.add(new RefListenerPair(clearedLessonsRef, clearedLessonsListener));
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

    //for debugging
    @Override
    public void clearAllLessons(List<List<LessonListRow>> lessonLevels){
        int lessonLevelCt = lessonLevels.size();
        for (int i=0; i<lessonLevelCt; i++) {
            List<LessonListRow> lessonRows = lessonLevels.get(i);
            //0th level is level 1
            int level = i+1;
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData == null)
                        continue;
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                                    getUserID() + "/" +
                                    level + "/" +
                                    lessonData.getKey()
                    );
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()){
                                ref.setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
    }

    @Override
    public void addReviewQuestion(List<String> questionIDs, final OnResultListener onResultListener){
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REVIEW_QUESTIONS + "/" +
                        getUserID() + "/"
        );

        Map<String, Object> toUpdate = new HashMap<>(questionIDs.size());
        for (String questionID : questionIDs){
            toUpdate.put(questionID, true);
        }
        reviewRef.updateChildren(toUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onResultListener.onReviewQuestionsAdded();
            }
        });
    }

    @Override
    public void getReviewQuestions(final OnResultListener onResultListener){
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REVIEW_QUESTIONS + "/" +
                        getUserID() + "/"
        );
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> questionKeys = new ArrayList<>(
                        (int)dataSnapshot.getChildrenCount()
                );
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()){
                    Boolean exists = questionSnapshot.getValue(Boolean.class);
                    if (exists != null && exists){
                        questionKeys.add(questionSnapshot.getKey());
                    }
                }
                onResultListener.onReviewQuestionsQueried(questionKeys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void removeReviewQuestions(final OnResultListener onResultListener){
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REVIEW_QUESTIONS + "/" +
                        getUserID() + "/"
        );
        reviewRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onResultListener.onReviewQuestionsRemoved();
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
        String reportCardPath = FirebaseDBHeaders.REPORT_CARD + "/" +
                getUserID() + "/" +
                Integer.toString(level);
        DatabaseReference reportCardRef = FirebaseDatabase.getInstance().getReference(
                reportCardPath
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
        refListenerPairs.add(new RefListenerPair(reportCardRef, reportCardListener));

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
    public void addAppUsageLog(AppUsageLog appUsageLog){
        long startTime = appUsageLog.getStartTimeStamp();
        DateTime dateTime = new DateTime(startTime);
        int month = dateTime.getMonthOfYear();
        int year = dateTime.getYear();
        String key = AppUsageLog.formatKey(month, year);
        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                        getUserID() + "/" +
                        key
        );
        logRef.push().setValue(appUsageLog);
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

    //for admin use
    @Override
    public void addSport(String sportWikiDataID, String verb, String object){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.UTILS + "/" +
                        FirebaseDBHeaders.UTILS_SPORTS_VERB_MAPPINGS + "/" +
                        sportWikiDataID
        );

        ref.child(FirebaseDBHeaders.UTILS_SPORT_VERB_MAPPING_OBJECT).setValue(object);
        ref.child(FirebaseDBHeaders.UTILS_SPORT_VERB_MAPPING_VERB).setValue(verb);
    }

    @Override
    public void getSports(Collection<String> sportsWikiDataIDs, final OnResultListener onResultListener){
        DatabaseReference mappingsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.UTILS + "/" +
                    FirebaseDBHeaders.UTILS_SPORTS_VERB_MAPPINGS
        );
        final int lastSportCt = sportsWikiDataIDs.size();
        final AtomicInteger sportCt = new AtomicInteger(0);
        for (final String sportID : sportsWikiDataIDs) {
            DatabaseReference sportRef = mappingsRef.child(sportID);
            sportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String verb = (String) dataSnapshot.child(FirebaseDBHeaders.UTILS_SPORT_VERB_MAPPING_VERB).getValue();
                        String object = (String) dataSnapshot.child(FirebaseDBHeaders.UTILS_SPORT_VERB_MAPPING_OBJECT).getValue();
                        if (object == null) {
                            object = "";
                        }
                        //if no match, the default (and most likely) is
                        // play + sport

                        onResultListener.onSportQueried(sportID, verb, object);
                    }

                    //we searched the last sport
                    if (sportCt.incrementAndGet() == lastSportCt){
                        onResultListener.onSportsQueried();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
