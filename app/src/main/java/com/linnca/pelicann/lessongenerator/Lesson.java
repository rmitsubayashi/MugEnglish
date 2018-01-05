package com.linnca.pelicann.lessongenerator;

import android.content.Context;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//this class (inherited classes) will create the questions for the lesson
//the first time around.
//from the second time on, we can just read the questions in from the db
public abstract class Lesson {
	protected static final String TAG = "lessonGenerator";
	//there are lessons that need to access the database,
	//so make this protected
	protected Database db;
	//each lesson will have a unique key
	protected String lessonKey;
	//the lesson instance we will be creating
	private final LessonInstanceData lessonInstanceData = new LessonInstanceData();
	//how we will order the questions
	protected int questionOrder;
    //vocabulary words for the lesson instance
    //(we have these in a separate location in the db)
    private final List<String> lessonInstanceVocabularyWordIDs = new ArrayList<>();
    //any question sets we only have the IDs of, so we can grab the data later.
	//for now, used when populating questions from the user's question history
	private final List<String> questionSetIDs = new ArrayList<>();
    //where we will populate new questions for this instance
	// (+ extra questions not needed in the current instance)
	protected final List<QuestionSetData> newQuestions = new ArrayList<>();
	//question set ids that are chosen by the user's interests.
	//we will increment this to identify popular question sets for this lesson
	private final List<String> questionSetIDsToIncrementCount = new ArrayList<>();
	//interests to search
	private Set<WikiDataEntity> userInterests;
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
	//what to do after we finish creating an instance
	private final LessonListener lessonListener;

	private EndpointConnectorReturnsXML connector = null;
	//to detect network interruptions
	private Context context;

	public interface LessonListener {
		void onLessonCreated();
		void onNoConnection();
	}

	protected Lesson(EndpointConnectorReturnsXML connector, Database db, LessonListener lessonListener){
		this.connector = connector;
		this.lessonListener = lessonListener;
		this.db = db;
	}

	// 1. check if a question already exists in the database
	// 2. create and fill the rest of the questions by querying WikiData
	// 3. save the new questions in the DB
	public void createInstance(Context context) {
		this.context = context;
		startFlow();
	}

	//the next method is inside the previous method so we can make these
	//asynchronous methods act synchronously
	private void startFlow(){
		//fill generic questions first
		addGenericQuestionIDsToInstance();
        addGenericVocabularyIDsToInstance();
		if (getSPARQLQuery().equals("")){
			//we have a lesson without any dynamic questions
			saveInstance();
		} else {
			//TODO adjust questions left to lower (too many is boring)
			questionSetsLeftToPopulate = 2;
			//populate dynamic questions
			populateUserInterests();
		}
	}

