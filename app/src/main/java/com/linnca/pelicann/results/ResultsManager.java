package com.linnca.pelicann.results;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessonlist.LessonHierarchyViewer;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import java.util.List;

//this manages the results displayed to the user
class ResultsManager {
    private final Database db = new FirebaseDB();
    private final InstanceRecord instanceRecord;
    private final ResultsManagerListener resultsManagerListener;

    interface ResultsManagerListener {
        void onLessonCleared();
    }

    ResultsManager(InstanceRecord instanceRecord, ResultsManagerListener listener){
        this.instanceRecord = instanceRecord;
        this.resultsManagerListener = listener;
    }

    void saveInstanceRecord(){
        OnResultListener instanceRecordOnResultListener = new OnResultListener() {
            @Override
            public void onInstanceRecordAdded(String generatedRecordKey) {
                super.onInstanceRecordAdded(generatedRecordKey);
            }
        };
        db.addInstanceRecord(instanceRecord, instanceRecordOnResultListener);

        //also save correct count for displaying the report card
        LessonHierarchyViewer lessonHierarchyViewer = new LessonHierarchyViewer();
        String lessonKey = instanceRecord.getLessonId();
        int level = lessonHierarchyViewer.getLessonLevel(lessonKey);
        final int[] correctCt = calculateCorrectCount(instanceRecord.getAttempts());
        OnResultListener reportCartOnResultListener = new OnResultListener() {
            @Override
            public void onReportCardAdded() {
                super.onReportCardAdded();
            }
        };
        db.addReportCard(level, lessonKey, correctCt[0], correctCt[1], reportCartOnResultListener);
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
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onClearedLessonAdded(boolean firstTimeCleared) {
                if(firstTimeCleared){
                    resultsManagerListener.onLessonCleared();
                }
            }
        };
        db.addClearedLesson(level, lessonKey, onResultListener);
    }
}
