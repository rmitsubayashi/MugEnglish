package com.linnca.pelicann.lessongenerator;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

//this class (inherited classes) will create the questions for the lesson
//the first time around.
//from the second time on, we can just read the questions in from the db
public abstract class Lesson {
	private static final String TAG = "lesson";
	protected final String TOPIC_GENERIC_QUESTION = "一般問題";
	private FirebaseDatabase db;
	protected String lessonKey;
	//the lesson instance we will be creating
	private final LessonInstanceData lessonInstanceData = new LessonInstanceData();
	//where we will populate new questions for this instance (from each inherited class)
	protected final List<QuestionDataWrapper> newQuestions = new ArrayList<>();
	//question set ids we need to query FireBase to get more information
	private final List<String> questionSetIDs = Collections.synchronizedList(new ArrayList<String>());
	//これが出題される問題のトピック
	//protected Document documentOfTopics = null
	//all interests (used when we check related interests
	private Set<WikiDataEntryData> allUserInterests;
	//interests to search
	private Set<WikiDataEntryData> userInterests;
	//interests that we have checked/know we can't fill (so we don't search for them)
	//needs to be synchronized
	private final Set<WikiDataEntryData> userInterestsChecked = Collections.synchronizedSet(new HashSet<WikiDataEntryData>());
	//makes sure we are not giving duplicate questions
	// from previous instances of the user.
	// Storing the IDs of the question sets
	private final Set<String> userQuestionHistory = new HashSet<>();
	//how many question sets we should have.
	//set in the inherited classes
	protected int questionSetsLeftToPopulate;
	//a category to search for so we don't have to search every user interest
	protected int categoryOfQuestion;
	//indicates whether we have already searched for interests related to the user.
	//this prevents an infinite loop
	private boolean relatedUserInterestsSearched = false;
	//how many related interests to search for each interest
	private int relatedUserInterestsToSearch = 3;
	//what to do after we finish creating an instance
	private final LessonListener lessonListener;

	private WikiBaseEndpointConnector connector = null;

	public interface LessonListener {
		void onLessonCreated();
	}
	
	
	protected Lesson(WikiBaseEndpointConnector connector, LessonListener lessonListener){
		this.connector = connector;
		this.lessonListener = lessonListener;
		this.db = FirebaseDatabase.getInstance();
	}

	// 1. check if a question already exists in the database
	// 2. create and fill the rest of the questions by querying WikiData
	// 3. save the new questions in the DB
	public void createInstance() {
		startFlow();
	}


	//the next method is inside the previous method so we can make these
	//asynchronous methods act synchronously
	private void startFlow(){
		//fill generic questions first
		List<List<String>> genericQuestionSets = getGenericQuestionIDSets();
		List<String> pickGenericQuestions = pickQuestions(genericQuestionSets);
		lessonInstanceData.addQuestionIds(pickGenericQuestions);
		if (getSPARQLQuery().equals("")){
			//we have a lesson without any dynamic questions
			saveInstance();
		} else {
			//now populate dynamic questions
			populateUserInterests();
		}
	}

