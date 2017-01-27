package com.example.ryomi.myenglish.questionmanager;

import android.content.Context;

import com.example.ryomi.myenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.myenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.myenglish.gui.Results;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//this manages the results displayed to the user
public class ResultsManager {
    private Context context;
    private InstanceRecord instanceRecord;
    //we can find this from the instance ID of instance record
    //but that's one more connection we will have to worry about
    private String themeID;

    public ResultsManager(InstanceRecord instanceRecord, String themeID){
        this.instanceRecord = instanceRecord;
        this.themeID = themeID;
    }

    //save and display results
    public void displayResults(Context context){
        this.context = context;
        startFlow();
    }

    private void startFlow(){
        identifyExistingAchievements();
    }

    private void identifyExistingAchievements(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = db.getReference("achievements/"+userID+"/"+themeID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AchievementStars existingAchievements = dataSnapshot.getValue(AchievementStars.class);
                if (existingAchievements == null){
                    existingAchievements = new AchievementStars();
                    existingAchievements.setFirstInstance(false);
                    existingAchievements.setSecondInstance(false);
                    existingAchievements.setRepeatInstance(false);
                }

                ((Results)context).populateExistingStars(existingAchievements);


                identifyNewAchievements(existingAchievements);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void identifyNewAchievements(AchievementStars existingAchievements){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = db.getReference("instanceRecords/"+userID+"/"+themeID);
        final AchievementStars finalExistingAchievements = existingAchievements;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AchievementStars newAchievements = new AchievementStars();
                newAchievements.setFirstInstance(false);
                newAchievements.setSecondInstance(false);
                newAchievements.setRepeatInstance(false);
                boolean shouldSend = false;
                //first check if this is the first instance.
                //we do this first to avoid calling get children on a null snapshot
                if (dataSnapshot.getValue() == null){
                    //there are no values so this has to be th first instance
                    newAchievements.setFirstInstance(true);
                    shouldSend = true;
                } else {
                    //check if this is a repeat of the same instance
                    DataSnapshot sameInstance = dataSnapshot.child(instanceRecord.getInstanceId());
                    if (sameInstance.getChildrenCount() == 1){
                        //one record with the same instance exists
                        newAchievements.setRepeatInstance(true);
                        shouldSend = true;
                    } else if (sameInstance.getChildrenCount() == 0){
                        //repeat and second (new) instance are mutually exclusive.
                        if (dataSnapshot.getChildrenCount() == 1){
                            newAchievements.setSecondInstance(true);
                            shouldSend = true;
                        }
                    }
                }

                if (shouldSend){
                    ((Results)context).populateNewStars(newAchievements);
                    updateAchievements(finalExistingAchievements, newAchievements);
                }

                //we should save the new instance record after all this so it doesn't
                // interfere with identifying achievements.
                //note this doesn't have to be synchronous with updateAchievements()
                saveInstanceRecord();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = db.getReference("achievements/"+userID+"/"+themeID);
        //overwrite/create achievements
        ref.setValue(combinedAchievements);
    }

    private void saveInstanceRecord(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = db.getReference(
                "instanceRecords/"+userID+"/"+themeID+"/"+instanceRecord.getInstanceId());
        String key = ref.push().getKey();
        instanceRecord.setId(key);
        DatabaseReference ref2 = db.getReference(
                "instanceRecords/"+userID+"/"+themeID+"/"+instanceRecord.getInstanceId()+
                "/"+key);
        ref2.setValue(instanceRecord);
    }

}
