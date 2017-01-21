package com.example.ryomi.myenglish.questionmanager;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.QuestionRecord;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.gui.Question_MultipleChoice;
import com.example.ryomi.myenglish.gui.ThemeList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//manages the execution of questions.
//this means that any new instances will be generated before calling this class

//this class will be passed to each question activity

//parcelable is faster but more boilerplate code
//and since the difference is ~10ms I don't care about speed
public class QuestionManager implements Serializable{
	private Context currentActivity;
	ThemeInstanceData instanceData;
	private final List<QuestionData> questionData = new ArrayList<>();
	//-1 so first call of nextQuestion() would be 0
	private int questionMkr = -1;
	List<QuestionRecord> questionRecord = new ArrayList<>();
	//variables to save individual question records before adding them into the array
	private long questionStartTime;
	private long questionEndTime;
	//user input
	private Boolean correct;
	private String response;
	//records how many times a user touches a vocabulary word
	private Map<String, Integer> vocabularyTouchedCt;
	//end variables


	private QuestionManager(){
		//should not call without inserting instance information
	}

	public QuestionManager(ThemeInstanceData data, Activity startingActivity){
		this.currentActivity = startingActivity;
		this.instanceData = data;
		//initializeQuestions();
	}

	public void setActivity(Activity activity){
		this.currentActivity = activity;
	}

	public QuestionData getQuestionData(){
		return questionData.get(questionMkr);
	}

	public void nextQuestion(){
		clearRecord();
		questionMkr ++;

		if (questionMkr == questionData.size()){
			System.out.println("Finished all questions");
			return;
		}

		QuestionData data = questionData.get(questionMkr);

		//start activity
		Intent intent = findQuestionIntent(data.getQuestionType());
		//close previous activity
		((Activity)currentActivity).finish();
		currentActivity.startActivity(intent);

		questionStartTime = System.currentTimeMillis();

	}

	public void incrementVocabularyTouched(String word){
		if (vocabularyTouchedCt.containsKey(word)){
			Integer prevCt = vocabularyTouchedCt.get(word);
			Integer currCt = prevCt + 1;
			vocabularyTouchedCt.put(word, currCt);
		} else {
			vocabularyTouchedCt.put(word, 1);
		}
	}

	public Boolean checkAnswer(String response){
		this.response = response;
		//this should be the end time for the question
		questionEndTime = System.currentTimeMillis();

		QuestionData data = questionData.get(questionMkr);
		if (data.getAnswer().equals(response)){
			return true;
		} else{
			return false;
		}
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
		DatabaseReference ref = db.getReference("questions/"+instanceData.getThemeId());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				int questionCt = questionIDs.size();
				for (int i=0; i<questionCt; i++){
					//find question
					QuestionData data = dataSnapshot.child(questionIDs.get(i)).getValue(QuestionData.class);
					questionData.add(data);
				}

				nextQuestion();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}


	private void clearRecord(){
		vocabularyTouchedCt.clear();
		//everything else right now are just strings that can be overwritten
	}

	private Intent findQuestionIntent(int questionType){
		Intent intent = new Intent(currentActivity, Question_MultipleChoice.class);
		intent.putExtra("manager",this);
		return intent;
	}


}