	private void populateUserInterests(){
		String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		DatabaseReference ref = db.getReference(
				FirebaseDBHeaders.USER_INTERESTS + "/" + userID);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				allUserInterests = new HashSet<>((int)dataSnapshot.getChildrenCount());
				//have the size set to the maximum size after getting related user interests.
				//we populate this asynchronously when grabbing related user interests
				userInterests = Collections.synchronizedSet(new HashSet<WikiDataEntryData>((int)dataSnapshot.getChildrenCount() * relatedUserInterestsToSearch));
				for (DataSnapshot child : dataSnapshot.getChildren()){
					WikiDataEntryData data = child.getValue(WikiDataEntryData.class);
					if (data == null)
						continue;
					//filter by category so we don't have to search for user interests that are guaranteed not to work
					if (data.getClassification() == categoryOfQuestion ||
							data.getClassification() == WikiDataEntryData.CLASSIFICATION_NOT_SET) {
						userInterests.add(data);
					}
					allUserInterests.add(data);
				}
				populateUserQuestionHistory();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	//we need to skip over questions the user has already solved
	private void populateUserQuestionHistory(){
		String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		//this fetches all questions done by the user already
		DatabaseReference ref = db.getReference(
				FirebaseDBHeaders.LESSON_INSTANCES + "/" + userID + "/" + lessonKey);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				for (DataSnapshot lessonInstanceSnapshot : dataSnapshot.getChildren()) {
					LessonInstanceData lessonInstanceData = lessonInstanceSnapshot.getValue(LessonInstanceData.class);
					userQuestionHistory.addAll(lessonInstanceData.getQuestionSetIds());
				}

				fillQuestionsFromDatabase();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}


	//we should try fetching already created questions (from FireBase).
	//if any match the user's interests & the user has not had that question yet,
	//add it to the list of questions.
	//pretty expensive because we are grabbing all question set ids for a lesson,
	// but I can't think of any other way to make this work..
	private void fillQuestionsFromDatabase(){
		if (userInterests.size() == 0){
			//skip trying to fetch questions from db/wikiData.
			//we still might want to get related interests of interests
			//that were filtered out, so don't go to
			//fillRemainingQuestions() yet
			saveNewQuestions();
			return;
		}

		final AtomicInteger questionSetsToPopulateAtomicInt = new AtomicInteger(questionSetsLeftToPopulate);
		final AtomicInteger userInterestsLooped = new AtomicInteger(0);
		DatabaseReference questionSetRef = db.getReference(
				FirebaseDBHeaders.QUESTION_SET_IDS_PER_LESSON + "/" + lessonKey);
		//prevent the same user interests popping up over and over.
		//this is not a concern about repeated instances of a single theme.
		//but more of a problem when the user starts 10 themes and 9 of them include
		//Leonardo Dicaprio because he is first on the list in the database.
		//set -> list
		final List<WikiDataEntryData> userInterestList = new ArrayList<>(userInterests);
		Collections.shuffle(userInterestList);
		for (final WikiDataEntryData userInterest : userInterestList){
			//we might be finished already.
			if (questionSetsToPopulateAtomicInt.get() == 0){
				break;
			}
			DatabaseReference userInterestQuestionSetRef = questionSetRef.child(userInterest.getWikiDataID());

			userInterestQuestionSetRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {

					if (questionSetsToPopulateAtomicInt.get() == 0){
						return;
					}
					//this means someone already checked and it didn't match
					if (dataSnapshot.exists() && dataSnapshot.getValue() == null){
						userInterestsChecked.add(userInterest);
					}

					//check the questions to see if we have one the user hasn't had yet
					if (dataSnapshot.exists() && dataSnapshot.getValue() != null){
						userInterestsChecked.add(userInterest);
						for (DataSnapshot questionSetIDSnapshot : dataSnapshot.getChildren()){
							String questionSetID = questionSetIDSnapshot.getValue(String.class);
							if (!userQuestionHistory.contains(questionSetID) && !questionSetIDs.contains(questionSetID)) {
								questionSetIDs.add(questionSetID);
								if (questionSetsToPopulateAtomicInt.decrementAndGet() == 0) break;
							}
						}

						if (questionSetsToPopulateAtomicInt.get() == 0) {
							questionSetsLeftToPopulate = questionSetsToPopulateAtomicInt.get();
							//skip creating questions
							//and save them in the db
							getQuestionDataFromQuestionSetIDs();
							return;
						}
					}

					//if this is the last one, we should continue ()
					if (userInterestsLooped.incrementAndGet() == userInterestList.size()){
						questionSetsLeftToPopulate = questionSetsToPopulateAtomicInt.get();
						//we have to do this in a separate thread because the
						//onDataChange method runs on the UI thread
						CreateQuestionHelper helper = new CreateQuestionHelper();
						//we can ignore the warning on Android Studio
						helper.start();
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});

		}
	}

	//Firebase onChange() works on the main UI Thread (even if it's called from another service
	// so we have to make a separate thread
	private class CreateQuestionHelper extends Thread{
		@Override
		public void run(){
			try {
				//we don't need to check for interests we've already matched /
				// know we can't match
				Set<WikiDataEntryData> copy = new HashSet<>(userInterests);
				copy.removeAll(userInterestsChecked);
				populateResults(copy);

				//from here the methods are not (might not be) synchronous
				//more methods embedded in this.
				//create questions
				//save in db
				accessDBWhenCreatingQuestions();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	
	//検索するのは特定のentityひとつに対するクエリー
	//UNIONしてまとめて検索してもいいけど時間が異常にかかる
	protected abstract String getSPARQLQuery();
	//一つ一つのクエリーを送って、まとめる
	private void populateResults(Set<WikiDataEntryData> interests) throws Exception {
		//shuffle so we don't get the same interests over and over
		ArrayList<WikiDataEntryData> interestList = new ArrayList<>(interests);
		Collections.shuffle(interestList);
		for (WikiDataEntryData interest : interestList){
			String entityID = interest.getWikiDataID();
			String query = addEntityToQuery(entityID);
			Document resultDOM = connector.fetchDOMFromGetRequest(query);
			this.processResultsIntoClassWrappers(resultDOM);
			//there can be more results than we need.
			//subtracting handled in each theme
			if (getQueryResultCt() >= questionSetsLeftToPopulate){
				break;
			}
		}
	}

	protected abstract int getQueryResultCt();

	//ドキュメントのデータを、わかりやすいクラスに入れる
	protected abstract void processResultsIntoClassWrappers(Document document);

	//問題を作ってリストに保存する
	protected abstract void createQuestionsFromResults();

	/* if we ever want to access the database when writing the questions
	 * overwrite this method.
	 * 1. write your createquestionsfromresults, accessing the database
	 * 2. put the saveQuestionsInDB() inside the db listener
	 * see NAME_plays_SPORT for example
	 */
    private void accessDBWhenCreatingQuestions(){
		createQuestionsFromResults();
		saveNewQuestions();
	}

	//saves the newly made questions in the database
	// and the question ID list we have for this instance.
	//called after QuestionData is populated.
	//we may create more questions than the user will be getting,
	//but this is so all questions possible for one lesson are created.
	//if we only create questions for the user, all the rest of the questions
	//will never be created
	private void saveNewQuestions(){
		DatabaseReference questionRef = db.getReference(FirebaseDBHeaders.QUESTIONS);
		DatabaseReference questionSetRef = db.getReference(
				FirebaseDBHeaders.QUESTION_SETS
		);
		DatabaseReference questionSetIDsPerLessonRef = db.getReference(
				FirebaseDBHeaders.QUESTION_SET_IDS_PER_LESSON + "/" +
						lessonKey
		);
		DatabaseReference randomQuestionSetIDsRef = db.getReference(
				FirebaseDBHeaders.RANDOM_QUESTION_SET_IDS + "/" +
						lessonKey
		);
		//we are looping through each question set.
		// (questionDataWrapper has an extra field to store the wikiData ID
		// associated with the question set)
		for (QuestionDataWrapper questionDataWrapper : newQuestions){
			List<List<String>> questionIDs = new ArrayList<>();
			//save each question in the database
			for (List<QuestionData> question : questionDataWrapper.getQuestionSet()) {
				List<String> questionIDsForEachVariation = new ArrayList<>();
				//each question may have multiple variations.
				//save all variations as individual questions
				for (QuestionData data : question) {
					String questionKey = questionRef.push().getKey();
					//set ID in data
					data.setId(questionKey);
					//save in db
					questionRef.child(questionKey).setValue(data);
					questionIDsForEachVariation.add(questionKey);
				}
				questionIDs.add(questionIDsForEachVariation);
			}

			String questionSetWikiDataID = questionDataWrapper.getWikiDataID();
			//save the question set
			String questionSetKey = questionSetRef.push().getKey();
			questionSetRef.child(questionSetKey).child(FirebaseDBHeaders.QUESTION_SETS_QUESTION_IDS)
					.setValue(questionIDs);
			questionSetRef.child(questionSetKey).child(FirebaseDBHeaders.QUESTION_SETS_LABEL)
					.setValue(questionDataWrapper.getInterestLabel());

			//save the id reference of the question set we just created
			questionSetIDsPerLessonRef.child(questionSetWikiDataID).push().setValue(questionSetKey);

			DatabaseReference randomQuestionSetIDRef = randomQuestionSetIDsRef.push();
			randomQuestionSetIDRef.child(FirebaseDBHeaders.RANDOM_QUESTION_SET_ID).setValue(questionSetKey);
			randomQuestionSetIDRef.child(FirebaseDBHeaders.RANDOM_QUESTION_SET_DATE).setValue(ServerValue.TIMESTAMP);

			//only save the data in the user's current set of questions if
			//less than the remaining question count.
			//if this exceeds the remaining count, still continue
			//because we want to save the question data anyways
			if (questionSetsLeftToPopulate != 0) {
				List<String> instanceQuestions = pickQuestions(questionIDs);
				lessonInstanceData.addQuestionIds(instanceQuestions);
				lessonInstanceData.addQuestionSetId(questionSetKey);
				lessonInstanceData.addInterestLabel(questionDataWrapper.getInterestLabel());
				questionSetsLeftToPopulate--;
			}
		}

		if (questionSetsLeftToPopulate != 0 && relatedUserInterestsSearched) {
			fillRemainingQuestions();
		} else if (questionSetsLeftToPopulate != 0 && !relatedUserInterestsSearched){
			populateRelatedUserInterests();
		}
		else {
			getQuestionDataFromQuestionSetIDs();
		}

	}

	//we couldn't populate the questions with the user interests so try with relevant interests
	// that we can grab using the recommendation map
	private void populateRelatedUserInterests(){
		relatedUserInterestsSearched = true;
		//have atomic int & atomic list
		//for each user interest get related interest and populate
		//if atomic int == user interest count
		// copy the atomic list into user interests and re-try populating
		newQuestions.clear();
		userInterests.clear();
		final AtomicInteger relatedInterestsSearched = new AtomicInteger(0);
		final int allUserInterestCt = allUserInterests.size();
		for (WikiDataEntryData userInterest : allUserInterests){
			DatabaseReference relatedInterestRef = db.getReference(
					FirebaseDBHeaders.RECOMMENDATION_MAP_FOR_LESSON_GENERATION + "/" +
							userInterest.getWikiDataID() + "/" +
							categoryOfQuestion
			);
			Query relatedInterestQuery = relatedInterestRef.orderByChild(FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_COUNT)
					.limitToLast(relatedUserInterestsToSearch);
			relatedInterestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
						WikiDataEntryData data = childSnapshot.child(FirebaseDBHeaders.RECOMMENDATION_MAP_EDGE_DATA)
								.getValue(WikiDataEntryData.class);
						if (!allUserInterests.contains(data))
							userInterests.add(data);
					}

					if (relatedInterestsSearched.incrementAndGet() == allUserInterestCt){
						for (WikiDataEntryData interest : userInterests){
							Log.d(TAG, interest.getLabel());
						}
						fillQuestionsFromDatabase();
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}
	}

	//this is for if we can't populate the questions with just the user interests
	// (and sub-interests once we implement that).
	//first, we check any questions in the db non-related to the user.
	//if that doesn't work, then repeat the user's existing questions
	private void fillRemainingQuestions(){
		//the topics in the db only store wikiData IDs
		//so we will need to fetch the label from wikiData and update

		DatabaseReference randomIDsRef = FirebaseDatabase.getInstance().getReference(
				FirebaseDBHeaders.RANDOM_QUESTION_SET_IDS + "/" +
						lessonKey
		);

		//by pigeon hole, we are guaranteed to get questions the user hasn't had
		final int minimumFetchCount = userQuestionHistory.size() + questionSetsLeftToPopulate;
		Query allRandomIDsQuery = randomIDsRef.orderByChild(FirebaseDBHeaders.RANDOM_QUESTION_SET_DATE)
				.limitToFirst(minimumFetchCount);
		allRandomIDsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				List<DataSnapshot> questionSetSnapshots = new ArrayList<>();
				for (DataSnapshot questionSetSnapshot : dataSnapshot.getChildren()) {
					questionSetSnapshots.add(questionSetSnapshot);
				}
				//we are ordering by date, so it's likely that a user will get two sets created at the same time
				// (same user interest) so shuffle them to make sure this won't happen as often
				Collections.shuffle(questionSetSnapshots);
				//so far we've only added newly created question ids to the lesson instance data
				//(the rest are all just IDs that we will need to fetch the whole data later)
				List<String> newQuestionSetIDs = lessonInstanceData.getQuestionSetIds();
				for (DataSnapshot questionSetSnapshot : questionSetSnapshots){
					String questionSetID = questionSetSnapshot.child(FirebaseDBHeaders.RANDOM_QUESTION_SET_ID).getValue(String.class);
					//if the user hasn't had the question yet and is not in their current question set
					if (!userQuestionHistory.contains(questionSetID) && !questionSetIDs.contains(questionSetID) &&
							!newQuestionSetIDs.contains(questionSetID)) {
						questionSetIDs.add(questionSetID);
						questionSetsLeftToPopulate--;
					}

					//refresh timestamp so this goes to the end
					String key = questionSetSnapshot.getKey();
					db.getReference(FirebaseDBHeaders.RANDOM_QUESTION_SET_IDS + "/" +
							lessonKey + "/" +
							key + "/" +
							FirebaseDBHeaders.RANDOM_QUESTION_SET_DATE
					).setValue(ServerValue.TIMESTAMP);

					if (questionSetsLeftToPopulate == 0)
						break;

				}

				//last resort, populate with already created questions
				if (questionSetsLeftToPopulate != 0){
					//make it a list so we can shuffle
					List<String> userQuestionHistoryList = new ArrayList<>(userQuestionHistory);
					Collections.shuffle(userQuestionHistoryList);
					//no need to check if the user's question history is more than the remaining topics
					//because it is guaranteed to be at least equal
					for (String questionSetID : userQuestionHistoryList){
						//we don't want duplicates
						if (!questionSetIDs.contains(questionSetID)) {
							questionSetIDs.add(questionSetID);
							questionSetsLeftToPopulate--;
						}

						//we are finished populating questions
						if (questionSetsLeftToPopulate == 0)
							break;
					}
				}

				//continue
				getQuestionDataFromQuestionSetIDs();

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	private void getQuestionDataFromQuestionSetIDs(){
		//if we don't need to search (all questions are newly created)
		if (questionSetIDs.size() == 0){
			Log.d(TAG, "no need to get question sets from db");
			saveInstance();
			return;
		}
		Log.d(TAG, "getting question sets from db");
		//to check each listener to see if all listeners have completed
		final AtomicInteger questionSetsRetrieved = new AtomicInteger(0);
		DatabaseReference questionSetsRef = db.getReference(FirebaseDBHeaders.QUESTION_SETS);
		for (String questionSetID : questionSetIDs){
			DatabaseReference questionSetRef = questionSetsRef.child(questionSetID);
			questionSetRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					String interestLabel = dataSnapshot.child(FirebaseDBHeaders.QUESTION_SETS_LABEL)
							.getValue(String.class);
					GenericTypeIndicator<List<List<String>>> type =
							new GenericTypeIndicator<List<List<String>>>() {};
					List<List<String>> allQuestions = dataSnapshot.child(FirebaseDBHeaders.QUESTION_SETS_QUESTION_IDS)
							.getValue(type);
					List<String> questions = pickQuestions(allQuestions);
					//these are synchronized so no worrying about concurrency issues
					lessonInstanceData.addQuestionSetId(dataSnapshot.getKey());
					lessonInstanceData.addQuestionIds(questions);
					lessonInstanceData.addInterestLabel(interestLabel);
					if (questionSetsRetrieved.incrementAndGet() == questionSetIDs.size()){
						//all listeners have completed so continue
						saveInstance();
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
				}
			});
		}
	}

	private void saveInstance(){
		//shouldn't happen
		if (lessonInstanceData.questionCount() == 0){
			Log.d("TAG","question count is 0");
			lessonListener.onLessonCreated();
			return;
		}

		lessonInstanceData.setCreatedTimeStamp(System.currentTimeMillis());

		String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		DatabaseReference lessonInstanceRef = db.getReference(
				FirebaseDBHeaders.LESSON_INSTANCES + "/" + userID + "/" + lessonKey);
		String key = lessonInstanceRef.push().getKey();
		lessonInstanceData.setId(key);
		lessonInstanceRef.child(key).setValue(lessonInstanceData);

		//end of flow
		lessonListener.onLessonCreated();
	}

	//ひとつのクエリーで複数のエンティティを入れる必要があるかも？？
	private String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}

	//choose random questions that will be used for this instance
	private List<String> pickQuestions(List<List<String>> questionSet){
		Random random = new Random(System.currentTimeMillis());
		List<String> questionIDs = new ArrayList<>();
		for (List<String> questionVariations : questionSet){
			int index = random.nextInt(questionVariations.size());
			String questionToAdd = questionVariations.get(index);
			questionIDs.add(questionToAdd);
		}
		return questionIDs;
	}

	//override this if we want to add generic questions to the beginning
	protected List<List<String>> getGenericQuestionIDSets(){
		return new ArrayList<>(1);
	}

	//since we are requesting IDs for generic questions, we need some way of having
	//the question already saved in the DB.
	//this will be called by the maintenance team to pre-populate
	//generic questions
	void saveGenericQuestions(){
		List<QuestionData> questions = getGenericQuestions();
		for (QuestionData data : questions){
			String id = data.getId();
			if (id == null){
				Log.d(TAG, "Generic question ID is null");
				continue;
			}
			db.getReference(
					FirebaseDBHeaders.QUESTIONS + "/" +
							id
			).setValue(data);
		}
	}

	protected List<QuestionData> getGenericQuestions(){
		return new ArrayList<>();
	}

	
}
