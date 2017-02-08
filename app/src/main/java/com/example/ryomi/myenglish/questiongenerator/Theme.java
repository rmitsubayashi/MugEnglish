package com.example.ryomi.myenglish.questiongenerator;

import android.os.AsyncTask;

import com.example.ryomi.myenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//this class (inherited classes) will create the questions for the theme
//the first time around.
//from the second time on, we can just read the questions in from the db
public abstract class Theme {
	protected ThemeData themeData;
	//where we will populate new questions for this instance
	protected List<QuestionData> newQuestions = new ArrayList<>();
	//a full list of question IDs we will save into the theme instance data.
	//any existing questions we are putting into the instance will be put in here
	//directly as the ID instead of having to query again to get the question data
	private List<String> questionIDs = new ArrayList<>();
	//これが出題される問題のトピック
	protected Document documentOfTopics = null;
	//interests to search
	private final Set<WikiDataEntryData> userInterests = new HashSet<>();
	//makes sure we are not giving duplicate question
	// from previous instances of the user
	private final Set<String> userQuestionHistory = new HashSet<>();
	//save topics in the instance as unique identifiers
	// for when we display a list of instances to the user.
	protected final Set<String> topics = new HashSet<>();
	//how many questions we have
	protected int questionsLeftToPopulate;
	//MAXIMUM number of theme topics we need.
	//not directly related to the number of topics since one topic
	//may create more than one question
	protected int themeTopicCount;

	private EndpointConnectorReturnsXML connector = null;
	
	
	public Theme(EndpointConnectorReturnsXML connector, ThemeData data){
		this.themeData = data;
		this.connector = connector;
	}

	// 1. check if a question with matching topics already exists in the database
	// 2. fill the rest of the topics
	// 3. save in the DB
	public void createInstance() {
		startFlow();
	}


	//the next method is inside the previous method so we can make these
	//asynchronous methods act synchronously
	private void startFlow(){
		populateUserInterests();
		//populate user interests
		//populate existing questions
		//populateResults, processResultsIntoClassWrappers, createQuestionsFromResults
		//save questions in db
		//create instance
	}

