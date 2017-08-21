package com.linnca.pelicann.questionmanager;


import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.db.database2classmappings.QuestionTypeMappings;
import com.linnca.pelicann.db.datawrappers.InstanceRecord;
import com.linnca.pelicann.db.datawrappers.LessonInstanceData;
import com.linnca.pelicann.db.datawrappers.QuestionAttempt;
import com.linnca.pelicann.db.datawrappers.QuestionData;
import com.linnca.pelicann.gui.Question_FillInBlank_Input;
import com.linnca.pelicann.gui.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.gui.Question_MultipleChoice;
import com.linnca.pelicann.gui.Question_Puzzle_Piece;
import com.linnca.pelicann.gui.Question_TrueFalse;
import com.linnca.pelicann.gui.Results;

import java.util.ArrayList;
import java.util.List;

//manages the execution of questions.
//this means that any new instances will be generated before calling this class

public class QuestionManager{
	private FirebaseDatabase db;
	private Boolean started = false;
	private LessonInstanceData lessonInstanceData = null;
	private QuestionData currentQuestionData;
	private QuestionManagerListener questionManagerListener;
	//-1 so first call of nextQuestion() would be 0
	private int questionMkr = 0;
	//store information about this instance of the instance.
	//a user can run this instance multiple times
	private InstanceRecord instanceRecord;
	//temp variables for each question attempt
	private long startTimestamp;
	private long endTimeStamp;

	public interface QuestionManagerListener{
		void onNextQuestion(QuestionData questionData);
		void onQuestionsFinished(InstanceRecord instanceRecord);
	}

	public QuestionManager(QuestionManagerListener listener){
		this.questionManagerListener = listener;
		this.db = FirebaseDatabase.getInstance();
	}

	public void startQuestions(LessonInstanceData data){
		if(!started) {
			started = true;
			this.lessonInstanceData = data;
			startNewInstanceRecord();
			nextQuestion();
		}
	}

	public void nextQuestion(){
		//don't do anything if we haven't started
		if (!started){
			return;
		}
		//if we are done with the questions
		if (questionMkr == lessonInstanceData.questionCount()){
			instanceRecord.setCompleted(true);
			questionManagerListener.onQuestionsFinished(instanceRecord);
			resetManager();
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
				questionManagerListener.onNextQuestion(currentQuestionData);
				questionMkr++;
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	public void saveResponse(String response, Boolean correct){
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

	}

	public String getAnswer(){
		return currentQuestionData.getAnswer();
	}

	private void startNewInstanceRecord(){
		instanceRecord = new InstanceRecord();
		instanceRecord.setCompleted(false);
		//instanceRecord.setInstanceId();
		//instanceRecord.setLessonId(instanceData.getThemeId());
		//not sure if we can instantiate in the question attempt class?
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


	private void resetManager(){
		started = false;
		questionMkr = 0;
		lessonInstanceData = null;
		currentQuestionData = null;
		instanceRecord = null;
	}


}
