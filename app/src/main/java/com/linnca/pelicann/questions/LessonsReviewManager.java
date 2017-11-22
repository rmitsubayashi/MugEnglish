package com.linnca.pelicann.questions;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnResultListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//for the cumulative review after completing all the lessons
// up until the review
public class LessonsReviewManager {
    private Database db;
    private List<String> questionIDs = new ArrayList<>();
    private int questionMkr = 0;
    private int totalQuestions = 0;
    private int lessonLevel = 0;
    private String reviewID = null;
    private boolean reviewStarted = false;
    private LessonReviewManagerListener lessonReviewManagerListener;

    public interface LessonReviewManagerListener {
        void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion);
        void onReviewFinished(int lessonLevel, String reviewID);
    }

    public LessonsReviewManager(Database db, LessonReviewManagerListener listener){
        this.db = db;
        this.lessonReviewManagerListener = listener;
    }

    public void startReview(int lessonLevel, String reviewID){
        if (reviewStarted)
            return;

        this.lessonLevel = lessonLevel;
        this.reviewID = reviewID;
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onReviewQuestionsQueried(List<String> questionKeys) {
                questionIDs = selectQuestions(questionKeys);
                totalQuestions = questionIDs.size();
                reviewStarted = true;
                nextQuestion(true);
            }
        };
        db.getReviewQuestions(onResultListener);
    }

    public boolean reviewStarted(){
        return reviewStarted;
    }

    public void nextQuestion(final boolean isFirstQuestion){
        if (!reviewStarted)
            return;

        if (questionMkr == totalQuestions){
            lessonReviewManagerListener.onReviewFinished(lessonLevel, reviewID);
            reviewStarted = false;
            return;
        }

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionQueried(QuestionData questionData) {
                lessonReviewManagerListener.onNextQuestion(questionData, questionMkr+1, totalQuestions, isFirstQuestion);
                questionMkr++;
            }
        };
        db.getQuestion(questionIDs.get(questionMkr), onResultListener);
    }

    //for the lesson review, don't let the user finish until the user has correctly answered
    // all questions.
    public void returnQuestionToStack(){
        int previousQuestionMkr = questionMkr - 1;
        String previousQuestionID = questionIDs.get(previousQuestionMkr);
        questionIDs.remove(previousQuestionMkr);
        //add the question to the back of the stack
        questionIDs.add(previousQuestionID);
        //the total number of questions remains the same,
        // but the index should be shifted by 1
        // (question queried -> increment marker -> display question ->
        //  got it wrong -> decrement marker).
        questionMkr--;
    }

    private List<String> selectQuestions(List<String> questionIDs){
        //shuffle them and get random 20 questions.
        //every question should be mutually exclusively solvable
        Collections.shuffle(questionIDs);
        //we should generally have more than 20 questions,
        // but just in case
        int max = questionIDs.size() > 20 ?
                20 : questionIDs.size();
        return new ArrayList<>(questionIDs.subList(0, max));
    }
}