	private void populateUserInterests(){
		OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onUserInterestsQueried(List<WikiDataEntity> queriedUserInterests) {
				userInterests = Collections.synchronizedSet(new HashSet<WikiDataEntity>(
						queriedUserInterests.size())
				);
				for (WikiDataEntity interest : queriedUserInterests){
					//filter by category so we don't have to search for user interests
					// that are guaranteed not to work
					if (interest.getClassification() == categoryOfQuestion ||
							interest.getClassification() == WikiDataEntity.CLASSIFICATION_NOT_SET) {
						userInterests.add(interest);
					}
				}

				populateSimilarUserInterests();
			}

			@Override
			public void onNoConnection(){
				lessonListener.onNoConnection();
			}
		};
		db.getUserInterests(context, false, onDBResultListener);
	}

	private void populateSimilarUserInterests(){
		final AtomicInteger userInterestsQueried = new AtomicInteger(0);
		final int userInterestSize = userInterests.size();
		OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onSimilarUserInterestsQueried(List<WikiDataEntity> userInterests) {
				Lesson.this.userInterests.addAll(userInterests);

				if (userInterestsQueried.incrementAndGet() == userInterestSize){
					populateUserQuestionHistory();
				}
			}
		};
		for (WikiDataEntity interest  : userInterests){
			String interestID = interest.getWikiDataID();
			db.getSimilarInterest(interestID, onDBResultListener);
		}
	}

	//we need to skip over questions the user has already solved
	private void populateUserQuestionHistory(){
		OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
				for (LessonInstanceData instanceData : lessonInstances){
					userQuestionHistory.addAll(instanceData.questionSetIds());
				}

				fillQuestionsFromDatabase();
			}
			@Override
			public void onNoConnection(){
				lessonListener.onNoConnection();
			}
		};
		db.getLessonInstances(context, lessonKey, false, onDBResultListener);
	}

	//we should try fetching already created questions from the database
	// by matching the user's interests. if the user has not had that question yet,
	// add it to the list of questions.
	private void fillQuestionsFromDatabase(){
		if (userInterests.size() == 0){
			//skip trying to fetch questions from db/wikiData.
			fillRemainingQuestions();
			return;
		}

		//prevent the same user interests popping up over and over by shuffling the list.
		//this is not a concern about repeated instances of a single theme.
		//but more of a problem when the user starts 10 lessons and 9 of them include
		//Leonardo Dicaprio because he is first on the list in the database.
		//set -> list so we can shuffle
		final List<WikiDataEntity> userInterestList = new ArrayList<>(userInterests);
		Collections.shuffle(userInterestList);
		//we don't want to match any question the user has already had
		List<String> questionSetIDsToAvoid = new ArrayList<>(userQuestionHistory);
		OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onQuestionsQueried(List<QuestionSet> questionSetsFound, List<WikiDataEntity> userInterestsSearched) {
				for (QuestionSet set : questionSetsFound) {
					lessonInstanceData.addQuestionSet(set, true);
					questionSetIDsToIncrementCount.add(set.getKey());
					if (set.getVocabularyIDs() != null)
						lessonInstanceVocabularyWordIDs.addAll(set.getVocabularyIDs());
					questionSetsLeftToPopulate --;
					if (questionSetsLeftToPopulate == 0){
						break;
					}
				}

				// == 0 because the database should stop when the question sets left to populate is 0,
				// but just in case
				if (questionSetsLeftToPopulate <= 0){
					//we are done getting questions
					//so save them in the db
					getQuestionDataFromQuestionSetIDs();
				} else {
					//we still need to create questions.
					//we don't need to check for interests we've already matched or
					// know we can't match
					Set<WikiDataEntity> copy = new HashSet<>(userInterests);
					copy.removeAll(userInterestsSearched);
					searchWikiData(copy);
				}
			}

			@Override
			public void onNoConnection(){
				lessonListener.onNoConnection();
			}
		};
		db.searchQuestions(context, lessonKey, userInterestList, questionSetsLeftToPopulate,
				questionSetIDsToAvoid, onDBResultListener);
	}
	
	//検索するのは特定のentityひとつに対するクエリー
	//UNIONしてまとめて検索してもいいけど時間が異常にかかる
	protected abstract String getSPARQLQuery();
	//一つ一つのクエリーを送って、まとめる
	private void searchWikiData(Set<WikiDataEntity> interests){
		//shuffle so we don't get the same interests over and over
		ArrayList<WikiDataEntity> interestList = new ArrayList<>(interests);
		Collections.shuffle(interestList);
		ArrayList<String> allQueries = new ArrayList<>(interestList.size());
		for (WikiDataEntity interest : interestList){
			String entityID = interest.getWikiDataID();
			String query = addEntityToQuery(entityID);
			allQueries.add(query);
		}
		final int queryCt = allQueries.size();
		EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
			AtomicInteger DOMsFetched = new AtomicInteger(0);
			AtomicBoolean onStoppedCalled = new AtomicBoolean(false);
			AtomicBoolean error = new AtomicBoolean(false);
			@Override
			public boolean shouldStop() {
				//should stop either if we've got enough questions or
				// we finished checking
				return getQueryResultCt() >= questionSetsLeftToPopulate ||
						DOMsFetched.get() >= queryCt;
			}

			@Override
			public void onStop(){
				//only call once
				if (!onStoppedCalled.getAndSet(true)) {
					if (!error.get()) {
						accessDBWhenCreatingQuestions();
					} else {
						lessonListener.onNoConnection();
					}
				}
			}

			@Override
			public void onFetchDOM(Document result) {
				DOMsFetched.incrementAndGet();
				if (!onStoppedCalled.get()) {
					processResultsIntoClassWrappers(result);
				}
			}

			@Override
			public void onError(){
				error.set(true);
			}
		};
		try {
			connector.fetchDOMFromGetRequest(onFetchDOMListener, allQueries);
		} catch (Exception e){
			//if we couldn't connect, just get questions from the database
			fillRemainingQuestions();
		}
	}

	//getQueryResultCt, processResultsIntoClassWrappers, and createQuestionsFromResults
	// should be synchronized because multiple threads may access the list used in these
	// methods.
	//a synchronized list doesn't lock the list during iterations (createQuestionsFromResults),
	// so still causes concurrent modification exceptions..
	protected abstract int getQueryResultCt();

	//wrap the data into usable classes.
	//each lesson has different data, so all the classes are unique
	protected abstract void processResultsIntoClassWrappers(Document document);

	//generate questions using the data we got
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
    	OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onQuestionSetAdded(QuestionSet questionSet) {
				//only add to the user's current set of questions if
				//less than the remaining question count.
				if (questionSetsLeftToPopulate != 0) {
					lessonInstanceData.addQuestionSet(questionSet, true);
					if (questionSet.getVocabularyIDs() != null)
						lessonInstanceVocabularyWordIDs.addAll(questionSet.getVocabularyIDs());
					questionSetsLeftToPopulate--;
					questionSetIDsToIncrementCount.add(questionSet.getKey());
				}
			}

			@Override
			public void onQuestionsAdded() {
				if (questionSetsLeftToPopulate <= 0) {
					//we filled enough questions
					getQuestionDataFromQuestionSetIDs();
				} else {
					//we need to get more questions
					fillRemainingQuestions();
				}
			}

			//saving new questions in offline state already handled by FireBase
			// (FireBase just queues all write operations for the next time
			// the user is connected).
			//no need to notify if the user is not connected because
			// we can handle it in the next methods
		};

    	db.addQuestions(lessonKey, newQuestions, onDBResultListener);

	}

	//this is for if we can't populate the questions with just the user interests.
	//first, we check any questions in the db non-related to the user.
	//if that doesn't work, then repeat the user's existing questions
	private void fillRemainingQuestions(){
		OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onPopularQuestionSetsQueried(List<QuestionSet> questionSetsQueried) {
				for (QuestionSet questionSet : questionSetsQueried){
					lessonInstanceData.addQuestionSet(questionSet, false);
					if (questionSet.getVocabularyIDs() != null)
						lessonInstanceVocabularyWordIDs.addAll(questionSet.getVocabularyIDs());
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

			@Override
			public void onNoConnection(){
				lessonListener.onNoConnection();
			}
		};
		List<String> questionSetIDsToAvoid = new ArrayList<>(
				userQuestionHistory.size() + lessonInstanceData.questionSetIds().size()
		);
		//this stores the IDs we created for this instance.
		//since we already saved them in the database, we need to
		//add these so we can avoid them
		questionSetIDsToAvoid.addAll(lessonInstanceData.questionSetIds());
		questionSetIDsToAvoid.addAll(userQuestionHistory);
		db.getPopularQuestionSets(context, lessonKey, questionSetIDsToAvoid,
				questionSetsLeftToPopulate, onDBResultListener);
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
			if (!lessonInstanceData.questionSetIds().contains(questionSetID)) {
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
		OnDBResultListener onDBResultListener = new OnDBResultListener() {
			@Override
			public void onQuestionSetsQueried(List<QuestionSet> questionSets) {
				for (QuestionSet questionSet : questionSets){
					//for now, the only questions that need to be fetched are
					// questions from the user's question history, so we can assume
					// partOfPopularityRating = false
					lessonInstanceData.addQuestionSet(questionSet, false);
					if (questionSet.getVocabularyIDs() != null)
						lessonInstanceVocabularyWordIDs.addAll(questionSet.getVocabularyIDs());
				}

				saveInstance();
			}

			@Override
			public void onNoConnection(){
				lessonListener.onNoConnection();
			}
		};

		db.getQuestionSets(context, lessonKey, questionSetIDs, onDBResultListener);
	}

	private void saveInstance(){
		lessonInstanceData.setCreatedTimeStamp(System.currentTimeMillis());
		lessonInstanceData.setQuestionOrder(questionOrder);
		lessonInstanceData.setLessonKey(lessonKey);

		//shouldn't happen
		if (lessonInstanceData.questionCount() == 0){
			lessonListener.onLessonCreated();
			return;
		}

		OnDBResultListener onLessonInstanceAddedResultListener = new OnDBResultListener() {
			@Override
			public void onLessonInstanceAdded() {
				lessonListener.onLessonCreated();
			}

			@Override
			public void onNoConnection(){
				//this will still queue the lesson instance to be updated (FireBase).
				//so when the user regains connection,
				// the new lesson instance show up
				lessonListener.onNoConnection();
			}
		};
		db.addLessonInstance(context, lessonInstanceData,
				lessonInstanceVocabularyWordIDs,
				onLessonInstanceAddedResultListener);

		//this can be asynchronous
		OnDBResultListener onQuestionSetCountChangeResultListener = new OnDBResultListener() {
			@Override
			public void onQuestionSetCountChanged() {
				super.onQuestionSetCountChanged();
			}
		};
		for (String questionSetID : questionSetIDsToIncrementCount){
			db.changeQuestionSetCount(lessonKey, questionSetID, 1, onQuestionSetCountChangeResultListener);
		}
	}

	//will we ever have multiple entities per query?
	private String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}

	//override these if we want to add generic questions to the beginning or end.
	//generally, pre questions are introductory questions and post questions are applications/summaries
	protected List<List<QuestionData>> getPreGenericQuestions(){
		return new ArrayList<>(1);
	}
    protected List<List<QuestionData>> getPostGenericQuestions(){ return new ArrayList<>(1);}
    //if we have three items like good morning, good afternoon, and good evening,
	//we don't want them to appear in teh same order every instance.
	//so, shuffle them up AFTER we set the IDs.
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){ return;}
    //for both the pre and post
    protected List<VocabularyWord> getGenericQuestionVocabulary(){return new ArrayList<>(1);}

	//since we are requesting IDs for generic questions, we need some way of having
	//the question already saved in the DB.
	//this will be called by the maintenance team to pre-populate
	//generic questions
	void saveGenericQuestions(){
		List<List<QuestionData>> preQuestions = getPreGenericQuestions();
		List<List<QuestionData>> postQuestions = getPostGenericQuestions();
		setGenericQuestionIDs(preQuestions, postQuestions);
        List<VocabularyWord> vocabularyWords = getGenericQuestionVocabulary();
        setGenericVocabularyIDs(vocabularyWords);
        List<QuestionData> allQuestions = new ArrayList<>();
        for (List<QuestionData> questionVariations : preQuestions){
        	allQuestions.addAll(questionVariations);
		}
		for (List<QuestionData> questionVariations : postQuestions){
			allQuestions.addAll(questionVariations);
		}
        db.addGenericQuestions(allQuestions, vocabularyWords);
	}

	private void setGenericQuestionIDs(List<List<QuestionData>> preQuestions, List<List<QuestionData>> postQuestions){
		int index = 1;
		for (List<QuestionData> questionVariations : preQuestions){
			for (QuestionData question : questionVariations){
				question.setId(formatGenericQuestionID(lessonKey, index));
				index++;
			}
		}

		for (List<QuestionData> questionVariations : postQuestions){
			for (QuestionData question : questionVariations){
				question.setId(formatGenericQuestionID(lessonKey, index));
				index++;
			}
		}
	}

	private void setGenericVocabularyIDs(List<VocabularyWord> words){
		for (VocabularyWord word : words){
			String id = formatGenericQuestionVocabularyID(lessonKey, word.getWord());
			word.setId(id);
		}
	}

	private String formatGenericQuestionID(String lessonKey, int questionNumber){
		return lessonKey + "_generic" + Integer.toString(questionNumber);
	}

	private String formatGenericQuestionVocabularyID(String lessonKey, String word){
		word = word.replaceAll(" ", "_");
		return lessonKey + "_generic_" + word;
	}

	private void addGenericQuestionIDsToInstance(){
		List<List<QuestionData>> preGenericQuestions = getPreGenericQuestions();
		List<List<QuestionData>> postGenericQuestions = getPostGenericQuestions();
		setGenericQuestionIDs(preGenericQuestions, postGenericQuestions);
		shufflePreGenericQuestions(preGenericQuestions);
		//make a temporary question set so we can pick questions
		QuestionSet tempSet = new QuestionSet();
		List<List<String>> preGenericQuestionIDs = new ArrayList<>(preGenericQuestions.size());
		for (List<QuestionData> questionVariations : preGenericQuestions){
			List<String> questionVariationIDs = new ArrayList<>(questionVariations.size());
			for (QuestionData question : questionVariations){
				questionVariationIDs.add(question.getId());
			}
			preGenericQuestionIDs.add(questionVariationIDs);
		}
		tempSet.setQuestionIDs(preGenericQuestionIDs);
		List<String> pickPreGenericQuestions = tempSet.pickQuestions();
		lessonInstanceData.setPreGenericQuestionIds(pickPreGenericQuestions);
		//do the same for the post questions
		tempSet = new QuestionSet();
		List<List<String>> postGenericQuestionIDs = new ArrayList<>(postGenericQuestions.size());
		for (List<QuestionData> questionVariations : postGenericQuestions){
			List<String> questionVariationIDs = new ArrayList<>(questionVariations.size());
			for (QuestionData question : questionVariations){
				questionVariationIDs.add(question.getId());
			}
			postGenericQuestionIDs.add(questionVariationIDs);
		}
		tempSet.setQuestionIDs(postGenericQuestionIDs);
		List<String> pickPostGenericQuestions = tempSet.pickQuestions();
		lessonInstanceData.setPostGenericQuestionIds(pickPostGenericQuestions);
	}

	private void addGenericVocabularyIDsToInstance(){
		List<VocabularyWord> vocabularyWords = getGenericQuestionVocabulary();
		setGenericVocabularyIDs(vocabularyWords);
		List<String> genericVocabularyIDs = new ArrayList<>(vocabularyWords.size());
		for (VocabularyWord word : vocabularyWords){
			genericVocabularyIDs.add(word.getId());
		}
		lessonInstanceVocabularyWordIDs.addAll(genericVocabularyIDs);
	}
}
