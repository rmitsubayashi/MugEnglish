package com.example.ryomi.mugenglish.questionmanager;

import android.content.Context;

import com.example.ryomi.mugenglish.db.FirebaseDBHeaders;
import com.example.ryomi.mugenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.mugenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionAttempt;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.gui.Results;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//this manages the results displayed to the user
public class ResultsManager {
    private Context context;
    private InstanceRecord instanceRecord;
    private List<QuestionData> questions;
    //we can find this from the instance ID of instance record
    //(instanceID -> instance -> themeID)
    //but that's one more connection we will have to send
    private String themeID;

    public ResultsManager(InstanceRecord instanceRecord, List<QuestionData> questions, String themeID){
        this.instanceRecord = instanceRecord;
        this.questions = new ArrayList<>(questions);
        this.themeID = themeID;
    }

    //save and display results
    public void displayResults(Context context){
        this.context = context;
        saveInstanceRecord();
        identifyAchievements();
        //showQuestionRecord();
        showQuestionsCorrect();
    }

    //since we need to do this synchronously
    //the calls to firebase are nested.
    //1. look at the achievements for the user
    //2. look at past instance records to identify what new stars should be added
    private void identifyAchievements(){
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

                identifyNewAchievements(existingAchievements);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //we are only passing the existing achievements so we can compare the old and new set of achievements.
    //identification should not be different whether this instance record is updated prior or after
    private void identifyNewAchievements(AchievementStars existingAchievements){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = db.getReference("instanceRecords/"+userID+"/"+themeID);
        final AchievementStars finalExistingAchievements = existingAchievements;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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

                if (shouldUpdateStars(finalExistingAchievements, newAchievements)){
                    ((Results)context).populateNewStars(finalExistingAchievements, newAchievements);
                    updateAchievements(finalExistingAchievements, newAchievements);
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
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" + userID + "/" +
                themeID + "/" + instanceRecord.getInstanceId() +"/" + instanceRecord.getId());
        ref.setValue(instanceRecord);
    }

    /*
    private void showQuestionRecord(){
        //organize question data into a map for easier retrieval
        Map<String, QuestionData> questionMap = new HashMap<>();
        for (QuestionData data : questions){
            questionMap.put(data.getId(), data);
        }

        List<QuestionAttempt> attempts = instanceRecord.getAttempts();
        for (QuestionAttempt attempt : attempts){
            QuestionData questionData = questionMap.get(attempt.getQuestionID());
            //shouldn't happen
            if (questionData == null){
                continue;
            }
            ((Results)context).addQuestion(questionData.getQuestion());
            ((Results)context).addResponse(attempt.getResponse(),attempt.getCorrect());
        }
    }*/

    private void showQuestionsCorrect(){
        List<QuestionAttempt> attempts = instanceRecord.getAttempts();
        String tempQuestionID = "";
        int totalQuestions = 0;
        int correctQuestions = 0;
        for (QuestionAttempt attempt : attempts){
            String questionID = attempt.getQuestionID();
            if (attempt.getCorrect())
                correctQuestions++;
            if (!tempQuestionID.equals(questionID)){
                totalQuestions++;
                tempQuestionID = questionID;
            }
        }

        ((Results)context).populateCorrectCount(correctQuestions, totalQuestions);
    }

}
