package com.linnca.pelicann.results;

import android.content.Context;
import android.util.Log;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.lessonlist.LessonListViewer;
import com.linnca.pelicann.lessonlist.LessonListViewerImplementation;
import com.linnca.pelicann.lessonlist.UserLessonListViewer;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//this manages the results displayed to the user
class ResultsManager {
    private final Database db;
    private final InstanceRecord instanceRecord;
    private final List<String> questionIDs;
    private final ResultsManagerListener resultsManagerListener;
    private UserLessonListViewer userLessonListViewer = null;

    interface ResultsManagerListener {
        void onLessonFirstCleared(UserLessonListViewer previousState);
        void onLessonCleared(boolean cleared);
        void onAddDailyLessonCt(int oldCt, int newCt);
    }

    ResultsManager(InstanceRecord instanceRecord, List<String> questionIDs, Database db, ResultsManagerListener listener){
        this.instanceRecord = instanceRecord;
        this.questionIDs = new ArrayList<>(questionIDs);
        this.resultsManagerListener = listener;
        this.db = db;
    }

    void saveInstanceRecord(Context context){
        //save the whole instance record
        final OnDBResultListener instanceRecordOnDBResultListener = new OnDBResultListener() {
            @Override
            public void onInstanceRecordAdded(String generatedRecordKey) {
                super.onInstanceRecordAdded(generatedRecordKey);
            }
        };
        db.addInstanceRecord(instanceRecord, instanceRecordOnDBResultListener);

        //save correct count for displaying the report card
        LessonListViewer lessonListViewer = new LessonListViewerImplementation();
        String lessonKey = instanceRecord.getLessonId();
        int level = lessonListViewer.getLessonLevel(lessonKey);
        final int[] correctCt = calculateCorrectCount(instanceRecord.getAttempts());
        OnDBResultListener reportCardOnDBResultListener = new OnDBResultListener() {
            @Override
            public void onReportCardAdded() {
                super.onReportCardAdded();
            }
        };
        db.addReportCard(level, lessonKey, correctCt[0], correctCt[1], reportCardOnDBResultListener);

        //save the questions for review if the questions are in the newest section of questions.
        //we also add the cleared lesson separately, but that should never affect
        //whether we should add the questions to the review question stock because
        //the only lesson that would affect it is the review lesson, but
        // we don't update the review lessons here
        OnDBResultListener clearedLessonOnDBResultListener = new OnDBResultListener() {
            @Override
            public void onClearedLessonsQueried(Set<String> clearedLessonKeys) {
                OnDBResultListener reviewQuestionOnDBResultListener =
                        new OnDBResultListener() {
                            @Override
                            public void onReviewQuestionsAdded() {
                                super.onReviewQuestionsAdded();
                            }
                        };
                //save it in a class variable so other methods may use it
                userLessonListViewer = new UserLessonListViewer(new LessonListViewerImplementation(),
                        clearedLessonKeys);

                if (userLessonListViewer.shouldSaveForReview(instanceRecord.getLessonId())){
                    db.addReviewQuestion(questionIDs, reviewQuestionOnDBResultListener);
                }
            }
        };
        //we grabbed level when adding the report card
        db.getClearedLessons(context, level, false, clearedLessonOnDBResultListener);
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

    void clearLesson(Context context){
        //first check if the user's score is over the to-clear line
        int[] correctCt = calculateCorrectCount(instanceRecord.getAttempts());
        double correctPercentage = 100 * correctCt[0] / correctCt[1];
        LessonListViewer lessonListViewer = new LessonListViewerImplementation();
        String lessonKey = instanceRecord.getLessonId();
        int lessonClearScore = lessonListViewer.getLessonData(lessonKey)
                .getToClearScore();
        resultsManagerListener.onLessonCleared(correctPercentage >= lessonClearScore);
        //don't need to save the cleared lesson
        // if the user didn't clear the lesson
        if (correctPercentage < lessonClearScore){
            return;
        }

        //we create an instance of UserLessonListViewer when we
        // save review questions.
        //if that has run already, just use that.
        // if not, fetch it here
        if (userLessonListViewer != null){
            clearLessonHelperMethod(userLessonListViewer);
        } else {
            OnDBResultListener onDBResultListener = new OnDBResultListener() {
                @Override
                public void onClearedLessonsQueried(Set<String> clearedLessonKeys) {
                    UserLessonListViewer newUserLessonListViewer = new UserLessonListViewer(
                            new LessonListViewerImplementation(), clearedLessonKeys);
                    clearLessonHelperMethod(newUserLessonListViewer);
                }
            };

            int level = lessonListViewer.getLessonLevel(lessonKey);
            db.getClearedLessons(context, level, false, onDBResultListener);
        }

    }

    private void clearLessonHelperMethod(final UserLessonListViewer previousList){
        LessonListViewer lessonListViewer = new LessonListViewerImplementation();
        String lessonKey = instanceRecord.getLessonId();
        int level = lessonListViewer.getLessonLevel(lessonKey);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onClearedLessonAdded(boolean firstTimeCleared) {
                if(firstTimeCleared){
                    resultsManagerListener.onLessonFirstCleared(previousList);
                }
            }
        };
        db.addClearedLesson(level, lessonKey, onDBResultListener);
    }

    void addDailyLessonCt(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onDailyLessonAdded(int newCt) {
                int oldCt = newCt - 1;
                resultsManagerListener.onAddDailyLessonCt(oldCt, newCt);
            }
        };
        db.addDailyLesson(formatDailyLessonCtDate(), onDBResultListener);
    }

    private String formatDailyLessonCtDate(){
        int month = DateTime.now().monthOfYear().get();
        int day = DateTime.now().dayOfMonth().get();
        return Integer.toString(month) + "-" + Integer.toString(day);
    }
}
