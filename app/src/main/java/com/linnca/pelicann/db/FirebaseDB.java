package com.linnca.pelicann.db;

import android.support.annotation.NonNull;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.FirebaseDBHeaders;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceDataQuestionSet;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonListRow;
import pelicann.linnca.com.corefunctionality.questions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSet;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.results.ResultsVocabularyWord;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;
import pelicann.linnca.com.corefunctionality.userprofile.UserProfile_ReportCardDataWrapper;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyListWord;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

//make sure the behavior replicates the behavior in the mock database
// (we user the mock database to test methods that would otherwise
//  connect to an actual database)

public class FirebaseDB extends Database {
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
    public void cleanupDB(){
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
    public void searchQuestions(NetworkConnectionChecker networkConnectionChecker, String lessonKey, List<WikiDataEntity> userInterests, int toPopulate,
                                final List<String> questionSetIDsToAvoid,
                                final OnDBResultListener onDBResultListener){
        final List<QuestionSet> questionSetsToReturn = Collections.synchronizedList(
                new ArrayList<QuestionSet>(toPopulate)
        );
        final List<WikiDataEntity> userInterestsAlreadyChecked = Collections.synchronizedList(
                new ArrayList<WikiDataEntity>(userInterests.size())
        );
        final AtomicInteger questionSetsToPopulateAtomicInt = new AtomicInteger(toPopulate);
        final AtomicInteger userInterestsLooped = new AtomicInteger(0);
        final int userInterestsToLoop = userInterests.size();
        DatabaseReference questionSetRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey);

        for (final WikiDataEntity userInterest : userInterests){
            //we might be finished before looping through the interests.
            //in that case, we shouldn't send a request to the database
            if (questionSetsToPopulateAtomicInt.get() == 0){
                break;
            }

            final Query userInterestQuestionSetRef = questionSetRef
                    .orderByChild(FirebaseDBHeaders.QUESTION_SET_INTEREST_ID)
                    .equalTo(userInterest.getWikiDataID());

            final AtomicBoolean called = new AtomicBoolean(false);
            final ValueEventListener userInterestQuestionSetListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    called.set(true);
                    //if we don't need to get any more
                    if (questionSetsToPopulateAtomicInt.get() == 0){
                        userInterestQuestionSetRef.removeEventListener(this);
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
                            onDBResultListener.onQuestionsQueried(questionSetsToReturn,
                                    userInterestsAlreadyChecked);
                            userInterestQuestionSetRef.removeEventListener(this);
                            return;
                        }
                    }

                    //if this is the last one, we should finish
                    if (userInterestsLooped.incrementAndGet() == userInterestsToLoop){
                        onDBResultListener.onQuestionsQueried(questionSetsToReturn,
                                userInterestsAlreadyChecked);
                    }
                    userInterestQuestionSetRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userInterestQuestionSetRef.addValueEventListener(userInterestQuestionSetListener);
            final RefListenerPair pair = new RefListenerPair(userInterestQuestionSetRef, userInterestQuestionSetListener);
            refListenerPairs.add(pair);

            OnDBResultListener noConnectionListener = new OnDBResultListener() {
                @Override
                public void onNoConnection() {
                    //this is for the UI
                    onDBResultListener.onNoConnection();
                    //this is so we don't continue trying to listen after cancelling.
                    //we can't just clean up because we are still listening to other locations
                    // (i.e. lesson details -> create lesson -> search questions)
                    refListenerPairs.remove(pair);
                    userInterestQuestionSetRef.removeEventListener(userInterestQuestionSetListener);
                }

                @Override
                public void onSlowConnection() {
                    onDBResultListener.onSlowConnection();
                }
            };

            networkConnectionChecker.checkConnection(noConnectionListener, called);
            networkConnections.add(networkConnectionChecker);
        }
    }

    @Override
    public void addQuestions(String lessonKey, List<QuestionSetData> questions, OnDBResultListener onDBResultListener){
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
        for (QuestionSetData questionSetData : questions){
            List<List<String>> questionIDs = new ArrayList<>();
            List<String> vocabularyIDs = new ArrayList<>();
            //save each question in the database
            for (List<QuestionData> question : questionSetData.getQuestionSet()) {
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
            }

            //save the vocabulary for the question set
            List<VocabularyWord> questionSetVocabulary = questionSetData.getVocabulary();
            if (questionSetVocabulary != null) {
                for (VocabularyWord word : questionSetVocabulary) {
                    String vocabularyKey = vocabularyRef.push().getKey();
                    word.setId(vocabularyKey);
                    vocabularyRef.child(vocabularyKey).setValue(word);
                    vocabularyIDs.add(vocabularyKey);
                }
            }

            //save the question set (pretty much the same thing as the
            //questionSetData but just the IDs
            String questionSetKey = questionSetRef.push().getKey();
            //the initial count should be 0 since we don't know if the user calling this
            //is adding this to his lesson instance
            // (we may be creating extra question sets not needed by the current user)
            QuestionSet questionSet = new QuestionSet(questionSetKey, questionSetData.getInterestID(),
                    questionSetData.getInterestLabel(),
                    questionIDs, vocabularyIDs, 0);
            questionSetRef.child(questionSetKey).setValue(questionSet);

            onDBResultListener.onQuestionSetAdded(questionSet);
        }

        onDBResultListener.onQuestionsAdded();
    }

    @Override
    public void getQuestionSets(NetworkConnectionChecker networkConnectionChecker, String lessonKey, List<String> questionSetIDs, final OnDBResultListener onDBResultListener){
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
            final AtomicBoolean called = new AtomicBoolean(false);
            final DatabaseReference questionSetRef = questionSetsRef.child(questionSetID);
            final ValueEventListener questionSetListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    called.set(true);
                    QuestionSet questionSet = dataSnapshot.getValue(QuestionSet.class);
                    questionSetsRetrieved.add(questionSet);
                    if (questionSetsRetrievedCt.incrementAndGet() == questionSetsToRetrieve){
                        //all listeners have completed so continue
                        onDBResultListener.onQuestionSetsQueried(questionSetsRetrieved);
                    }
                    //don't need to keep on listening anymore
                    questionSetRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            questionSetRef.addValueEventListener(questionSetListener);
            final RefListenerPair pair = new RefListenerPair(questionSetRef, questionSetListener);
            refListenerPairs.add(pair);

            OnDBResultListener noConnectionListener = new OnDBResultListener() {
                @Override
                public void onNoConnection() {
                    //this is for the UI
                    onDBResultListener.onNoConnection();
                    //this is so we don't continue trying to listen after cancelling.
                    //we can't just clean up because we are still listening to other locations
                    // (i.e. lesson details -> create lesson -> fetch previous lesson instances)
                    refListenerPairs.remove(pair);
                    questionSetRef.removeEventListener(questionSetListener);
                }

                @Override
                public void onSlowConnection() {
                    onDBResultListener.onSlowConnection();
                }
            };

            networkConnectionChecker.checkConnection(noConnectionListener, called);
            networkConnections.add(networkConnectionChecker);
        }
    }

    @Override
    public void changeQuestionSetCount(String lessonKey, String questionSetID, final int amount, OnDBResultListener onDBResultListener){
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
    public void getPopularQuestionSets(NetworkConnectionChecker networkConnectionChecker, String lessonKey,
                                       final List<String> questionSetsToAvoid,
                                       final int questionSetsToPopulate,
                                       final OnDBResultListener onDBResultListener){
        //by pigeon hole, we can guarantee that we get enough question sets
        // (only if there are enough questions sets available)
        int toGet = questionSetsToAvoid.size() + questionSetsToPopulate;
        DatabaseReference questionSetsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTION_SETS + "/" +
                        lessonKey
        );
        //since ordering goes 1, 2, ..., 10
        // we want the last ones because they are the most popular
        final Query questionSetPopularityQuery = questionSetsRef.orderByChild(FirebaseDBHeaders.QUESTION_SET_COUNT)
                .limitToLast(toGet);
        final AtomicBoolean called = new AtomicBoolean(false);
        final ValueEventListener questionSetPopularityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
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
                onDBResultListener.onPopularQuestionSetsQueried(questionSets);

                questionSetPopularityQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        questionSetPopularityQuery.addListenerForSingleValueEvent(questionSetPopularityListener);
        final RefListenerPair pair = new RefListenerPair(questionSetPopularityQuery, questionSetPopularityListener);
        refListenerPairs.add(pair);

        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                //this is for the UI
                onDBResultListener.onNoConnection();
                //this is so we don't continue trying to listen after cancelling.
                //we can't just clean up because we are still listening to other locations
                // (i.e. lesson details -> create lesson -> fetch popular questions)
                refListenerPairs.remove(pair);
                questionSetPopularityQuery.removeEventListener(questionSetPopularityListener);
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void getQuestion(NetworkConnectionChecker networkConnectionChecker, String questionID, final OnDBResultListener onDBResultListener){
        final AtomicBoolean called = new AtomicBoolean(false);
        final DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.QUESTIONS + "/" +
                        questionID
        );

        final ValueEventListener questionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                QuestionData questionData = dataSnapshot.getValue(QuestionData.class);
                onDBResultListener.onQuestionQueried(questionData);
                called.set(true);
                //we want to remove this listener
                FirebaseDB.this.cleanup();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        final RefListenerPair pair = new RefListenerPair(questionRef, questionListener);
        refListenerPairs.add(pair);
        questionRef.addValueEventListener(questionListener);

        //if we can't establish a connection,
        // we want to remove the connection so we don't try to query the question
        // when connection is re-established
        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                //this is for the UI
                onDBResultListener.onNoConnection();
                //remove the listener.
                //we can't call cleanUp() because that will remove all listeners.
                //we still want to attempt to listen to the other locations
                // (i.e. lesson details)
                refListenerPairs.remove(pair);
                questionRef.removeEventListener(questionListener);
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };
        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void addLessonInstance(NetworkConnectionChecker networkConnectionChecker,
                                  LessonInstanceData lessonInstanceData,
                                  List<String> lessonInstanceVocabularyIDs,
                                  final OnDBResultListener onDBResultListener){
        DatabaseReference lessonInstanceRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_INSTANCES + "/" +
                        getUserID() + "/" +
                        lessonInstanceData.getLessonKey());
        String key = lessonInstanceRef.push().getKey();
        lessonInstanceData.setId(key);
        Map<String, Object> consistentUpdate = new HashMap<>();
        consistentUpdate.put(FirebaseDBHeaders.LESSON_INSTANCES + "/" +
                        getUserID() + "/" +
                        lessonInstanceData.getLessonKey() + "/" +
                        key,
                lessonInstanceData);
        consistentUpdate.put(FirebaseDBHeaders.LESSON_INSTANCE_VOCABULARY + "/" +
                        key,
                lessonInstanceVocabularyIDs);

        final AtomicBoolean called = new AtomicBoolean(false);
        final AtomicBoolean noConnectionCalled = new AtomicBoolean(false);
        FirebaseDatabase.getInstance().getReference().updateChildren(consistentUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                called.set(true);
                if  (!noConnectionCalled.get()) {
                    onDBResultListener.onLessonInstanceAdded();
                }
            }
        });

        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                onDBResultListener.onNoConnection();
                noConnectionCalled.set(true);
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void getLessonInstances(NetworkConnectionChecker networkConnectionChecker, String lessonKey, final boolean persistentConnection, final OnDBResultListener onDBResultListener){
        final AtomicBoolean called = new AtomicBoolean(false);
        final DatabaseReference lessonInstancesRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_INSTANCES + "/"+
                        getUserID()+"/"+
                        lessonKey
        );

        final ValueEventListener lessonInstancesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<LessonInstanceData> lessonInstances = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    LessonInstanceData instance =childSnapshot.getValue(LessonInstanceData.class);
                    lessonInstances.add(instance);
                }
                onDBResultListener.onLessonInstancesQueried(lessonInstances);

                if (!persistentConnection){
                    lessonInstancesRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        lessonInstancesRef.addValueEventListener(lessonInstancesListener);
        final RefListenerPair pair = new RefListenerPair(lessonInstancesRef, lessonInstancesListener);
        refListenerPairs.add(pair);

        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                //this is for the UI
                onDBResultListener.onNoConnection();
                //this is so we don't continue trying to listen after cancelling.
                if (!persistentConnection){
                    //we can't just clean up because we are still listening to other locations
                    // (i.e. lesson details -> create lesson -> fetch previous lesson instances)
                    refListenerPairs.remove(pair);
                    lessonInstancesRef.removeEventListener(lessonInstancesListener);
                }
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void getLessonInstanceDetails(String lessonKey, String instanceID, final OnDBResultListener onDBResultListener){
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

                onDBResultListener.onLessonInstanceDetailsQueried(allRecords);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void removeLessonInstance(String lessonKey, LessonInstanceData instance, final OnDBResultListener onDBResultListener){
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
                onDBResultListener.onLessonInstanceRemoved();
            }
        });
        //if any of the question sets were part of the popularity rankings,
        //decrement the ranking count
        for (LessonInstanceDataQuestionSet set : instance.getQuestionSets()){
            if (set.isPartOfPopularityRating()){
                this.changeQuestionSetCount(lessonKey, set.getId(), -1,
                        new OnDBResultListener() {
                            @Override
                            public void onQuestionSetCountChanged() {
                                super.onQuestionSetCountChanged();
                            }
                        });
            }
        }
    }

    @Override
    public void getVocabularyDetails(NetworkConnectionChecker networkConnectionChecker, String vocabularyItemID, final OnDBResultListener onDBResultListener){
        AtomicBoolean called = new AtomicBoolean(false);
        final DatabaseReference detailsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_DETAILS + "/" +
                        getUserID() + "/" +
                        vocabularyItemID
        );

        ValueEventListener detailsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VocabularyWord> allClusters = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    VocabularyWord word = childSnapshot.getValue(VocabularyWord.class);
                    allClusters.add(word);
                }
                onDBResultListener.onVocabularyWordQueried(allClusters);

                //we have no reason to keep listening
                detailsRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        detailsRef.addValueEventListener(detailsListener);
        refListenerPairs.add(new RefListenerPair(detailsRef, detailsListener));

        networkConnectionChecker.checkConnection(onDBResultListener, called);
        networkConnections.add(networkConnectionChecker);
    }


    @Override
    public void getVocabularyList(NetworkConnectionChecker networkConnectionChecker, final OnDBResultListener onDBResultListener){
        String vocabularyPath = FirebaseDBHeaders.VOCABULARY_LIST + "/" +
                getUserID();
        DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                vocabularyPath
        );

        final AtomicBoolean called = new AtomicBoolean(false);

        ValueEventListener vocabularyEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<VocabularyListWord> words = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    VocabularyListWord word = childSnapshot.getValue(VocabularyListWord.class);
                    words.add(word);
                }
                onDBResultListener.onVocabularyListQueried(words);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        vocabularyRef.addValueEventListener(vocabularyEventListener);
        refListenerPairs.add(new RefListenerPair(vocabularyRef, vocabularyEventListener));

        networkConnectionChecker.checkConnection(onDBResultListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    //when the user adds a word to his list,
    // not when lesson generation adds vocabulary words
    @Override
    public void addVocabularyWord(final VocabularyWord word, final OnDBResultListener onDBResultListener){
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
                    addVocabularyWordPt2(key, word, onDBResultListener);
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

                    addVocabularyWordPt2(key, word, onDBResultListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //we are going to add the whole word to the details list,
    //using the key used/created when adding the item to the list before this
    private void addVocabularyWordPt2(String key, VocabularyWord word, final OnDBResultListener onDBResultListener){
        DatabaseReference detailsRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.VOCABULARY_DETAILS + "/" +
                        getUserID() + "/" +
                        key
        );
        detailsRef.push().setValue(word).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onDBResultListener.onVocabularyWordAdded();
            }
        });
    }

    @Override
    public void removeVocabularyListItems(List<String> vocabularyListItemKeys, final OnDBResultListener onDBResultListener){
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
                        onDBResultListener.onVocabularyListItemsRemoved();
                    }
                }
        );

    }

    @Override
    public void getLessonVocabulary(NetworkConnectionChecker networkConnectionChecker, String lessonInstanceKey, final OnDBResultListener onDBResultListener){
        final AtomicBoolean called = new AtomicBoolean(false);
        //first get all vocabulary for the lesson instance.
        //second, clump all duplicate vocabulary (i.e. dynamic sentences w/ the same word)
        //third, check if the user already has the word added to his list.
        //last, show the list to the user
        final DatabaseReference vocabularyRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.LESSON_INSTANCE_VOCABULARY + "/" +
                        lessonInstanceKey
        );
        ValueEventListener vocabularyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                GenericTypeIndicator<List<String>> type =
                        new GenericTypeIndicator<List<String>>() {};
                List<String> vocabularyIDs = dataSnapshot.getValue(type);
                //the lesson has no new vocabulary words
                if (vocabularyIDs == null) {
                    onDBResultListener.onLessonVocabularyQueried(null);
                    vocabularyRef.removeEventListener(this);
                    return;
                }
                int vocabularyCt = vocabularyIDs.size();
                AtomicInteger vocabularyFetched = new AtomicInteger(0);
                List<VocabularyWord> words = Collections.synchronizedList(new ArrayList<VocabularyWord>(vocabularyCt));
                for (String id : vocabularyIDs){
                    //we only have the IDs so we need to get all information for these words
                    fetchVocabularyWord(id, vocabularyCt, vocabularyFetched, words, onDBResultListener);
                    //after we'v gotten all the word info,
                    //proceed to filtering all the words the user has added already
                }

                vocabularyRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        vocabularyRef.addValueEventListener(vocabularyListener);
        refListenerPairs.add(new RefListenerPair(vocabularyRef, vocabularyListener));

        //we want to keep listening even if there is no connection
        networkConnectionChecker.checkConnection(onDBResultListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    private void fetchVocabularyWord(String id, final int vocabularyToFetch, final AtomicInteger vocabularyFetched, final List<VocabularyWord> allWords, final OnDBResultListener onDBResultListener){
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
                    clusterDuplicateVocabularyItems(allWords, onDBResultListener);
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

    private void clusterDuplicateVocabularyItems(List<VocabularyWord> allWords, OnDBResultListener onDBResultListener){
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

        checkIfAlreadyAdded(filteredWords, onDBResultListener);
    }

    private void checkIfAlreadyAdded(List<List<VocabularyWord>> list, final OnDBResultListener onDBResultListener){
        final AtomicInteger vocabularyCheckedCt = new AtomicInteger(0);
        final int vocabularyToCheckCt = list.size();
        final List<ResultsVocabularyWord> checkedVocabulary = Collections.synchronizedList(new ArrayList<ResultsVocabularyWord>());
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
                    checkedVocabulary.add(new ResultsVocabularyWord(wordToAdd, newWord));

                    if (vocabularyCheckedCt.incrementAndGet() == vocabularyToCheckCt){
                        onDBResultListener.onLessonVocabularyQueried(checkedVocabulary);
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
    public void getUserInterests(NetworkConnectionChecker networkConnectionChecker, final boolean persistentConnection, final OnDBResultListener onDBResultListener){
        String userInterestPath = FirebaseDBHeaders.USER_INTERESTS + "/" +
                getUserID();
        DatabaseReference userInterestRef = FirebaseDatabase.getInstance()
                .getReference(userInterestPath);
        //order alphabetically.
        //'pronunciation' is a variable in the WikiDataEntity class
        final Query userInterestQuery = userInterestRef.orderByChild("pronunciation");

        final AtomicBoolean called = new AtomicBoolean(false);
        final ValueEventListener userInterestQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<WikiDataEntity> userInterests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    WikiDataEntity interest = snapshot.getValue(WikiDataEntity.class);
                    userInterests.add(interest);
                }

                onDBResultListener.onUserInterestsQueried(userInterests);

                if (!persistentConnection){
                    userInterestQuery.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userInterestQuery.addValueEventListener(userInterestQueryListener);
        final RefListenerPair pair = new RefListenerPair(userInterestQuery, userInterestQueryListener);
        refListenerPairs.add(pair);

        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                //this is for the UI
                onDBResultListener.onNoConnection();
                //this is so we don't continue trying to listen after cancelling.
                if (!persistentConnection){
                    //we can't just clean up because we are still listening to other locations
                    // (i.e. lesson details -> create lesson -> fetch user interests)
                    refListenerPairs.remove(pair);
                    userInterestQuery.removeEventListener(userInterestQueryListener);
                }
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void addUserInterests(NetworkConnectionChecker networkConnectionChecker, final List<WikiDataEntity> userInterestsToAdd, final OnDBResultListener onDBResultListener){
        //add the interests here so we don't have to worry about whether
        // we grabbed the updated user interest list or the old one
        Map<String, Object> toAdd = new HashMap<>(userInterestsToAdd.size());
        for (WikiDataEntity data : userInterestsToAdd){
            String ref = FirebaseDBHeaders.USER_INTERESTS + "/" +
                    getUserID() + "/" +
                    data.getWikiDataID();
            toAdd.put(ref, data);
        }
        final AtomicBoolean called = new AtomicBoolean(false);
        final AtomicBoolean noNetworkCalled = new AtomicBoolean(false);
        FirebaseDatabase.getInstance().getReference().updateChildren(toAdd).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                called.set(true);
                if (!noNetworkCalled.get()) {
                    onDBResultListener.onUserInterestsAdded();
                }
            }
        });

        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                onDBResultListener.onNoConnection();
                noNetworkCalled.set(true);
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);

        //add to rankings
        for (WikiDataEntity data : userInterestsToAdd) {
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
    public void addSimilarInterest(String fromID, WikiDataEntity toEntity){
        DatabaseReference similarInterestRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.SIMILAR_USER_INTERESTS + "/" +
                        fromID + "/" +
                        toEntity.getWikiDataID()
        );
        similarInterestRef.setValue(toEntity);
    }

    @Override
    public void getSimilarInterest(String id, final OnDBResultListener onDBResultListener){
        final DatabaseReference similarInterestRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.SIMILAR_USER_INTERESTS + "/" +
                        id
        );

        ValueEventListener similarInterestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WikiDataEntity> similarInterests = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    WikiDataEntity toAdd = childSnapshot.getValue(WikiDataEntity.class);
                    similarInterests.add(toAdd);
                }
                onDBResultListener.onSimilarUserInterestsQueried(similarInterests);

                similarInterestRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        refListenerPairs.add(new RefListenerPair(similarInterestRef, similarInterestListener));
        similarInterestRef.addValueEventListener(similarInterestListener);

        //TODO no network
    }

    @Override
    public void removeUserInterests(final List<WikiDataEntity> userInterestsToRemove, final OnDBResultListener onDBResultListener){
        //remove all the interests first so the UI updates as soon as the items are deleted
        Map<String, Object> toRemove = new HashMap<>(userInterestsToRemove.size());
        for (WikiDataEntity data : userInterestsToRemove){
            String ref = FirebaseDBHeaders.USER_INTERESTS + "/" +
                    getUserID() + "/" +
                    data.getWikiDataID();
            toRemove.put(ref, null);
        }
        FirebaseDatabase.getInstance().getReference().updateChildren(toRemove)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onDBResultListener.onUserInterestsRemoved();
                    }
                });
        for (WikiDataEntity data : userInterestsToRemove){
            this.changeUserInterestRanking(data, -1);
        }
    }

    @Override
    public void changeUserInterestRanking(final WikiDataEntity data, final int count){
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
    public void getPopularUserInterests(NetworkConnectionChecker networkConnectionChecker, int count, final OnDBResultListener onDBResultListener){
        final AtomicBoolean called = new AtomicBoolean(false);
        DatabaseReference popularUserInterestRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.USER_INTEREST_RANKINGS
        );
        final Query popularUserInterestQuery = popularUserInterestRef
                .orderByChild(FirebaseDBHeaders.USER_INTEREST_RANKINGS_COUNT)
                .limitToLast(count);
        final ValueEventListener popularUserInterestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<WikiDataEntity> userInterests = new LinkedList<>();
                //since the ordering is 1, 2, ... , 10,
                //reverse the order
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userInterests.add(0,
                            snapshot.child(FirebaseDBHeaders.USER_INTEREST_RANKINGS_DATA)
                                    .getValue(WikiDataEntity.class)
                    );
                }
                onDBResultListener.onUserInterestRankingsQueried(userInterests);

                popularUserInterestQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        popularUserInterestQuery.addValueEventListener(popularUserInterestListener);
        final RefListenerPair pair = new RefListenerPair(popularUserInterestQuery, popularUserInterestListener);
        refListenerPairs.add(pair);

        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                onDBResultListener.onNoConnection();
                refListenerPairs.remove(pair);
                popularUserInterestQuery.removeEventListener(popularUserInterestListener);
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void getClearedLessons(NetworkConnectionChecker networkConnectionChecker, int lessonLevel, final boolean persistentConnection, final OnDBResultListener onDBResultListener){
        String clearedLessonsPath = FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                getUserID() + "/" +
                lessonLevel;
        final DatabaseReference clearedLessonsRef = FirebaseDatabase.getInstance().getReference(
                clearedLessonsPath
        );
        final AtomicBoolean called = new AtomicBoolean(false);
        final ValueEventListener clearedLessonsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                Set<String> clearedLessonKeys = new HashSet<>((int)dataSnapshot.getChildrenCount()+1);
                for (DataSnapshot lessonSnapshot : dataSnapshot.getChildren()){
                    String lessonKey = lessonSnapshot.getKey();
                    clearedLessonKeys.add(lessonKey);
                }
                onDBResultListener.onClearedLessonsQueried(clearedLessonKeys);
                if (!persistentConnection){
                    clearedLessonsRef.removeEventListener(this);
                    //we can't remove the connection from the saved list,
                    // but removing the same listener twice doesn't throw any exceptions
                    // so we are technically fine
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        clearedLessonsRef.addValueEventListener(clearedLessonsListener);

        final RefListenerPair pair = new RefListenerPair(clearedLessonsRef, clearedLessonsListener);
        refListenerPairs.add(pair);

        //just handling persistent connections for now (one step at a time so we don't break things)
        //if we can't establish a connection,
        // we want to remove the connection so we don't try to query the question
        // when connection is re-established
        OnDBResultListener noConnectionListener = new OnDBResultListener() {
            @Override
            public void onNoConnection() {
                //this is for the UI
                onDBResultListener.onNoConnection();
                //don't need to stop listening for the persistent connection.
                //not sure about the single connection
            }

            @Override
            public void onSlowConnection() {
                onDBResultListener.onSlowConnection();
            }
        };

        networkConnectionChecker.checkConnection(noConnectionListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void addClearedLesson(int lessonLevel, String lessonKey, final OnDBResultListener onDBResultListener){
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
                    onDBResultListener.onClearedLessonAdded(true);
                } else {
                    onDBResultListener.onClearedLessonAdded(false);
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
    public void addReviewQuestion(List<String> questionIDs, final OnDBResultListener onDBResultListener){
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
                onDBResultListener.onReviewQuestionsAdded();
            }
        });
    }

    @Override
    public void getReviewQuestions(final OnDBResultListener onDBResultListener){
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
                onDBResultListener.onReviewQuestionsQueried(questionKeys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void removeReviewQuestions(final OnDBResultListener onDBResultListener){
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REVIEW_QUESTIONS + "/" +
                        getUserID() + "/"
        );
        reviewRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onDBResultListener.onReviewQuestionsRemoved();
            }
        });
    }

    @Override
    public void addInstanceRecord(InstanceRecord record, final OnDBResultListener onDBResultListener){
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
                onDBResultListener.onInstanceRecordAdded(recordKey);
            }
        });
    }

    @Override
    public void getReportCard(int level, final OnDBResultListener onDBResultListener){
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

                onDBResultListener.onReportCardQueried(reportCardData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        reportCardRef.addValueEventListener(reportCardListener);
        refListenerPairs.add(new RefListenerPair(reportCardRef, reportCardListener));

    }

    @Override
    public void addReportCard(int level, String lessonKey, final int correctCt, final int totalCt, final OnDBResultListener onDBResultListener){
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
                            onDBResultListener.onReportCardAdded();
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
                            onDBResultListener.onReportCardAdded();
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
    public void getFirstAppUsageDate(final OnDBResultListener onDBResultListener){
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
                    onDBResultListener.onFirstAppUsageDateQueried(minDate);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getAppUsageForMonths(NetworkConnectionChecker networkConnectionChecker, String startMonthKey, String endMonthKey, final OnDBResultListener onDBResultListener){
        final AtomicBoolean called = new AtomicBoolean(false);
        DatabaseReference usageRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.APP_USAGE + "/" +
                        getUserID() + "/"
        );
        final Query usageRefForMonths;
        if (startMonthKey.equals(endMonthKey)){
            usageRefForMonths = usageRef.orderByKey().equalTo(startMonthKey);
        } else {
            usageRefForMonths = usageRef.orderByKey().startAt(startMonthKey).endAt(endMonthKey);
        }
        ValueEventListener usageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<AppUsageLog> logsForMonth = new ArrayList<>();
                for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot logSnapshot : monthSnapshot.getChildren()) {
                        AppUsageLog log = logSnapshot.getValue(AppUsageLog.class);
                        logsForMonth.add(log);
                    }
                }
                onDBResultListener.onAppUsageForMonthsQueried(logsForMonth);
                //stop listening
                usageRefForMonths.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        usageRefForMonths.addValueEventListener(usageListener);
        refListenerPairs.add(new RefListenerPair(usageRefForMonths, usageListener));

        networkConnectionChecker.checkConnection(onDBResultListener, called);
        networkConnections.add(networkConnectionChecker);
    }

    @Override
    public void addDailyLesson(String date, final OnDBResultListener onDBResultListener){
        final DatabaseReference dailyLessonCtRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.DAILY_LESSON_CT + "/" +
                        getUserID() + "/" +
                        date
        );

        ValueEventListener dailyLessonCtListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer dailyLessonCt = dataSnapshot.getValue(Integer.class);
                //if empty, clear the lesson count from the previously recorded day
                if (dailyLessonCt == null || dailyLessonCt == 0){
                    FirebaseDatabase.getInstance().getReference(
                            FirebaseDBHeaders.DAILY_LESSON_CT + "/" +
                                    getUserID()
                    ).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dailyLessonCtRef.setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    onDBResultListener.onDailyLessonAdded(1);
                                }
                            });
                        }
                    });
                } else {
                    final int newCt = dailyLessonCt + 1;
                    dailyLessonCtRef.setValue(newCt).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            onDBResultListener.onDailyLessonAdded(newCt);
                        }
                    });
                }

                //don't need to keep listening
                dailyLessonCtRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dailyLessonCtRef.addValueEventListener(dailyLessonCtListener);
        //don't schedule this to be cleared by cleanUp(),
        // because that would stop the update.
        //instead, make sure to handle the case in the UI where the user is not
        // on the same fragment anymore
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
    public void getSports(Collection<String> sportsWikiDataIDs, final OnDBResultListener onDBResultListener){
        //if we don't have any sports to query,
        //onSportsQueried will never be called
        if (sportsWikiDataIDs.size() == 0){
            onDBResultListener.onSportsQueried();
            return;
        }

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

                        onDBResultListener.onSportQueried(sportID, verb, object);
                    }

                    //we searched the last sport
                    if (sportCt.incrementAndGet() == lastSportCt){
                        onDBResultListener.onSportsQueried();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
