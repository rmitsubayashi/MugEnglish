package com.linnca.pelicann.questionmanager;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.datawrappers.AchievementStars;
import com.linnca.pelicann.db.datawrappers.InstanceRecord;
import com.linnca.pelicann.db.datawrappers.QuestionAttempt;
import com.linnca.pelicann.db.datawrappers.QuestionData;
import com.linnca.pelicann.gui.Results;

import java.util.ArrayList;
import java.util.List;

//this manages the results displayed to the user
public class ResultsManager {
    private FirebaseDatabase db;
    private String userID;
    private InstanceRecord instanceRecord;
    private ResultsManagerListener resultsManagerListener;

    public interface ResultsManagerListener {
        void onAchievementsSaved(AchievementStars existingAchievements, AchievementStars newAchievements);
    }

    public ResultsManager(InstanceRecord instanceRecord, ResultsManagerListener listener){
        db = FirebaseDatabase.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.instanceRecord = instanceRecord;
        this.resultsManagerListener = listener;
    }

    //since we need to do this synchronously
    //the calls to firebase are nested.
    //1. look at the achievements for the user
    //2. look at past instance records to identify what new stars should be added
    public void identifyAchievements(){
        identifyExistingAchievements();
    }

    private void identifyExistingAchievements(){
        DatabaseReference achievementsRef = db.getReference(
                FirebaseDBHeaders.ACHIEVEMENTS + "/" +
                        userID + "/" +
                        instanceRecord.getLessonId());
        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AchievementStars existingAchievements = dataSnapshot.getValue(AchievementStars.class);
                if (existingAchievements == null){
                    existingAchievements = new AchievementStars();
                    existingAchievements.setFirstInstance(false);
                    existingAchievements.setSecondInstance(false);
                    existingAchievements.setRepeatInstance(false);
                }

                identifyNewAchievements(existingAchievements);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //we are passing the existing achievements so we can compare the old and new set of achievements.
    //identification should not be different whether this instance record is updated prior or after
    private void identifyNewAchievements(final AchievementStars existingAchievements){
        DatabaseReference instanceRecordsRef = db.getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/"+
                        userID + "/" +
                        instanceRecord.getLessonId());
        instanceRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AchievementStars newAchievements = new AchievementStars();
                //since we are updating this instance the first instance has to be true
                newAchievements.setFirstInstance(true);
                newAchievements.setSecondInstance(false);
                newAchievements.setRepeatInstance(false);
                //the reference might be empty if this is the first instance and
                // this instance still hasn't been updated
                if (dataSnapshot.getValue() != null){
                    //check if this is a repeat of the same instance
                    DataSnapshot sameInstanceRecords = dataSnapshot.child(instanceRecord.getInstanceId());
                    for (DataSnapshot snapshot : sameInstanceRecords.getChildren()){
                        InstanceRecord record = snapshot.getValue(InstanceRecord.class);

                        //there exists a record that is not this current one
                        if (!record.equals(instanceRecord)){
                            newAchievements.setRepeatInstance(true);
                        }
                    }
                    //check if there is another instance
                    if (dataSnapshot.getChildrenCount() > 1){
                        newAchievements.setSecondInstance(true);
                    }
                    //this shouldn't be called because this would return null?
                    else if (dataSnapshot.getChildrenCount() == 0) {
                        newAchievements.setSecondInstance(false);
                    } else { // children count = 1
                        if ( !dataSnapshot.hasChild(instanceRecord.getInstanceId()) ){
                            newAchievements.setSecondInstance(true);
                        }
                    }
                }

                if (shouldUpdateStars(existingAchievements, newAchievements)){
                    updateAchievements(existingAchievements, newAchievements);
                    resultsManagerListener.onAchievementsSaved(existingAchievements, newAchievements);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean shouldUpdateStars(AchievementStars existingAchievements, AchievementStars newAchievements){
        //basically check if old = false and new = true
        if (!existingAchievements.getFirstInstance() && newAchievements.getFirstInstance())
            return true;
        if (!existingAchievements.getSecondInstance() && newAchievements.getSecondInstance())
            return true;
        if (!existingAchievements.getRepeatInstance() && newAchievements.getRepeatInstance())
            return true;

        //no need to update
        return false;
    }

    private void updateAchievements(AchievementStars existingAchievements, AchievementStars newAchievements){
        //combine achievements
        AchievementStars combinedAchievements = new AchievementStars();
        if (existingAchievements.getFirstInstance() || newAchievements.getFirstInstance()){
            combinedAchievements.setFirstInstance(true);
        } else{
            combinedAchievements.setFirstInstance(false);
        }

        if (existingAchievements.getSecondInstance() || newAchievements.getSecondInstance()){
            combinedAchievements.setSecondInstance(true);
        } else{
            combinedAchievements.setSecondInstance(false);
        }

        if (existingAchievements.getRepeatInstance() || newAchievements.getRepeatInstance()){
            combinedAchievements.setRepeatInstance(true);
        } else{
            combinedAchievements.setRepeatInstance(false);
        }

        DatabaseReference ref = db.getReference(
                FirebaseDBHeaders.ACHIEVEMENTS + "/" +
                        userID + "/" +
                        instanceRecord.getLessonId());
        //overwrite/create achievements
        ref.setValue(combinedAchievements);
    }

    public void saveInstanceRecord(){
        DatabaseReference ref = db.getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" + userID + "/" +
                instanceRecord.getLessonId() + "/" + instanceRecord.getInstanceId() +"/" + instanceRecord.getId());
        ref.setValue(instanceRecord);
    }
}
