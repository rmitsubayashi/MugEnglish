package com.linnca.pelicann.lessongenerator;

import android.util.Log;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

//this class (inherited classes) will create the questions for the lesson
//the first time around.
//from the second time on, we can just read the questions in from the db
public abstract class Lesson {
	protected static final String TAG = "lessonGenerator";
	protected final String TOPIC_GENERIC_QUESTION = "一般問題";
	//there are lessons that need to access the database,
	//so make this protected
	protected final Database db = new FirebaseDB();
	protected String lessonKey;
	//the lesson instance we will be creating
	private final LessonInstanceData lessonInstanceData = new LessonInstanceData();
    //vocabulary words for the lesson instance
    //(we have these in a separate location in the db)
    private final List<String> lessonInstanceVocabularyWordIDs = new ArrayList<>();
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
	protected int questionSetsToPopulate;
	//how many question sets we have left to populate
	private int questionSetsLeftToPopulate;
	//a category to search for so we don't have to search every user interest
	protected int categoryOfQuestion;
	//indicates whether we have already searched for interests related to the user.
	//this prevents an infinite loop
	private boolean relatedUserInterestsSearched = false;
	//how many related interests to search for each interest
	private final int relatedUserInterestsToSearch = 3;
	//what to do after we finish creating an instance
	private final LessonListener lessonListener;

	private WikiBaseEndpointConnector connector = null;

	public interface LessonListener {
		void onLessonCreated();
	}
	
	
	protected Lesson(WikiBaseEndpointConnector connector, LessonListener lessonListener){
		this.connector = connector;
		this.lessonListener = lessonListener;
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
        List<String> genericQuestionVocabularyIDs = getGenericQuestionVocabularyIDs();
        lessonInstanceVocabularyWordIDs.addAll(genericQuestionVocabularyIDs);
		if (getSPARQLQuery().equals("")){
			//we have a lesson without any dynamic questions
			saveInstance();
		} else {
			questionSetsLeftToPopulate = questionSetsToPopulate;
			//now populate dynamic questions
			populateUserInterests();
		}
	}

