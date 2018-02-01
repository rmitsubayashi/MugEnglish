package pelicann.linnca.com.corefunctionality.lessonquestions;


import java.util.ArrayList;
import java.util.List;

//for the review right after answering questions
// (re-answering wrong questions)
public class InstanceReviewManager {
    private List<QuestionData> questions = new ArrayList<>();
    private int questionMkr = 0;
    private int totalQuestions;
    private boolean reviewStarted = false;
    private InstanceReviewManagerListener instanceReviewManagerListener;

    public interface InstanceReviewManagerListener {
        void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion);
        void onReviewFinished();
    }

    //since this will always be called after answering questions,
    // we can manage the missed questions in the question manager
    // and initialize the review manager with the data
    public InstanceReviewManager(InstanceReviewManagerListener listener){
        this.instanceReviewManagerListener = listener;
    }
    
    public void setQuestions(List<QuestionData> questions){
        this.questions = questions;
        this.totalQuestions = questions.size();
    }

    public void startReview(){
        if (!reviewStarted && questions.size() != 0) {
            reviewStarted = true;
            nextQuestion(true);
        }
    }

    public boolean reviewStarted(){
        return reviewStarted;
    }

    public void resetCurrentQuestionIndex(){
        questionMkr = 0;
        reviewStarted = false;
    }

    public void nextQuestion(boolean isFirstQuestion){
        //review
        if (questionMkr == questions.size()){
            instanceReviewManagerListener.onReviewFinished();
            //the user might press back and want to redo the review,
            //so don't clear the data
            resetCurrentQuestionIndex();
            return;
        }

        QuestionData currentQuestionData = questions.get(questionMkr);
        instanceReviewManagerListener.onNextQuestion(currentQuestionData, questionMkr+1, totalQuestions, isFirstQuestion);
        questionMkr++;
    }

    public void resetManager(){
        questions.clear();
        questionMkr = 0;
        totalQuestions = 0;
        reviewStarted = false;
    }


}