	private  void populateUserInterests(){
		//user can be empty.
		//we allow non-registered users to play.
		//just not save progress
		if (FirebaseAuth.getInstance().getCurrentUser() != null){
			String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
			FirebaseDatabase db = FirebaseDatabase.getInstance();
			DatabaseReference ref = db.getReference("userInterests/"+userID);
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot child : dataSnapshot.getChildren()){
						WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
						userInterests.add(data);
					}
					populateUserQuestionHistory();
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}
	}

	//we need to skip over questions the user has already solved
	private void populateUserQuestionHistory(){
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
			FirebaseDatabase db = FirebaseDatabase.getInstance();
			DatabaseReference ref = db.getReference("themeInstances/" + userID + "/" + themeData.getId());
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot child : dataSnapshot.getChildren()) {
						ThemeInstanceData data = child.getValue(ThemeInstanceData.class);
						List<String> questionIDs = data.getQuestionIds();
						//shouldn't be null?
						//mainly for debug purposes
						if (questionIDs != null)
							userQuestionHistory.addAll(questionIDs);

					}

					populateExistingQuestions();
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}
	}


	//we are fetching all topics for questions of that thee.
	//if any match the user & the user has not had that question yet,
	//add it to the list of questions

	//the db is organized like
	//questions
	// +--theme id
	//   +--optional category
	//     +--wikidata id
	//        +--question1
	//        +--question2
	//        +-- ...
	private void populateExistingQuestions(){
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference("questionTopics/"+themeData.getId());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot topicsForTheme) {
				//prevent the same user interests popping up over and over.
				//this is not a concern about repeated instances of a single theme.
				//but more of a problem when the user starts 10 themes and 9 of them include
				//Leonardo Dicaprio because he is first on the list in the database
				List<WikiDataEntryData> userInterestList = new ArrayList<>(userInterests);
				Collections.shuffle(userInterestList);
				for (WikiDataEntryData userInterest : userInterestList){
					String interestID = userInterest.getWikiDataID();
					if (topicsForTheme.hasChild(interestID)){
						//there most likely will be more than one question per topic
						DataSnapshot questionSet = topicsForTheme.child(interestID);
						for (DataSnapshot question : questionSet.getChildren()){
							String questionID = (String)question.getValue();
							if (!userQuestionHistory.contains(questionID)) {
								questionIDs.add(questionID);
								topics.add(userInterest.getLabel());
								questionsLeftToPopulate--;
							}

							if (questionsLeftToPopulate == 0) break;
						}
						//if the user's interest exists for the theme,
						//no matter if the questions are new(we will add them to the question list)
						// or previously done (we do not add it to our question list),
						//we will not need to search for the interest again
						userInterests.remove(userInterest);

						if (questionsLeftToPopulate == 0) break;
					}
				}

				//we don't need to create new questions
				if (questionsLeftToPopulate == 0){
					//skip creating questions
					//and save them in the db
					saveInstance();
				} else {
					//we have to do this in a separate thread because the
					//onDataChange method runs on the UI thread
					CreateQuestionHelper helper = new CreateQuestionHelper();
					helper.execute();
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	//Firebase onChange() works on the main UI Thread (even if it's called from another service
	// so we have to make a separate thread
	private class CreateQuestionHelper extends AsyncTask<Void, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(Void... params){
			try {
				populateResults();
				processResultsIntoClassWrappers();

				//from here the methods are not (might not be) synchronous
				//more methods embedded in this.
				//create questions
				//save in db
				accessDBWhenCreatingQuestions();
				return true;
			} catch (Exception e){
				e.printStackTrace();
			}
			return false;
		}
	}


	
	//検索するのは特定のentityひとつに対するクエリー
	//UNIONしてまとめて検索してもいいけど時間が異常にかかる
	protected abstract String getSPARQLQuery();
	//一つ一つのクエリーを送って、まとめる
	private void populateResults() throws Exception {
		for (WikiDataEntryData interest : userInterests){
			String entityID = interest.getWikiDataID();
			String query = addEntityToQuery(entityID);
			Document resultDOM = connector.fetchDOMFromGetRequest(query);
			this.addResultsToMainDocument(resultDOM);
			if (WikiDataSPARQLConnector.countResults(documentOfTopics) >= questionsLeftToPopulate){
				break;
			}
		}

		//TODO recommendation algorithm when creating questions

		//if we can't populate results with user interests, fetch random questions from the db

	}
	//ドキュメントのデータを、わかりやすいクラスに入れる
	protected abstract void processResultsIntoClassWrappers();
	//save topic data
	protected abstract void saveResultTopics();

	//問題を作ってリストに保存する
	protected abstract void createQuestionsFromResults();

	/* if we ever want to access the database when writing the questions
	 * overwrite this method.
	 * 1. write your createquestionsfromresults, accessing the database
	 * 2. put the saveQuestionsInDB() inside the db listener
	 * see NAME_plays_SPORT for example
	 */
	protected void accessDBWhenCreatingQuestions(){
		//we are saving the result topics in here because
		//typically when we are accessing the db at this point
		//we are trying to read a value.
		//If we fail to locate the value, we just remove the question (for now).
		//so we should save the topics after potentially removing questions
		saveResultTopics();
		createQuestionsFromResults();
		saveQuestionsInDB();
	}

	//called after QuestionData is populated
	//where the question ID is set as an empty string
	protected void saveQuestionsInDB(){
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		for (QuestionData data : newQuestions){
			//if the question already has an ID,
			//that means it is a question we read from
			//the database and not a question we just generated
			if (!data.getId().equals("")){
				continue;
			}
			String topicID = data.getTopicId();
			DatabaseReference ref = db.getReference("questions");
			String key = ref.push().getKey();
			//set ID in data
			data.setId(key);
			//save in db
			DatabaseReference ref2 = db.getReference("questions/"+key);
			ref2.setValue(data);

			//now save a reference for the question topics
			//so we can easily detect if a topic for a theme exists
			//and grab the question id
			DatabaseReference ref3 = db.getReference("questionTopics/"+themeData.getId()+
				"/"+data.getTopicId());
			String key2 = ref3.push().getKey();
			DatabaseReference ref4 = db.getReference("questionTopics/"+themeData.getId()+
				"/"+data.getTopicId()+"/"+key2);
			ref4.setValue(data.getId());
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
		for (QuestionData q : newQuestions){
			questionIDs.add(q.getId());
		}

		//we can't save collections
		List<String> topicList = new ArrayList<>(topics);
		ThemeInstanceData data = new ThemeInstanceData(key, themeData.getId(),
				userID, questionIDs, topicList, System.currentTimeMillis(),0);
		DatabaseReference ref2 = db.getReference("themeInstances/"+userID+"/"+themeData.getId()+"/"+key);
		ref2.setValue(data);

		//end of flow
	}

	//when we populate results, we want to combine each DOM for each query
	// into one single DOM
	private void addResultsToMainDocument(Document newDocument){
		if (this.documentOfTopics == null){
			this.documentOfTopics = newDocument;
			return;
		}
		
		//<results>タグは一つしかない前提
		Node documentOfTopicsHead = documentOfTopics.getElementsByTagName("results").item(0);
		int dotResultsCount = WikiDataSPARQLConnector.countResults(documentOfTopics);
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
	private String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}


	
	
}
