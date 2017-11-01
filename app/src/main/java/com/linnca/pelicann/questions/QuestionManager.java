package com.linnca.pelicann.questions;


import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.db.FirebaseDBHeaders;
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
	private FirebaseDatabase db;
	private boolean questionsStarted = false;
	private boolean reviewStarted = false;
	private LessonInstanceData lessonInstanceData = null;
	private String lessonKey = null;
	private QuestionData currentQuestionData;
	private QuestionManagerListener questionManagerListener;
	private int questionMkr = 0;
	private int totalQuestions = 0;
	//store information about this instance of the instance.
	//a user can run this instance multiple times
	private InstanceRecord instanceRecord;
	//temp variables for each question attempt
	private long startTimestamp;
	private long endTimeStamp;

	//save the question data for the review.
	//we can fetch them again from the question ID, but this prevents another connection to FireBase
	//store in a set to prevent duplicates (we are adding every time we get a question attempt)
	private Set<QuestionData> missedQuestionsForReviewSet = new HashSet<>();
	private List<QuestionData> missedQuestionsForReviewList = new ArrayList<>();

	public interface QuestionManagerListener{
		void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion);
		void onQuestionsFinished(InstanceRecord instanceRecord);
        void onReviewFinished();
	}

	public QuestionManager(QuestionManagerListener listener){
		this.questionManagerListener = listener;
		this.db = FirebaseDatabase.getInstance();
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

	public boolean isQuestionsStarted(){
        return questionsStarted;
    }

	public void startReview(InstanceRecord instanceRecord){
		if (!reviewStarted){
			reviewStarted = true;
			questionsStarted = false;//just to make sure
			this.instanceRecord = instanceRecord;
			totalQuestions = missedQuestionsForReviewSet.size();
			//make it easier to loop through
			missedQuestionsForReviewList = new ArrayList<>(missedQuestionsForReviewSet);
			nextQuestion(true);
		}
	}

	public boolean isReviewStarted(){
        return reviewStarted;
    }

    //we need to know whether this is the first question
	//so we can put the previous fragment on the back stack
	public void nextQuestion(final boolean isFirstQuestion){
		//don't do anything if we haven't started anything
		if (!questionsStarted && !reviewStarted){
			return;
		}

		//for normal questions
		if (questionsStarted) {
			//if we are done with the questions
			if (questionMkr == lessonInstanceData.questionCount()) {
				instanceRecord.setCompleted(true);
				questionManagerListener.onQuestionsFinished(instanceRecord);
				//make sure to call this last because this resets the instance record
				resetManager(QUESTIONS);
				return;
			}
			//next question
			String questionID = lessonInstanceData.getQuestionIdAt(questionMkr);
			DatabaseReference questionRef = db.getReference(
					FirebaseDBHeaders.QUESTIONS + "/" +
							questionID
			);
			questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					currentQuestionData = dataSnapshot.getValue(QuestionData.class);
					questionManagerListener.onNextQuestion(currentQuestionData, questionMkr+1, totalQuestions, isFirstQuestion);
					questionMkr++;
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					Log.d(TAG, "Error" + databaseError.getMessage());
				}
			});
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
		} /*else {
			//somehow we don't have the questions stored.
			//the instance record still has a record of the questions attempts
			//so use that
			boolean foundNextQuestion = false;
			List<QuestionAttempt> attempts = instanceRecord.getAttempts();
			for (int i=questionMkr; i<attempts.size(); i++){
				QuestionAttempt attempt = attempts.get(questionMkr);
				if (!attempt.getCorrect() && currentQuestionData == null ||
						!attempt.getCorrect() && !attempt.getQuestionID().equals(currentQuestionData.getId())){
					foundNextQuestion = true;
					String questionID = attempt.getQuestionID();
					DatabaseReference questionRef = db.getReference(
							FirebaseDBHeaders.QUESTIONS + "/" +
									questionID
					);
					//we don't just increment the question marker because
					//we are skipping correct questions
					final int newQuestionMkr = i+1;
					questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							currentQuestionData = dataSnapshot.getValue(QuestionData.class);
							questionManagerListener.onNextQuestion(currentQuestionData, false);
							questionMkr = newQuestionMkr;
						}

						@Override
						public void onCancelled(DatabaseError databaseError) {

						}
					});
					break;
				}
			}

			if (!foundNextQuestion){
				//that means the user is finished with the review
				resetManager(REVIEW);
                questionManagerListener.onReviewFinished();
			}
		}*/

	}

	public void saveResponse(String response, Boolean correct){
		if (reviewStarted){
			//don't save anything if this is a review
			return;
		}
		String questionID = currentQuestionData.getId();
		List<QuestionAttempt> attempts = instanceRecord.getAttempts();
		int attemptNumber;
		//first attempt at first question
		if (attempts.size() == 0)
			attemptNumber = 1;
		else {
			QuestionAttempt lastAttempt = attempts.get(attempts.size() - 1);
			if (questionID.equals(lastAttempt.getQuestionID())){
				//same question so an attempt at the same question
				attemptNumber = lastAttempt.getAttemptNumber() + 1;
			} else {
				//new question
				attemptNumber = 1;
			}
		}
		endTimeStamp = System.currentTimeMillis();
		QuestionAttempt attempt = new QuestionAttempt(
				attemptNumber,questionID,response,correct,
				startTimestamp,endTimeStamp);

		attempts.add(attempt);

		//this should be the new start time for the next question
		startTimestamp = System.currentTimeMillis();

		//for when the user reviews
		if (!correct){
			//the user may have multiple question attempts per question.
			//the set prevents duplicate questions
			missedQuestionsForReviewSet.add(currentQuestionData);
		}

	}

	public String getAnswer(){
		return currentQuestionData.getAnswer();
	}

	private void startNewInstanceRecord(){
		instanceRecord = new InstanceRecord();
		instanceRecord.setCompleted(false);
		instanceRecord.setInstanceId(lessonInstanceData.getId());
		instanceRecord.setLessonId(lessonKey);
		instanceRecord.setAttempts(new ArrayList<QuestionAttempt>());
		startTimestamp = System.currentTimeMillis();
		String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference(
				FirebaseDBHeaders.INSTANCE_RECORDS + "/" + userID + "/" +
				"" + "/" + instanceRecord.getInstanceId());
		String key = ref.push().getKey();
		instanceRecord.setId(key);
	}


	public void resetManager(int identifier){
		//do for both review and normal run
		questionMkr = 0;
		totalQuestions = 0;
		lessonInstanceData = null;
		currentQuestionData = null;
		instanceRecord = null;
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
