package com.example.ryomi.myenglish.questionmanager;


import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.questiongenerator.Question;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//manages the execution of questions.
//this means that any new instances will be generated before calling this class
public class QuestionManager {
	ThemeInstanceData instanceData = null;
	List<Question> questions = new ArrayList<>();

	private QuestionManager(){
		//should not call without inserting instance information
	}

	public QuestionManager(ThemeInstanceData data){
		this.instanceData = data;
		findQuestions();
	}

	private void findQuestions(){
		final List<String> questionIDs = instanceData.getQuestionIds();
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference("questions/"+instanceData.getThemeId());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				int questionCt = questionIDs.size();
				for (int i=0; i<questionCt; i++){
					//find question
					dataSnapshot.child(questionIDs.get(i)).getValue();
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


	}




}
