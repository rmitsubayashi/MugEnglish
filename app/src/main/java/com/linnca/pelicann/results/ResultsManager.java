package com.linnca.pelicann.results;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessonlist.LessonListViewer;
import com.linnca.pelicann.lessonlist.LessonListViewerImplementation;
import com.linnca.pelicann.lessonlist.UserLessonListViewer;
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
    private UserLessonListViewer userLessonListViewer = null;

    interface ResultsManagerListener {
        void onLessonCleared(UserLessonListViewer previousState);
    }

    ResultsManager(InstanceRecord instanceRecord, List<String> questionIDs, Database db, ResultsManagerListener listener){
        this.instanceRecord = instanceRecord;
        this.questionIDs = new ArrayList<>(questionIDs);
        this.resultsManagerListener = listener;
        this.db = db;
    }

    void saveInstanceRecord(){
        //save the whole instance record
        final OnResultListener instanceRecordOnResultListener = new OnResultListener() {
            @Override
            public void onInstanceRecordAdded(String generatedRecordKey) {
                super.onInstanceRecordAdded(generatedRecordKey);
            }
        };
        db.addInstanceRecord(instanceRecord, instanceRecordOnResultListener);

        //save correct count for displaying the report card
        LessonListViewer lessonListViewer = new LessonListViewerImplementation();
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
                OnResultListener reviewQuestionOnResultListener =
                        new OnResultListener() {
                            @Override
                            public void onReviewQuestionsAdded() {
                                super.onReviewQuestionsAdded();
                            }
                        };
                //save it in a class variable so other methods may use it
                userLessonListViewer = new UserLessonListViewer(new LessonListViewerImplementation(),
                        clearedLessonKeys);
                if (userLessonListViewer.shouldSaveForReview(instanceRecord.getLessonId())){
                    db.addReviewQuestion(questionIDs, reviewQuestionOnResultListener);
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
        //we create an instance of UserLessonListViewer when we
        // save review questions.
        //if that has run already, just use that.
        // if not, fetch it here
        if (userLessonListViewer != null){
            clearLessonHelperMethod(userLessonListViewer);
        } else {
            OnResultListener onResultListener = new OnResultListener() {
                @Override
                public void onClearedLessonsQueried(Set<String> clearedLessonKeys) {
                    UserLessonListViewer newUserLessonListViewer = new UserLessonListViewer(
                            new LessonListViewerImplementation(), clearedLessonKeys);
                    clearLessonHelperMethod(newUserLessonListViewer);
                }
            };
            LessonListViewer lessonListViewer = new LessonListViewerImplementation();
            String lessonKey = instanceRecord.getLessonId();
            int level = lessonListViewer.getLessonLevel(lessonKey);
            db.getClearedLessons(level, false, onResultListener);
        }

    }

    private void clearLessonHelperMethod(final UserLessonListViewer previousList){
        LessonListViewer lessonListViewer = new LessonListViewerImplementation();
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
