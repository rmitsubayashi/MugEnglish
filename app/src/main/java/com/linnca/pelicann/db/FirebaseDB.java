package com.linnca.pelicann.db;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.FirebaseDBHeaders;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

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

    @Override
    public void searchEntityPropertyData(NetworkConnectionChecker networkConnectionChecker, String lessonKey, List<WikiDataEntity> userInterests, int toPopulate,
                                final OnDBResultListener onDBResultListener){
        final List<EntityPropertyData> toReturn = Collections.synchronizedList(
                new ArrayList<EntityPropertyData>(toPopulate)
        );
        final List<WikiDataEntity> userInterestsAlreadyChecked = Collections.synchronizedList(
                new ArrayList<WikiDataEntity>(userInterests.size())
        );
        final AtomicInteger toPopulateAtomicInt = new AtomicInteger(toPopulate);
        final AtomicInteger userInterestsLooped = new AtomicInteger(0);
        final int userInterestsToLoop = userInterests.size();
        final DatabaseReference entityPropertyDataRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.ENTITY_PROPERTY_DATA + "/" +
                        lessonKey);

        for (final WikiDataEntity userInterest : userInterests){
            //we might be finished before looping through the interests.
            //in that case, we shouldn't send a request to the database
            if (toPopulateAtomicInt.get() == 0){
                break;
            }

            final Query matchedUserInterestRef = entityPropertyDataRef
                    .orderByChild(FirebaseDBHeaders.ENTITY_PROPERTY_DATA_WIKIDATA_ID)
                    .equalTo(userInterest.getWikiDataID());

            final AtomicBoolean called = new AtomicBoolean(false);
            final ValueEventListener matchedUserInterestListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    called.set(true);
                    //if we don't need to get any more
                    if (toPopulateAtomicInt.get() == 0){
                        matchedUserInterestRef.removeEventListener(this);
                        return;
                    }

                    //check the question sets to see if we have one the user hasn't had yet.
                    //we can't just check for the user interest ID because the user might have
                    //covered one question set for a uer interest but not another
                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null){
                        userInterestsAlreadyChecked.add(userInterest);

                        //we only need one per entity
                        List<EntityPropertyData> allMatching = new ArrayList<>((int)dataSnapshot.getChildrenCount());
                        for (DataSnapshot entityDataSnapshot : dataSnapshot.getChildren()){
                            EntityPropertyData data = entityDataSnapshot.getValue(EntityPropertyData.class);
                            allMatching.add(data);
                        }

                        if (allMatching.size() > 0) {
                            Collections.shuffle(allMatching);
                            toReturn.add(allMatching.get(0));
                            //we have found enough questions from the database
                            if (toPopulateAtomicInt.decrementAndGet() == 0) {
                                onDBResultListener.onEntityPropertyDataSearched(toReturn,
                                        userInterestsAlreadyChecked);
                                matchedUserInterestRef.removeEventListener(this);
                                return;
                            }
                        }
                    }

                    //if this is the last one, we should finish
                    if (userInterestsLooped.incrementAndGet() == userInterestsToLoop){
                        onDBResultListener.onEntityPropertyDataSearched(toReturn,
                                userInterestsAlreadyChecked);
                    }
                    matchedUserInterestRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            matchedUserInterestRef.addValueEventListener(matchedUserInterestListener);
            final RefListenerPair pair = new RefListenerPair(entityPropertyDataRef, matchedUserInterestListener);
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
                    entityPropertyDataRef.removeEventListener(matchedUserInterestListener);
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
    public void addEntityPropertyData(String lessonKey, List<EntityPropertyData> data, OnDBResultListener onDBResultListener){
        DatabaseReference entityPropertyDataRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.ENTITY_PROPERTY_DATA + "/" +
                        lessonKey
        );

        //we are looping through each question set.
        // (questionDataWrapper has an extra field to store the wikiData ID
        // associated with the question set)
        for (EntityPropertyData entityPropertyData : data){
            String entityPropertyDataKey = entityPropertyDataRef.push().getKey();
            entityPropertyData.setKey(entityPropertyDataKey);
            //should be handled by each lesson, but just in case
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyDataRef.child(entityPropertyDataKey).setValue(entityPropertyData);
            onDBResultListener.onEntityPropertyDataAdded(entityPropertyData);
        }

        onDBResultListener.onAllEntityPropertyDataAdded();
    }

    @Override
    public void getRandomEntityPropertyData(NetworkConnectionChecker networkConnectionChecker, String lessonKey,
                                       final List<EntityPropertyData> toAvoid,
                                       final int toPopulate,
                                       final OnDBResultListener onDBResultListener){
        //by pigeon hole, we can guarantee that we get enough question sets
        // (only if there are enough questions sets available)
        final int toGet = toAvoid.size() + toPopulate;
        DatabaseReference entityPropertyDataRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.ENTITY_PROPERTY_DATA + "/" +
                        lessonKey
        );
        final Query randomEntityPropertyDataQuery =
                entityPropertyDataRef.limitToLast(toGet);
        final AtomicBoolean called = new AtomicBoolean(false);
        final ValueEventListener entityPropertyDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<EntityPropertyData> entityPropertyDataList = new ArrayList<>(toGet);
                for (DataSnapshot entityPropertyDataSnapshot : dataSnapshot.getChildren()){
                    EntityPropertyData data = entityPropertyDataSnapshot.getValue(EntityPropertyData.class);
                    if (data != null &&
                            data.isUnique(toAvoid)){
                        entityPropertyDataList.add(data);
                    }

                    if (entityPropertyDataList.size() == toPopulate){
                        break;
                    }
                }
                onDBResultListener.onRandomEntityPropertyDataQueried(entityPropertyDataList);

                randomEntityPropertyDataQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        randomEntityPropertyDataQuery.addListenerForSingleValueEvent(entityPropertyDataListener);
        final RefListenerPair pair = new RefListenerPair(randomEntityPropertyDataQuery, entityPropertyDataListener);
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
                randomEntityPropertyDataQuery.removeEventListener(entityPropertyDataListener);
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
        //lesson generation depends on the id being updated here
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
    public void getMostRecentLessonInstance(NetworkConnectionChecker networkConnectionChecker,
                                            String lessonKey, final OnDBResultListener onDBResultListener){
        final AtomicBoolean called = new AtomicBoolean(false);
        String instancesPath = FirebaseDBHeaders.LESSON_INSTANCES + "/" +
                getUserID() + "/" +
                lessonKey;
        DatabaseReference instancesRef = FirebaseDatabase.getInstance().getReference(instancesPath);
        final Query mostRecentInstanceQuery = instancesRef.orderByChild(FirebaseDBHeaders.LESSON_INSTANCE_TIMESTAMP)
                .limitToLast(1);
        ValueEventListener mostRecentInstanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                called.set(true);
                List<LessonInstanceData> result = new ArrayList<>(1);
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    LessonInstanceData data = childSnapshot.getValue(LessonInstanceData.class);
                    result.add(data);
                }

                onDBResultListener.onLessonInstancesQueried(result);

                mostRecentInstanceQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mostRecentInstanceQuery.addValueEventListener(mostRecentInstanceListener);
        refListenerPairs.add(new RefListenerPair(mostRecentInstanceQuery, mostRecentInstanceListener));

        networkConnectionChecker.checkConnection(onDBResultListener, called);
        networkConnections.add(networkConnectionChecker);
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
    public void addReportCard(String lessonKey, final int correctCt, final int totalCt, final OnDBResultListener onDBResultListener){
        final AtomicInteger finishedCt = new AtomicInteger(0);
        final int updateLocationCt = 2;
        final DatabaseReference correctCtRef = FirebaseDatabase.getInstance().getReference(
                FirebaseDBHeaders.REPORT_CARD + "/" +
                        getUserID() + "/" +
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