	private void populateUserInterests(){
		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onUserInterestsQueried(List<WikiDataEntryData> queriedUserInterests) {
				allUserInterests = new HashSet<>(queriedUserInterests.size());
				//have the size set to the maximum size after getting related user interests.
				//we populate this asynchronously when grabbing related user interests
				userInterests = Collections.synchronizedSet(new HashSet<WikiDataEntryData>(
						queriedUserInterests.size() * relatedUserInterestsToSearch)
				);
				for (WikiDataEntryData interest : queriedUserInterests){
					//filter by category so we don't have to search for user interests
					// that are guaranteed not to work
					if (interest.getClassification() == categoryOfQuestion ||
							interest.getClassification() == WikiDataEntryData.CLASSIFICATION_NOT_SET) {
						userInterests.add(interest);
					}
					//either way save the user interest because we will need it
					//later when we search related interests
					allUserInterests.add(interest);
				}
				populateUserQuestionHistory();
			}
		};
		db.getUserInterests(onResultListener);
	}

	//we need to skip over questions the user has already solved
	private void populateUserQuestionHistory(){
		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
				for (LessonInstanceData instanceData : lessonInstances){
					userQuestionHistory.addAll(instanceData.getQuestionSetIds());
				}

				fillQuestionsFromDatabase();
			}
		};
		db.getLessonInstances(lessonKey, onResultListener);
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

		//prevent the same user interests popping up over and over.
		//this is not a concern about repeated instances of a single theme.
		//but more of a problem when the user starts 10 themes and 9 of them include
		//Leonardo Dicaprio because he is first on the list in the database.
		//set -> list
		final List<WikiDataEntryData> userInterestList = new ArrayList<>(userInterests);
		Collections.shuffle(userInterestList);
		//we don't want to match any question the user has already had
		// or any questions already in this instance
		List<String> questionSetIDsToAvoid = new ArrayList<>(userQuestionHistory.size() + questionSetIDs.size());
		questionSetIDsToAvoid.addAll(userQuestionHistory);
		questionSetIDsToAvoid.addAll(questionSetIDs);
		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onQuestionsQueried(List<String> questionSetIDsFound, List<WikiDataEntryData> userInterestsSearched) {
				userInterestsChecked.addAll(userInterestsSearched);
				questionSetIDs.addAll(questionSetIDsFound);
				questionSetsLeftToPopulate -= questionSetIDsFound.size();
				// == 0 but just in case
				if (questionSetsLeftToPopulate <= 0){
					//we are done getting questions
					//so save them in the db
					getQuestionDataFromQuestionSetIDs();
				} else {
					//we still need to create questions.
					CreateQuestionHelper helper = new CreateQuestionHelper();
					//we can ignore the warning on Android Studio
					helper.start();
				}
			}
		};
		db.searchQuestions(lessonKey, userInterestList, questionSetsLeftToPopulate,
				questionSetIDsToAvoid, onResultListener);
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
	 * 1. write your createQuestionsFromResults, accessing the database
	 * 2. put the saveQuestionsInDB() inside the db listener
	 * see the 'play/do sports' examples
	 */
    protected void accessDBWhenCreatingQuestions(){
        //do something here
        //`````````//
		createQuestionsFromResults();
		saveNewQuestions();
	}

	//saves the newly made questions in the database
	// and the question ID list we have for this instance.
	//called after QuestionData is populated.
	//we may create more questions than the user will be getting,
	//but this is so all questions possible for one lesson are created.
	protected void saveNewQuestions(){
    	OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onQuestionSetAdded(String questionSetKey, List<List<String>> questionIDs, String interestLabel, List<String> vocabularyWordKeys) {
				//only add to the user's current set of questions if
				//less than the remaining question count.
				if (questionSetsLeftToPopulate != 0) {
					List<String> instanceQuestions = pickQuestions(questionIDs);
					lessonInstanceData.addQuestionIds(instanceQuestions);
					lessonInstanceData.addQuestionSetId(questionSetKey);
					lessonInstanceData.addInterestLabel(interestLabel);
					if (vocabularyWordKeys != null)
						lessonInstanceVocabularyWordIDs.addAll(vocabularyWordKeys);
					questionSetsLeftToPopulate--;
				}
			}

			@Override
			public void onQuestionsAdded() {
				if (questionSetsLeftToPopulate != 0 && relatedUserInterestsSearched) {
					fillRemainingQuestions();
				} else if (questionSetsLeftToPopulate != 0 && !relatedUserInterestsSearched){
					populateRelatedUserInterests();
				}
				else {
					getQuestionDataFromQuestionSetIDs();
				}
			}
		};

    	db.addQuestions(lessonKey, newQuestions, onResultListener);

	}

	//we couldn't populate the questions with the user interests so try with relevant interests
	// that we can grab using the recommendation map
	private void populateRelatedUserInterests(){
		//mark this as true so we don't call this again
		relatedUserInterestsSearched = true;
		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onRelatedUserInterestsQueried(List<WikiDataEntryData> relatedUserInterests) {
				//we are going to go back and search again with the related user interests,
				//so reset everything
				newQuestions.clear();
				userInterests.clear();
				//now go back
				userInterests.addAll(relatedUserInterests);
				fillQuestionsFromDatabase();
			}
		};
		db.getRelatedUserInterests(allUserInterests, categoryOfQuestion,
				relatedUserInterestsToSearch, onResultListener);
	}

	//this is for if we can't populate the questions with just the user interests.
	//first, we check any questions in the db non-related to the user.
	//if that doesn't work, then repeat the user's existing questions
	private void fillRemainingQuestions(){
		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onRandomQuestionsQueried(List<String> questionSetIDsQueried) {
				for (String questionSetID : questionSetIDsQueried){
					questionSetIDs.add(questionSetID);
					questionSetsLeftToPopulate--;
					if (questionSetsLeftToPopulate == 0){
						break;
					}
				}

				if (questionSetsLeftToPopulate != 0){
					addQuestionsFromUserQuestionHistory();
				}
				//now we are done with setting up all question set IDs fo this instance.
				//now grab all the data required for creating the instance
				getQuestionDataFromQuestionSetIDs();


			}
		};
		List<String> newQuestionSetIDs = lessonInstanceData.getQuestionSetIds();
		List<String> questionSetIDsToAvoid = new ArrayList<>(
				userQuestionHistory.size() + questionSetIDs.size() +
						newQuestionSetIDs.size()
		);
		//this stores the IDs we created for this instance.
		//since we already saved them in the database, we need to
		//add these so we can avoid them
		questionSetIDsToAvoid.addAll(newQuestionSetIDs);
		questionSetIDsToAvoid.addAll(userQuestionHistory);
		//this stores all the IDs we just fetched from the database
		questionSetIDsToAvoid.addAll(questionSetIDs);
		db.getRandomQuestions(lessonKey, userQuestionHistory.size(), questionSetIDsToAvoid,
				questionSetsToPopulate, onResultListener);
	}

	private void addQuestionsFromUserQuestionHistory(){
		//last resort, populate with already created questions.
		//this happens when the user has every question
		// stocked in the database.

		//make it a list so we can shuffle
		List<String> userQuestionHistoryList = new ArrayList<>(userQuestionHistory);
		Collections.shuffle(userQuestionHistoryList);
		//no need to check if the user's question history is more than the remaining question count
		//because it is guaranteed to be at least equal
		for (String questionSetID : userQuestionHistoryList){
			//just making sure.
			//we don't want duplicate questions
			if (!questionSetIDs.contains(questionSetID)) {
				questionSetIDs.add(questionSetID);
				questionSetsLeftToPopulate--;
			}

			//we are finished populating questions
			if (questionSetsLeftToPopulate == 0)
				break;
		}
	}

	private void getQuestionDataFromQuestionSetIDs(){
		//if we don't need to search for question set data
		// (all questions are newly created)
		if (questionSetIDs.size() == 0){
			saveInstance();
			return;
		}
		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onQuestionSetQueried(String questionSetKey, List<List<String>> questionIDs, String interestLabel, List<String> vocabularyWordKeys) {
				lessonInstanceData.addQuestionSetId(questionSetKey);
				List<String> instanceQuestions = pickQuestions(questionIDs);
				lessonInstanceData.addQuestionIds(instanceQuestions);
				lessonInstanceData.addInterestLabel(interestLabel);
				if (vocabularyWordKeys != null)
					lessonInstanceVocabularyWordIDs.addAll(vocabularyWordKeys);
			}

			@Override
			public void onQuestionSetsQueried() {
				saveInstance();
			}
		};

		db.getQuestionSets(questionSetIDs, onResultListener);
	}

	private void saveInstance(){
		//shouldn't happen
		if (lessonInstanceData.questionCount() == 0){
			Log.d("TAG","question count is 0");
			lessonListener.onLessonCreated();
			return;
		}

		lessonInstanceData.setCreatedTimeStamp(System.currentTimeMillis());

		OnResultListener onResultListener = new OnResultListener() {
			@Override
			public void onLessonInstanceAdded() {
				lessonListener.onLessonCreated();
			}
		};
		db.addLessonInstance(lessonKey, lessonInstanceData, lessonInstanceVocabularyWordIDs,
				onResultListener);
	}

	//will we ever have multiple entities per query?
	private String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}

	//choose random questions that will be used for this instance
	private List<String> pickQuestions(List<List<String>> questionSet){
		Random random = new Random();
		List<String> questionIDs = new ArrayList<>();
		for (List<String> questionVariations : questionSet){
			int index = random.nextInt(questionVariations.size());
			String questionToAdd = questionVariations.get(index);
			questionIDs.add(questionToAdd);
		}
		return questionIDs;
	}

	//override these if we want to add generic questions to the beginning
	protected List<List<String>> getGenericQuestionIDSets(){
		return new ArrayList<>(1);
	}
    protected List<QuestionData> getGenericQuestions(){
        return new ArrayList<>(1);
    }
    protected List<String> getGenericQuestionVocabularyIDs(){return new ArrayList<>(1);}
    protected List<VocabularyWord> getGenericQuestionVocabulary(){return new ArrayList<>(1);}

	//since we are requesting IDs for generic questions, we need some way of having
	//the question already saved in the DB.
	//this will be called by the maintenance team to pre-populate
	//generic questions
	void saveGenericQuestions(){
		List<QuestionData> questions = getGenericQuestions();

        List<VocabularyWord> vocabularyWords = getGenericQuestionVocabulary();

        db.addGenericQuestions(questions, vocabularyWords);
	}
}
