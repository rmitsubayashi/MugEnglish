package com.example.ryomi.myenglish.questiongenerator;

import android.os.AsyncTask;
import android.util.Log;

import com.example.ryomi.myenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataAPIGetConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.FirebaseDBHeaders;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.db.datawrappers.WikiDataEntryData;
import com.example.ryomi.myenglish.questiongenerator.themes.QuestionDataWrapper;
import com.example.ryomi.myenglish.userinterestcontrols.EntityGetter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
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
	private static final String TAG = "theme";
	protected ThemeData themeData;
	//where we will populate new questions for this instance
	protected List<QuestionDataWrapper> newQuestions = new ArrayList<>();
	//a full list of question IDs we will save into the theme instance data.
	//any existing questions we are putting into the instance will be put in here
	//directly as the ID instead of having to query again to get the question data
	private List<List<String>> questionSets = new ArrayList<>();
	//これが出題される問題のトピック
	protected Document documentOfTopics = null;
	//interests to search
	private final Set<WikiDataEntryData> userInterests = new HashSet<>();
	//makes sure we are not giving duplicate question
	// from previous instances of the user
	private final List<List<String>> userQuestionHistory = new ArrayList<>();
	//save topics in the instance as unique identifiers
	// for when we display a list of instances to the user.
	protected final Set<String> topics = new HashSet<>();
	//how many question sets we should have
	protected int questionSetsLeftToPopulate;
	//save this so we don't have to fetch same data again
	private DataSnapshot allTopics = null;
	//for getting wikiData entries from wikiData IDs in topics
	private EntityGetter getter;

	/*
	//MAXIMUM number of theme topics we need.
	//not directly related to the number of topics since one topic
	//may create more than one question
	protected int themeTopicCount;
	we don't want this now since with the ordering of fetching questions that we have,
	if we limit the query results, the rest of the results will never be called.
	*/

	private EndpointConnectorReturnsXML connector = null;
	
	
	public Theme(EndpointConnectorReturnsXML connector, ThemeData data){
		this.themeData = data;
		this.connector = connector;
		WikiDataAPIGetConnector getConnector = new WikiDataAPIGetConnector(
				WikiBaseEndpointConnector.JAPANESE);
		getter = new EntityGetter(getConnector);
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
			DatabaseReference ref = db.getReference(
					FirebaseDBHeaders.USER_INTERESTS + "/" + userID);
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
			//this fetches all questions done by the user already
			DatabaseReference ref = db.getReference(
					FirebaseDBHeaders.THEME_INSTANCES + "/" + userID + "/" + themeData.getId());
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot child : dataSnapshot.getChildren()) {
						ThemeInstanceData data = child.getValue(ThemeInstanceData.class);
						List<List<String>> dataQuestionSets = data.getQuestionSets();
						//shouldn't be null?
						//mainly for debug purposes
						if (dataQuestionSets != null)
							userQuestionHistory.addAll(dataQuestionSets);

					}

					populateExistingNewQuestions(new HashSet<>(userInterests));
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}
	}


	//we are fetching already created questions (from firebase).
	//if any match the user's interests & the user has not had that question yet,
	//add it to the list of questions

	//the db is organized like
	//questions
	// +--theme id
	//   +--optional category
	//     +--wikidata id
	//        +--question1
	//        +--question2
	//        +-- ...
	private void populateExistingNewQuestions(final Set<WikiDataEntryData> interests){
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		//this gets us a list of question sets indexed by the wikiData ID
		DatabaseReference ref = db.getReference(
				FirebaseDBHeaders.QUESTION_TOPICS + "/" + themeData.getId());
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot topicsForTheme) {
				//save this as a class variable since we will need the same exact data again.
				//(one less connection)
				allTopics = topicsForTheme;

				//prevent the same user interests popping up over and over.
				//this is not a concern about repeated instances of a single theme.
				//but more of a problem when the user starts 10 themes and 9 of them include
				//Leonardo Dicaprio because he is first on the list in the database.
				//set -> list
				List<WikiDataEntryData> userInterestList = new ArrayList<>(interests);
				Collections.shuffle(userInterestList);
				for (WikiDataEntryData userInterest : userInterestList){
					String interestID = userInterest.getWikiDataID();
					if (topicsForTheme.hasChild(interestID)){
						//there can be more than one question sets per topic
						//ex: Manny Pacquiao plays both basketball and boxing professionally
						DataSnapshot questionSetsSnapshot = topicsForTheme.child(interestID);
						for (DataSnapshot questionSet : questionSetsSnapshot.getChildren()){
							GenericTypeIndicator<List<String>> type =
									new GenericTypeIndicator<List<String>>() {};
							List<String> set = questionSet.getValue(type);
							//only check the first question.
							//if the first question exists, the rest of the questions have to also exist
							// (for now).
							if (!questionExists(set.get(0))) {
								questionSets.add(set);
								topics.add(userInterest.getLabel());
								questionSetsLeftToPopulate--;
								if (questionSetsLeftToPopulate == 0) break;
							}
						}
						//if the user's interest exists for the theme,
						//no matter if the questions are new(we will add them to the question list)
						// or previously done (we do not add it to our question list),
						//we will not need to search for the interest again
						interests.remove(userInterest);

						if (questionSetsLeftToPopulate == 0) break;
					}
				}

				//we don't need to create new questions
				if (questionSetsLeftToPopulate == 0){
					//skip creating questions
					//and save them in the db
					saveInstance();
				} else {
					//we have to do this in a separate thread because the
					//onDataChange method runs on the UI thread
					CreateQuestionHelper helper = new CreateQuestionHelper();
					//we can ignore the warning on Android Studio
					helper.execute(interests);
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	//checks if the user already has completed the question
	private boolean questionExists(String questionID){
		for (List<String> questionSet : userQuestionHistory){
			if (questionSet.contains(questionID))
				return true;
		}

		return false;
	}

	//Firebase onChange() works on the main UI Thread (even if it's called from another service
	// so we have to make a separate thread
	private class CreateQuestionHelper extends AsyncTask<Set<WikiDataEntryData>, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(Set<WikiDataEntryData>... params){
			try {
				Set<WikiDataEntryData> interests = params[0];
				populateResults(interests);
				if (documentOfTopics != null)
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
	private void populateResults(Set<WikiDataEntryData> interests) throws Exception {
		for (WikiDataEntryData interest : interests){
			String entityID = interest.getWikiDataID();
			String query = addEntityToQuery(entityID);
			Document resultDOM = connector.fetchDOMFromGetRequest(query);
			this.addResultsToMainDocument(resultDOM);
			//there can be more results than we need
			if (WikiDataSPARQLConnector.countResults(documentOfTopics) >= questionSetsLeftToPopulate){
				break;
			}
		}

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
		saveNewQuestions();
	}

	//saves the newly made questions in the database
	// and the question ID list we have for this instance.
	//called after QuestionData is populated
	//where the question ID is set as an empty string (will be set here).
	//we may create more questions than the user will be getting,
	//but this is so all questions possible for one theme are created.
	//if we only create questions for the user, all the rest of the questions
	//will never be created
	protected void saveNewQuestions(){
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		for (QuestionDataWrapper dataList : newQuestions){
			//we are storing an array of IDs, not the actual questions
			List<String> questionIDs = new ArrayList<>();
			//save each question in the database
			for (QuestionData data : dataList.getQuestionSet()) {
				DatabaseReference ref = db.getReference(FirebaseDBHeaders.QUESTIONS);
				String key = ref.push().getKey();
				//set ID in data
				data.setId(key);
				//save in db
				DatabaseReference ref2 = db.getReference(
						FirebaseDBHeaders.QUESTIONS + "/" + key);
				ref2.setValue(data);
				//just so we can store the data in question topics
				questionIDs.add(key);
			}

			String topicID = dataList.getWikiDataID();

			//now save a reference for the question topics
			//so we can easily detect if a topic for a theme exists
			//and grab the question id
			DatabaseReference ref3 = db.getReference(FirebaseDBHeaders.QUESTION_TOPICS + "/" +
					themeData.getId() + "/" + topicID);
			String key2 = ref3.push().getKey();
			DatabaseReference ref4 = db.getReference(FirebaseDBHeaders.QUESTION_TOPICS + "/" +
					themeData.getId() + "/" + topicID + "/" + key2);
			ref4.setValue(questionIDs);

			//only save the data in the user's current set of questions if
			//less than the topic count.
			if (questionSetsLeftToPopulate != 0) {
				//all question ids for this theme
				this.questionSets.add(questionIDs);
				questionSetsLeftToPopulate--;
			}
		}

		Log.d(TAG,"Finished saving new questions into the database");

		if (questionSetsLeftToPopulate != 0)
			fillRemainingQuestions(); //calls saveInstance() once this is finished
		else
			saveInstance();

	}

	//this is for if we can't populate the questions with just the user interests
	// (and sub-interests once we implement that).
	//first, we check any questions in the db non-related to the user.
	//if that doesn't work, then repeat the user's existing questions
	private void fillRemainingQuestions(){
		//the topics in the db only store wikiData IDs
		//so we will need to fetch the label from wikiData and update

		//if we don't shuffle, the user will get lower WikiData ID topics first
		List<DataSnapshot> shuffledTopics = new ArrayList<>();
		//allTopics reference =
		//FirebaseDBHeaders.QUESTION_TOPICS + "/" + themeData.getId()
		for (DataSnapshot topic : allTopics.getChildren()) {
			shuffledTopics.add(topic);
		}
		Collections.shuffle(shuffledTopics);

		//list of wikiData IDs. we will fetch the rest of the data later
		final List<String> topicsToGet = new ArrayList<>();
		for (DataSnapshot topic : shuffledTopics){
			String wikiDataID = topic.getKey();
			//this condition will cover both existing questions and newly created questions.
			//need to make sure we add recommended interests to userInterests as well
			if (!hasWikiDataID(userInterests,wikiDataID)){
				for (DataSnapshot questionSet : topic.getChildren()) {
					GenericTypeIndicator<List<String>> type =
							new GenericTypeIndicator<List<String>>() {
							};
					List<String> questionIDSet = questionSet.getValue(type);
					questionSets.add(questionIDSet);
					//we need the label, not the ID
					//we can either save the label as well in the firebase entry,
					//but that seems like too much extra data added?
					//so fetch the name from wikiData
					topicsToGet.add(wikiDataID);
					questionSetsLeftToPopulate--;

					if (questionSetsLeftToPopulate == 0)
						break;
				}
			}

			if (questionSetsLeftToPopulate == 0)
				break;
		}

		String[] topicArray = topicsToGet.toArray(new String[topicsToGet.size()]);

		//last resort, use the user's previous questions.
		//question history only has a reference to the question,
		// and the question only has reference to the wikiData ID.
		List<String> questionIDsToSearach = new ArrayList<>();
		if (questionSetsLeftToPopulate != 0){
			Collections.shuffle(userQuestionHistory);
			//no need to check if the user's question history is more than the remaining topics
			//because it is guaranteed to be at least equal
			for (int i=0; i<questionSetsLeftToPopulate; i++){
				List<String> questionSet = userQuestionHistory.get(i);
				this.questionSets.add(questionSet);
				questionIDsToSearach.addAll(questionSet);
			}

			//connect to firebase to get the questions' topics
			fetchTopicsFromFirebase(questionIDsToSearach,0,topicArray);

		} else {
			//no need to connect to fireBase.
			fetchTopics(topicArray);
		}
	}

	//helper to check if user interests contains a wikiData ID
	private boolean hasWikiDataID(Set<WikiDataEntryData> set, String id){
		for (WikiDataEntryData data : set){
			if (data.getWikiDataID().equals(id))
				return true;
		}
		return false;
	}

	//recursively (and therefore synchronously) fetch particular questions.
	//thought this is better than getting the whole list of questions
	private void fetchTopicsFromFirebase(final List<String> questionIDsToSearch, final int index, final String[] topicArray){
		if (index == questionIDsToSearch.size()){
			fetchTopics(topicArray);
			return;
		}

		String questionID = questionIDsToSearch.get(index);
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
				FirebaseDBHeaders.QUESTIONS + "/" + questionID
		);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				QuestionData data = dataSnapshot.getValue(QuestionData.class);
				topics.add(data.getTopic());
				fetchTopicsFromFirebase(questionIDsToSearch,(index + 1),topicArray);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				fetchTopics(topicArray);
			}
		});
	}

	private void fetchTopics(String[] topicArray){
		try {
			GetWikiDataHelper conn = new GetWikiDataHelper();
			//saves instance after this is called
			conn.execute(topicArray);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//fetch all wikiData entities in one query to reduce overhead
	private class GetWikiDataHelper extends AsyncTask<String, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(String... topicArray){
			try {
				List<WikiDataEntryData> topicsData = getter.get(topicArray);
				for (WikiDataEntryData topic : topicsData){
					topics.add(topic.getLabel());
				}
				saveInstance();
				return true;
			} catch (Exception e){
				e.printStackTrace();
			}
			return false;
		}
	}

	private void saveInstance(){
		String userID;
		if (FirebaseAuth.getInstance().getCurrentUser() == null){
			userID = "temp";
		} else {
			userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		}
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference ref = db.getReference(
				FirebaseDBHeaders.THEME_INSTANCES + "/" + userID + "/" +themeData.getId());
		String key = ref.push().getKey();

		//set->list (can't save sets)
		List<String> topicList = new ArrayList<>(topics);
		ThemeInstanceData data = new ThemeInstanceData(key, themeData.getId(),
				userID, questionSets, topicList, System.currentTimeMillis(),0);
		DatabaseReference ref2 = db.getReference(
				FirebaseDBHeaders.THEME_INSTANCES + "/" + userID + "/" + themeData.getId() + "/" +key);
		ref2.setValue(data);
		Log.d(TAG,"Finished saving instance into the database");

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
		Node documentOfTopicsHead = documentOfTopics.
				getElementsByTagName(WikiDataSPARQLConnector.ALL_RESULTS_TAG).item(0);
		//int dotResultsCount = WikiDataSPARQLConnector.countResults(documentOfTopics);
		//fetch all results
		NodeList newDocumentResults = newDocument.getElementsByTagName(
				WikiDataSPARQLConnector.RESULT_TAG
		);
		int newDocumentResultsCount = newDocumentResults.getLength();
		//add the node to the main document DOM
		for (int i=0; i<newDocumentResultsCount; i++){
			//if (dotResultsCount >= themeTopicCount) return;
			
			Node nextNode = newDocumentResults.item(i);
			//we need to import from new document to main document
			//importNode(Node, deep) where deep = copy children as well
			Node importedNextNode = documentOfTopics.importNode(nextNode, true);
			documentOfTopicsHead.appendChild(importedNextNode);
			//dotResultsCount ++;
		}
		
	}

	//ひとつのクエリーで複数のエンティティを入れる必要があるかも？？
	private String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}


	
	
}
