package com.linnca.pelicann.results;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessonlist.LessonHierarchyViewer;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import java.util.List;

//this manages the results displayed to the user
class ResultsManager {
    private final FirebaseDatabase db;
    private final String userID;
    private final InstanceRecord instanceRecord;
    private final ResultsManagerListener resultsManagerListener;

    interface ResultsManagerListener {
        void onLessonCleared();
    }

    ResultsManager(InstanceRecord instanceRecord, ResultsManagerListener listener){
        db = FirebaseDatabase.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.instanceRecord = instanceRecord;
        this.resultsManagerListener = listener;
    }

    void saveInstanceRecord(){
        DatabaseReference instanceRecordRef = db.getReference(
                FirebaseDBHeaders.INSTANCE_RECORDS + "/" + userID + "/" +
                instanceRecord.getLessonId() + "/" + instanceRecord.getInstanceId() +"/" + instanceRecord.getId());
        instanceRecordRef.setValue(instanceRecord);
        
        final int[] correctCt = calculateCorrectCount(instanceRecord.getAttempts());
        final DatabaseReference correctCtRef = db.getReference(
                FirebaseDBHeaders.REPORT_CARD + "/" +
                userID + "/" +
                instanceRecord.getLessonId() + "/" +
                FirebaseDBHeaders.REPORT_CARD_CORRECT
        );
        correctCtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentVal = dataSnapshot.getValue(Long.class);
                if (currentVal == null){
                    currentVal = 0L;
                }
                long newVal = currentVal + correctCt[0];
                correctCtRef.setValue(newVal);
                
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final DatabaseReference totalCtRef = db.getReference(
                FirebaseDBHeaders.REPORT_CARD + "/" +
                        userID + "/" +
                        instanceRecord.getLessonId() + "/" +
                        FirebaseDBHeaders.REPORT_CARD_TOTAL
        );
        totalCtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentVal = dataSnapshot.getValue(Long.class);
                if (currentVal == null){
                    currentVal = 0L;
                }
                long newVal = currentVal + correctCt[1];
                totalCtRef.setValue(newVal);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static int[] calculateCorrectCount(List<QuestionAttempt> attempts){
        String tempQuestionID = "";
        int correctCt = 0;
        int totalCt = 0;
        for (QuestionAttempt attempt : attempts){
            String questionID = attempt.getQuestionID();
            //there can only be one correct answer per question.
            // (there can be multiple incorrect answers)
            if (attempt.getCorrect())
                correctCt++;
            if (!tempQuestionID.equals(questionID)){
                totalCt++;
                tempQuestionID = questionID;
            }
        }

        return new int[]{correctCt, totalCt};
    }

    void checkLessonCleared(){
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer();
        String lessonKey = instanceRecord.getLessonId();
        int level = lessonHierarchyViewer.getLessonLevel(lessonKey);
        final DatabaseReference ref = db.getReference(
                FirebaseDBHeaders.CLEARED_LESSONS + "/" +
                        userID + "/" +
                        Integer.toString(level) + "/" +
                        lessonKey
        );
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    ref.setValue(true);
                    resultsManagerListener.onLessonCleared();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
