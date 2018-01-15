package pelicann.linnca.com.corefunctionality.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;

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

    public void startReview(final NetworkConnectionChecker networkConnectionChecker, String reviewID){
        if (reviewStarted)
            return;

        this.lessonLevel = LessonData.extractReviewLevel(reviewID);
        this.reviewID = reviewID;
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onReviewQuestionsQueried(List<String> questionKeys) {
                questionIDs = selectQuestions(questionKeys);
                totalQuestions = questionIDs.size();
                reviewStarted = true;
                nextQuestion(networkConnectionChecker, true);
            }
        };
        db.getReviewQuestions(onDBResultListener);
    }

    public boolean reviewStarted(){
        return reviewStarted;
    }

    public void nextQuestion(NetworkConnectionChecker networkConnectionChecker, final boolean isFirstQuestion){
        if (!reviewStarted)
            return;

        if (questionMkr == totalQuestions){
            lessonReviewManagerListener.onReviewFinished(lessonLevel, reviewID);
            reviewStarted = false;
            return;
        }

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionQueried(QuestionData questionData) {
                lessonReviewManagerListener.onNextQuestion(questionData, questionMkr+1, totalQuestions, isFirstQuestion);
                questionMkr++;
            }
        };
        db.getQuestion(networkConnectionChecker, questionIDs.get(questionMkr), onDBResultListener);
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
