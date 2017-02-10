package com.example.ryomi.myenglish.questionmanager;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.myenglish.db.datawrappers.QuestionAttempt;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.gui.Question_MultipleChoice;
import com.example.ryomi.myenglish.gui.Question_Puzzle_Piece;
import com.example.ryomi.myenglish.gui.Question_TrueFalse;
import com.example.ryomi.myenglish.gui.Results;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//manages the execution of questions.
//this means that any new instances will be generated before calling this class

//this class should be passed to each question activity
//but serializing seemed like a pain
//so making it a singleton.
//the only real demerit I see is harder to unit test?
public class QuestionManager{
	private static QuestionManager singleton;
	private Boolean started = false;
	private Context currentContext = null;
	//so we don't remove the theme details page
	private boolean canRemovePreviousActivity = false;
	private ThemeInstanceData instanceData = null;
	//we will fetch the question data on initialization
	private List<QuestionData> questionData = new ArrayList<>();
	//-1 so first call of nextQuestion() would be 0
	private int questionMkr = -1;
	//store information about this instance of the instance(?)
	//each topic has multiple instances which have different topics.
	//each of these instances can be run multiple times
	private InstanceRecord instanceRecord;
	//stores results.
	//this is needed to save data since we reset the question manager once the questions are finished
	private ResultsManager resultsManager = null;
	//temp variables for each question attempt
	private long startTimestamp;
	private long endTimeStamp;

	private QuestionManager(){
		//should not call without inserting instance information
	}

	public static QuestionManager getInstance(){
		if (singleton == null){
			singleton = new QuestionManager();
		}
		return singleton;
	}

	public void startQuestions(ThemeInstanceData data){
		if(!started && currentContext != null) {
			started = true;
			this.instanceData = data;
			initializeQuestions();
		}
	}

	//called from each new question activity.
	//we need this to delete each activity after the user moves on
	//to prevent going back to that question
	public void setCurrentContext(Activity newActivity){
		this.currentContext = newActivity;
	}

	//used by each question activity to get question data
	public QuestionData getQuestionData(){
		return questionData.get(questionMkr);
	}

	public ResultsManager getResultsManager(){
		return this.resultsManager;
	}

	public void nextQuestion(){
		if(!started)return;
		questionMkr ++;

		if (questionMkr == questionData.size()){
			instanceRecord.setCompleted(true);
			setResultsManager();
			goToResults();
			//note this does not reset the results manager
			//because the results manager is still needed in the new activity
			resetManager();
			return;
		}

		//start activity
		Intent intent = findQuestionIntent(getQuestionData().getQuestionType());
		//close previous activity only after first question
		if (canRemovePreviousActivity)
			((Activity)currentContext).finish();
		else
			canRemovePreviousActivity = true;
		currentContext.startActivity(intent);

	}

	public void recordResponse(String response, Boolean correct){
		QuestionData data = questionData.get(questionMkr);
		String questionID = data.getId();
		List<QuestionAttempt> attempts = instanceRecord.getAttempts();
		int attemptNumber = -1;
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
		QuestionData data = questionData.get(questionMkr);

		return data.getAnswer();
	}

	//note that this calls nextQuestion() to guarantee that the questions are populated
	//before nextQuestion() is called
	private void initializeQuestions(){
		final List<String> questionIDs = instanceData.getQuestionIds();
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference("questions");
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {

				for (String questionID : questionIDs){
					//find question
					QuestionData data = dataSnapshot.child(questionID).getValue(QuestionData.class);
					questionData.add(data);
				}

				startNewInstanceRecord();
				//do this only after every question is populated
				nextQuestion();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	private void startNewInstanceRecord(){
		instanceRecord = new InstanceRecord();
		instanceRecord.setCompleted(false);
		instanceRecord.setInstanceId(instanceData.getId());
		//not sure if we can instantiate in the question attempt class?
		instanceRecord.setAttempts(new ArrayList<QuestionAttempt>());
		startTimestamp = System.currentTimeMillis();
		//this is done asynchronously but it shouldn't matter tpp much
		if (FirebaseAuth.getInstance().getCurrentUser() != null){
			String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
			FirebaseDatabase db = FirebaseDatabase.getInstance();
			DatabaseReference ref = db.getReference("instanceRecords/"+userID+"/"+instanceData.getThemeId() +
					"/"+instanceRecord.getInstanceId());
			String key = ref.push().getKey();
			instanceRecord.setId(key);
		}
	}


	public void resetManager(){
		started = false;
		questionMkr = -1;
		currentContext = null;
		instanceData = null;
		instanceRecord = null;
		canRemovePreviousActivity = false;
		questionData.clear();
	}

	private Intent findQuestionIntent(int questionType){
		Intent intent = null;
		if (questionType == QuestionTypeMappings.MULTIPLE_CHOICE)
			intent = new Intent(currentContext, Question_MultipleChoice.class);
		else if (questionType == QuestionTypeMappings.TRUE_FALSE)
			intent = new Intent(currentContext, Question_TrueFalse.class);
		else if (questionType == QuestionTypeMappings.SENTENCE_PUZZLE)
			intent = new Intent(currentContext, Question_Puzzle_Piece.class);

		return intent;
	}

	private void setResultsManager(){
		this.resultsManager = new ResultsManager(instanceRecord, questionData, instanceData.getThemeId());
	}

	private void goToResults(){
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Intent intent = new Intent(currentContext, Results.class);
			((Activity) currentContext).finish();
			currentContext.startActivity(intent);
		} else {
			//ask user to sign up to save results
		}
	}


}
