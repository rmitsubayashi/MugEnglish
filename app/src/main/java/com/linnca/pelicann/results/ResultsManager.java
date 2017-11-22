package com.linnca.pelicann.results;

import android.util.Log;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessonlist.LessonListViewer;
import com.linnca.pelicann.lessonlist.UserLessonList;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//this manages the results displayed to the user
class ResultsManager {
    private final Database db;
    private final InstanceRecord instanceRecord;
    private final List<String> questionIDs;
    private final ResultsManagerListener resultsManagerListener;
    private UserLessonList userLessonList = null;

    interface ResultsManagerListener {
        void onLessonCleared(UserLessonList previousState);
    }

    ResultsManager(InstanceRecord instanceRecord, List<String> questionIDs, Database db, ResultsManagerListener listener){
        this.instanceRecord = instanceRecord;
        this.questionIDs = new ArrayList<>(questionIDs);
        this.resultsManagerListener = listener;
        this.db = db;
    }

    void saveInstanceRecord(){
        Log.d("resultsManager","Called saveInstanceRecord");
        //save the whole instance record
        final OnResultListener instanceRecordOnResultListener = new OnResultListener() {
            @Override
            public void onInstanceRecordAdded(String generatedRecordKey) {
                super.onInstanceRecordAdded(generatedRecordKey);
            }
        };
        db.addInstanceRecord(instanceRecord, instanceRecordOnResultListener);

        //save correct count for displaying the report card
        LessonListViewer lessonListViewer = new LessonListViewer();
        String lessonKey = instanceRecord.getLessonId();
        int level = lessonListViewer.getLessonLevel(lessonKey);
        final int[] correctCt = calculateCorrectCount(instanceRecord.getAttempts());
        OnResultListener reportCartOnResultListener = new OnResultListener() {
            @Override
            public void onReportCardAdded() {
                super.onReportCardAdded();
            }
        };
        db.addReportCard(level, lessonKey, correctCt[0], correctCt[1], reportCartOnResultListener);

        //save the questions for review if the questions are in the newest section of questions.
        //we also add the cleared lesson separately, but that should never affect
        //whether we should add the questions to the review question stock because
        //the only lesson that would affect it is the review lesson, but
        // we don't update the review lessons here
        OnResultListener clearedLessonOnResultListener = new OnResultListener() {
            @Override
            public void onClearedLessonsQueried(Set<String> clearedLessonKeys) {
                Log.d("resultsManager","called clearedLessonOnResultListener");
                OnResultListener reviewQuestionOnResultListener =
                        new OnResultListener() {
                            @Override
                            public void onReviewQuestionsAdded() {
                                super.onReviewQuestionsAdded();
                            }
                        };
                //save it in a class variable so other methods may use it
                userLessonList = new UserLessonList(clearedLessonKeys);
                if (userLessonList.shouldSaveForReview(instanceRecord.getLessonId())){
                    Log.d("resultsManager", "should save review questions");
                    db.addReviewQuestion(questionIDs, reviewQuestionOnResultListener);
                } else {
                    Log.d("resultsManager", "shouldn't save review questions");
                }
            }
        };
        //we grabbed level when adding the report card
        db.getClearedLessons(level, false, clearedLessonOnResultListener);
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

    void clearLesson(){
        //we create an instance of UserLessonList when we
        // save review questions.
        //if that has run already, just use that.
        // if not, fetch it here
        if (userLessonList != null){
            clearLessonHelperMethod(userLessonList);
        } else {
            OnResultListener onResultListener = new OnResultListener() {
                @Override
                public void onClearedLessonsQueried(Set<String> clearedLessonKeys) {
                    UserLessonList newUserLessonList = new UserLessonList(clearedLessonKeys);
                    clearLessonHelperMethod(newUserLessonList);
                }
            };
            LessonListViewer lessonListViewer = new LessonListViewer();
            String lessonKey = instanceRecord.getLessonId();
            int level = lessonListViewer.getLessonLevel(lessonKey);
            db.getClearedLessons(level, false, onResultListener);
        }

    }

    private void clearLessonHelperMethod(final UserLessonList previousList){
        LessonListViewer lessonListViewer = new LessonListViewer();
        String lessonKey = instanceRecord.getLessonId();
        int level = lessonListViewer.getLessonLevel(lessonKey);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onClearedLessonAdded(boolean firstTimeCleared) {
                if(firstTimeCleared){
                    resultsManagerListener.onLessonCleared(previousList);
                }
            }
        };
        db.addClearedLesson(level, lessonKey, onResultListener);
    }
}
