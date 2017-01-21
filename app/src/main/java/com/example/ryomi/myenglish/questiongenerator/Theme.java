package com.example.ryomi.myenglish.questiongenerator;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.ryomi.myenglish.connectors.EndpointConnector;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//this class (inherited classes) will create the questions for the theme
//the first time around.
//from the second time on, we can just read the questions in from the db
public abstract class Theme {
	protected ThemeData themeData;
	//the user can create multiple instances of the same theme
	protected ThemeInstanceData themeInstanceData;
	//so we can grab the wikidata IDs
	protected String wikiDataIDPH;
	//where we will populate questions for this instance
	protected List<QuestionData> questions = new ArrayList<>();
	//これが出題される問題のトピック
	protected Document documentOfTopics = null;
	protected final Set<String> userInterests = new HashSet<>();
	protected int themeTopicCount;
	protected EndpointConnector connector = null;
	private Activity activity;
	
	
	public Theme(EndpointConnector connector, ThemeData data){
		this.themeData = data;
		this.connector = connector;
	}

	//check if a question for this theme with the matching topics already exists
	//in the database. Then, fill the rest of the topics
	//then instantiate the question manager and start*
	//*we should ideally return the instance data and start the question manager in the activity..
	public void initiateQuestions(Activity activity) {
		this.activity = activity;
		startFlow();
	}


	//the next method is inside the previous method so we can make these
	//asynchronous methods act synchronously
	private void startFlow(){
		populateUserInterests();
		System.out.println("Called end of startFlow");
		//populate user interests
		//populate existing questions
		//populateResults, processResultsIntoClassWrappers, createQuestionsFromResults
		//save questions in db
		//create instance
	}

	private  void populateUserInterests(){
		if (FirebaseAuth.getInstance().getCurrentUser() != null){
			String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
			FirebaseDatabase db = FirebaseDatabase.getInstance();
			DatabaseReference ref = db.getReference("userInterests/"+userID);
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot child : dataSnapshot.getChildren()){
						WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
						userInterests.add(data.getWikiDataID());
					}
					System.out.println("Finish populating user interests");
					populateExistingQuestions();
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}
	}

	//the db is organized like
	//questions
	// +--theme id
	//     +--wikidata id
	//        +--question1
	//        +--question2
	//        +-- ...
	private void populateExistingQuestions(){
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference("questions/"+themeData.getId());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				//remove user interest if we can pre-populate the question
				List<String> toRemove = new ArrayList<String>();
				for (String interestID : userInterests){
					if (dataSnapshot.hasChild(interestID)){
						DataSnapshot questionSet = dataSnapshot.child(interestID);
						for (DataSnapshot snapshot : questionSet.getChildren()){
							QuestionData data = snapshot.getValue(QuestionData.class);
							questions.add(data);
						}
						//one less topic we have to search for
						themeTopicCount--;
						toRemove.add(interestID);
						if (themeTopicCount == 0){
							break;
						}
					}
				}
				//remove matched user interests
				for (String removeID : toRemove){
					userInterests.remove(removeID);
				}

				//we have to do this in a separate thread because the
				//onDataChange method runs on the UI thread
				CreateQuestionHelper helper = new CreateQuestionHelper();
				helper.execute();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	
	//検索するのは特定のentityひとつに対するクエリー
	//UNIONしてまとめて検索してもいいけど時間が異常にかかる
	protected abstract String getSPARQLQuery();
	//一つ一つのクエリーを送って、まとめる
	protected abstract void populateResults(Set<String> wikiDataIDs) throws Exception;
	//ドキュメントのデータを、わかりやすいクラスに入れる
	protected abstract void processResultsIntoClassWrappers();
	//問題を作ってリストに保存する
	protected abstract void createQuestionsFromResults();

	//called after QuestionData is populated
	//where the question ID is set as an empty string
	private void saveQuestionsInDB(){
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		for (QuestionData data : questions){
			//if the question already has an ID,
			//that means it is a question we read from
			//the database and not a question we just generated
			if (!data.getId().equals("")){
				continue;
			}
			String topicID = data.getTopicId();
			DatabaseReference ref = db.getReference("questions/"+themeData.getId()+"/"+topicID);
			String key = ref.push().getKey();
			//set ID in data
			data.setId(key);
			//save in db
			DatabaseReference ref2 = db.getReference(
					"questions/"+themeData.getId()+"/"+topicID+"/"+key
			);
			ref2.setValue(data);
		}

		System.out.println("Finished saving into db");

		saveInstance();

	}

	private void saveInstance(){
		String userID;
		if (FirebaseAuth.getInstance().getCurrentUser() == null){
			userID = "temp";
		} else {
			userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		}
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference("themeInstances/"+userID+"/"+themeData.getId());
		String key = ref.push().getKey();

		//create instance
		List<String> questionIDs = new ArrayList<>();
		for (QuestionData q : questions){
			questionIDs.add(q.getId());
		}
		ThemeInstanceData data = new ThemeInstanceData(key, themeData.getId(),
				userID, questionIDs, System.currentTimeMillis());
		DatabaseReference ref2 = db.getReference("themeInstances/"+userID+"/"+themeData.getId()+"/"+key);
		ref2.setValue(data);

		startInstance(data);
	}

	private void startInstance(ThemeInstanceData data){
		QuestionManager manager = new QuestionManager(data, activity);
	}
		
	
	protected int countResults(Document doc){
		return doc.getElementsByTagName("result").getLength();
		
	}
	
	protected void addResultsToMainDocument(Document newDocument){
		if (this.documentOfTopics == null){
			this.documentOfTopics = newDocument;
			return;
		}
		
		//<results>タグは一つしかない前提
		Node documentOfTopicsHead = documentOfTopics.getElementsByTagName("results").item(0);
		int dotResultsCount = this.countResults(documentOfTopics);
		NodeList newDocumentResults = newDocument.getElementsByTagName("result");
		int newDocumentResultsCount = newDocumentResults.getLength();
		
		for (int i=0; i<newDocumentResultsCount; i++){
			if (dotResultsCount >= themeTopicCount) return;
			
			Node nextNode = newDocumentResults.item(i);
			//we need to import from new document to main document
			//importNode(Node, deep) where deep = copy children as well
			Node importedNextNode = documentOfTopics.importNode(nextNode, true);
			documentOfTopicsHead.appendChild(importedNextNode);
			dotResultsCount ++;
		}
		
	}

	//ひとつのクエリーで複数のエンティティを入れる必要があるかも？？
	protected String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}

	//more of the workflow
	private class CreateQuestionHelper extends AsyncTask<Void, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(Void... params){
			try {
				//these are all synchronous
				populateResults(userInterests);
				processResultsIntoClassWrappers();
				createQuestionsFromResults();
				//from here the methods are not synchronous
				//more methods embedded in this
				saveQuestionsInDB();
				return true;
			} catch (Exception e){
				e.printStackTrace();
			}
			return false;
		}
	}
	
	
}
