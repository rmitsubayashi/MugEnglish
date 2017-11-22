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
	private Database db;
	private boolean questionsStarted = false;
	private LessonInstanceData lessonInstanceData = null;
	private String lessonKey = null;
	private QuestionData currentQuestionData;
	private QuestionManagerListener questionManagerListener;
	private int questionMkr = 0;
	private int totalQuestions = 0;
	//store information about this run of the instance.
	//a user can run an instance multiple times,
	// getting multiple records for an instance
	private InstanceRecordManager instanceRecordManager;

	//save the missed questions for the instance review.
	//we can fetch them again from the question ID, but this prevents another connection to the database.
	//store in a set to prevent duplicates (we are adding every time we get a question attempt)
	private Set<QuestionData> missedQuestionsForReview = new HashSet<>();

	public interface QuestionManagerListener{
		void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion);
		//arrayList so we can easily save it in a bundle
		void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs,
								 List<QuestionData> missedQuestions);
	}

	public QuestionManager(Database db, QuestionManagerListener listener){
		this.db = db;
		this.questionManagerListener = listener;
	}

	public void startQuestions(LessonInstanceData data, String lessonKey){
		if(!questionsStarted) {
			questionsStarted = true;
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

    //we need to know whether this is the first question
	//so we can put the previous fragment on the back stack
	public void nextQuestion(final boolean isFirstQuestion){
		//don't do anything if we haven't started anything
		if (!questionsStarted){
			return;
		}
		instanceRecordManager.setQuestionAttemptStartTimestamp();

		//if we are done with the questions
		if (questionMkr == lessonInstanceData.questionCount()) {
			instanceRecordManager.markInstanceCompleted();
			questionManagerListener.onQuestionsFinished(instanceRecordManager.getInstanceRecord(),
					new ArrayList<>(lessonInstanceData.getQuestionIds()),
					new ArrayList<>(missedQuestionsForReview));
			//the user will not be able to go back and redo this question again,
			// so we can reset everything
			resetManager();
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

	public void saveResponse(String response, Boolean correct){
		instanceRecordManager.addQuestionAttempt(currentQuestionData.getId(), response, correct);

		//save incorrect responses for when the user reviews
		if (!correct){
			//the user may have multiple question attempts per question.
			//the set prevents duplicate questions
			missedQuestionsForReview.add(currentQuestionData);
		}

	}

	private void startNewInstanceRecord(){
		instanceRecordManager = new InstanceRecordManager(lessonInstanceData.getId(),
				lessonKey);
	}

	public void resetManager(){
		questionsStarted = false;
		lessonInstanceData = null;
		lessonKey = null;
		currentQuestionData = null;
		questionMkr = 0;
		totalQuestions = 0;
		instanceRecordManager = null;
		missedQuestionsForReview.clear();
	}
}
