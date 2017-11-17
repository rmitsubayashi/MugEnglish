package com.linnca.pelicann.questions;


import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessondetails.LessonInstanceData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//manages the execution of questions.
//this means that any new instances will be generated before calling this class

public class QuestionManager{
	private final String TAG = "QuestionManager";
	public static final int QUESTIONS = 1;
	public static final int REVIEW = 2;
	private Database db;
	private boolean questionsStarted = false;
	private boolean reviewStarted = false;
	private LessonInstanceData lessonInstanceData = null;
	private String lessonKey = null;
	private QuestionData currentQuestionData;
	private QuestionManagerListener questionManagerListener;
	private int questionMkr = 0;
	private int totalQuestions = 0;
	//store information about this run of the instance.
	//a user can run an instance multiple times,
	// getting multiple records
	private InstanceRecordManager instanceRecordManager;

	//save the missed questions for the review.
	//we can fetch them again from the question ID, but this prevents another connection to the database.
	//store in a set to prevent duplicates (we are adding every time we get a question attempt)
	private Set<QuestionData> missedQuestionsForReviewSet = new HashSet<>();
	private List<QuestionData> missedQuestionsForReviewList = new ArrayList<>();

	public interface QuestionManagerListener{
		void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion);
		void onQuestionsFinished(InstanceRecord instanceRecord);
        void onReviewFinished();
	}

	public QuestionManager(Database db, QuestionManagerListener listener){
		this.db = db;
		this.questionManagerListener = listener;
	}

	public void startQuestions(LessonInstanceData data, String lessonKey){
		if(!questionsStarted) {
			questionsStarted = true;
			reviewStarted = false;//just to make sure
			this.lessonInstanceData = data;
			totalQuestions = lessonInstanceData.questionCount();
			this.lessonKey = lessonKey;
			startNewInstanceRecord();
			nextQuestion(true);
		}
	}

	public boolean questionsStarted(){
        return questionsStarted;
    }

	public void startReview(InstanceRecord instanceRecord){
		if (!reviewStarted){
			reviewStarted = true;
			questionsStarted = false;//just to make sure
			instanceRecordManager = new InstanceRecordManager(instanceRecord);
			totalQuestions = missedQuestionsForReviewSet.size();
			//make it easier to loop through
			missedQuestionsForReviewList = new ArrayList<>(missedQuestionsForReviewSet);
			nextQuestion(true);
		}
	}

	public boolean reviewStarted(){
        return reviewStarted;
    }

    //we need to know whether this is the first question
	//so we can put the previous fragment on the back stack
	public void nextQuestion(final boolean isFirstQuestion){
		//don't do anything if we haven't started anything
		if (!questionsStarted && !reviewStarted){
			return;
		}

		instanceRecordManager.setQuestionAttemptStartTimestamp();

		//for normal questions
		if (questionsStarted) {
			//if we are done with the questions
			if (questionMkr == lessonInstanceData.questionCount()) {
				instanceRecordManager.markInstanceCompleted();
				questionManagerListener.onQuestionsFinished(instanceRecordManager.getInstanceRecord());
				//make sure to call this last because this resets the instance record
				resetManager(QUESTIONS);
				return;
			}
			//next question
			String questionID = lessonInstanceData.getQuestionIdAt(questionMkr);
			OnResultListener onResultListener = new OnResultListener() {
				@Override
				public void onQuestionQueried(QuestionData questionData) {
					currentQuestionData = questionData;
					questionManagerListener.onNextQuestion(currentQuestionData, questionMkr+1, totalQuestions, isFirstQuestion);
					questionMkr++;
				}
			};
			db.getQuestion(questionID, onResultListener);
		}
		//for review
		else {
			//review
			if (questionMkr == missedQuestionsForReviewList.size()){
				resetManager(REVIEW);
                questionManagerListener.onReviewFinished();
				return;
			}
			currentQuestionData = missedQuestionsForReviewList.get(questionMkr);
			questionManagerListener.onNextQuestion(currentQuestionData, questionMkr+1, totalQuestions, isFirstQuestion);
			questionMkr++;
		}
	}

	public void saveResponse(String response, Boolean correct){
		if (reviewStarted){
			//don't save anything if this is a review
			return;
		}

		instanceRecordManager.addQuestionAttempt(currentQuestionData.getId(), response, correct);

		//save incorrect responses for when the user reviews
		if (!correct){
			//the user may have multiple question attempts per question.
			//the set prevents duplicate questions
			missedQuestionsForReviewSet.add(currentQuestionData);
		}

	}

	private void startNewInstanceRecord(){
		instanceRecordManager = new InstanceRecordManager(lessonInstanceData.getId(),
				lessonKey);
	}


	public void resetManager(int identifier){
		//do for both review and normal run
		questionMkr = 0;
		totalQuestions = 0;
		lessonInstanceData = null;
		currentQuestionData = null;
		instanceRecordManager = null;
		if (identifier == QUESTIONS){
			questionsStarted = false;
		}
		if (identifier == REVIEW){
			reviewStarted = false;
			missedQuestionsForReviewSet.clear();
			missedQuestionsForReviewList.clear();
		}
	}

	public void resetReviewMarker(){
		questionMkr = 0;
		reviewStarted = false;
		//we are going to make a new list the next time the
		//user reviews
		missedQuestionsForReviewList.clear();
	}


}
