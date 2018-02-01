package pelicann.linnca.com.corefunctionality.lessoninstance;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

//this class (inherited classes) will create the questions for the lesson
//the first time around.
//from the second time on, we can just read the questions in from the db
public abstract class LessonInstanceGenerator {
    protected static final String TAG = "lessongeneration";
    //there are lessons that need to access the database,
    //so make this protected
    protected Database db;
    //each lesson will have a unique key
    protected String lessonKey;
    //how many unique entities we need entity property data for
    protected int uniqueEntities;
    //how many unique entities we have left
    private int uniqueEntitiesLeft;
    //the lesson instance we will be creating
    private final LessonInstanceData lessonInstanceData = new LessonInstanceData();
    //vocabulary words for the lesson instance
    //(we have these in a separate location in the db)
    private final List<String> lessonInstanceVocabularyWordIDs = new ArrayList<>();
    //where we will populate new entity property data for this instance
    protected final List<EntityPropertyData> newEntityPropertyData = Collections.synchronizedList(
            new ArrayList<EntityPropertyData>());
    //interests to search
    private Set<WikiDataEntity> userInterests;
    //makes sure we are not giving duplicate entity property data
    // from previous instances of the user.
    private final Set<EntityPropertyData> userPreviousEntityPropertyData = new HashSet<>();
    //a category to search for so we don't have to search every user interest.
    // i.e. people, places, other
    protected int categoryOfQuestion;
    //what to do after we finish creating an instance
    private LessonInstanceGeneratorListener lessonListener;

    private EndpointConnectorReturnsXML connector = null;
    //to detect network interruptions
    private NetworkConnectionChecker networkConnectionChecker;

    public interface LessonInstanceGeneratorListener {
        void onLessonCreated(LessonInstanceData lessonInstanceData);
        void onNoConnection();
    }

    protected LessonInstanceGenerator(){
    }

    // 1. check if a question already exists in the database
    // 2. create and fill the rest of the questions by querying WikiData
    // 3. save the new questions in the DB
    public void createInstance(EndpointConnectorReturnsXML connector, Database db,
                               LessonInstanceGeneratorListener lessonListener,
                               NetworkConnectionChecker networkConnectionChecker) {
        this.connector = connector;
        this.lessonListener = lessonListener;
        this.db = db;
        this.networkConnectionChecker = networkConnectionChecker;
        uniqueEntitiesLeft = uniqueEntities;
        startFlow();
    }

    //the next method is inside the previous method so we can make these
    //asynchronous methods act synchronously
    private void startFlow(){
        if (getSPARQLQuery().equals("")){
            //we have a lesson without any dynamic content
            saveInstance();
        } else {
            //populate dynamic content
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
        db.getUserInterests(networkConnectionChecker, false, onDBResultListener);
    }

    //the similar interests are currently based on Bing search results.
    //we treat similar interests as if they are the user's interests
    private void populateSimilarUserInterests(){
        final AtomicInteger userInterestsQueried = new AtomicInteger(0);
        final int userInterestSize = userInterests.size();
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onSimilarUserInterestsQueried(List<WikiDataEntity> userInterests) {
                LessonInstanceGenerator.this.userInterests.addAll(userInterests);

                if (userInterestsQueried.incrementAndGet() == userInterestSize){
                    populateUserPreviousEntityData();
                }
            }
        };
        for (WikiDataEntity interest  : userInterests){
            String interestID = interest.getWikiDataID();
            db.getSimilarInterest(interestID, onDBResultListener);
        }
    }

    //we need to skip over entity data the user has already had
    private void populateUserPreviousEntityData(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                for (LessonInstanceData instanceData : lessonInstances){
                    userPreviousEntityPropertyData.addAll(instanceData.getEntityPropertyData());
                }

                getEntityDataFromDatabase();
            }
            @Override
            public void onNoConnection(){
                lessonListener.onNoConnection();
            }
        };
        db.getLessonInstances(networkConnectionChecker, lessonKey, false, onDBResultListener);
    }

    //we should try fetching already created data from the database
    // by matching the user's interests. if the user has not had that data yet,
    // add it to the list of entity data.
    private void getEntityDataFromDatabase(){
        if (userInterests.size() == 0){
            //skip trying to fetch entity data from db/wikiData.
            fillRemainingEntityPropertyData();
            return;
        }

        //prevent the same user interests popping up over and over by shuffling the list.
        //ex. the user starts 10 lessons and 9 of them include
        //Leonardo Dicaprio because he is first on the list in the database.
        //set -> list so we can shuffle
        final List<WikiDataEntity> userInterestList = new ArrayList<>(userInterests);
        Collections.shuffle(userInterestList);
        //we don't want to match any question the user has already had
        List<EntityPropertyData> entityPropertyDataToAvoid = new ArrayList<>(userPreviousEntityPropertyData);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataSearched(List<EntityPropertyData> entityPropertyDataFound, List<WikiDataEntity> userInterestsSearched) {
                for (EntityPropertyData data : entityPropertyDataFound) {
                    lessonInstanceData.addEntityPropertyData(data);
                    uniqueEntitiesLeft --;
                    if (uniqueEntitiesLeft == 0){
                        break;
                    }
                }

                // == 0 because the database should stop when the question sets left to populate is 0,
                // but just in case
                if (uniqueEntitiesLeft <= 0){
                    //we are done getting questions
                    //so save them in the db
                    saveInstance();
                } else {
                    //we still need to get more data.
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

        db.searchEntityPropertyData(networkConnectionChecker, lessonKey, userInterestList, uniqueEntitiesLeft,
                entityPropertyDataToAvoid, onDBResultListener);
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
                //should stop either if we've got enough data or
                // we finished checking
                return newEntityPropertyData.size() >= uniqueEntitiesLeft||
                        DOMsFetched.get() >= queryCt;
            }

            @Override
            public void onStop(){
                //only call once
                if (!onStoppedCalled.getAndSet(true)) {
                    if (!error.get()) {
                        saveNewEntityPropertyData();
                    } else {
                        lessonListener.onNoConnection();
                    }
                }
            }

            @Override
            public void onFetchDOM(Document result) {
                DOMsFetched.incrementAndGet();
                if (!onStoppedCalled.get()) {
                    processResultsIntoEntityPropertyData(result);
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
            fillRemainingEntityPropertyData();
        }
    }

    //processResultsIntoClassWrappers
    // should be synchronized because multiple threads may access the list used in these
    // methods.
    //a synchronized list doesn't lock the list during iterations,
    // so still causes concurrent modification exceptions..
    protected synchronized int getUniqueNewEntityPropertyDataCt(){
        Set<String> covered = new HashSet<>(newEntityPropertyData.size());
        //iterating a synchronized list doesn't lock it,
        //so to make sure we are consistent, make a copy and iterate the copy
        List<EntityPropertyData> copy = new ArrayList<>(newEntityPropertyData);
        for (EntityPropertyData copyData : copy){
            covered.add(copyData.getWikidataID());
        }
        return covered.size();

    }

    //wrap the data into entity property data
    protected abstract void processResultsIntoEntityPropertyData(Document document);

    //saves the newly made entity property data in the database
    //we may create more data than the user will be using for this instance,
    //but we still save all so all possible data for one entity are created.
    private void saveNewEntityPropertyData(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataAdded(EntityPropertyData data) {
                //only add to the user's current set of questions if
                //less than the remaining question count.
                if (uniqueEntitiesLeft != 0 && lessonInstanceData.isUniqueEntity(data)) {
                    System.out.println("added data to lesson instance");
                    lessonInstanceData.addEntityPropertyData(data);
                    uniqueEntitiesLeft--;
                }
            }

            @Override
            public void onAllEntityPropertyDataAdded() {
                if (uniqueEntitiesLeft <= 0) {
                    //we filled enough questions
                    saveInstance();
                } else {
                    //we need to get more questions
                    fillRemainingEntityPropertyData();
                }
            }

            //saving new questions in offline state already handled by FireBase
            // (FireBase just queues all write operations for the next time
            // the user is connected).
            //no need to notify if the user is not connected because
            // we can handle it in the next methods
        };

        db.addEntityPropertyData(lessonKey, newEntityPropertyData, onDBResultListener);

    }

    //this is for if we can't populate the lesson instance with just the user interests.
    //first, we check for any data in the db non-related to the user.
    //if that doesn't work, then repeat the user's existing data
    private void fillRemainingEntityPropertyData(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onRandomEntityPropertyDataQueried(List<EntityPropertyData> result) {
                for (EntityPropertyData data : result){
                    lessonInstanceData.addEntityPropertyData(data);
                    uniqueEntitiesLeft--;
                    if (uniqueEntitiesLeft == 0){
                        break;
                    }
                }

                if (uniqueEntitiesLeft != 0){
                    addEntityPropertyDataFromUserHistory();
                }
                //now we are done
                saveInstance();
            }

            @Override
            public void onNoConnection(){
                lessonListener.onNoConnection();
            }
        };
        List<EntityPropertyData> entityPropertyDataToAvoid = new ArrayList<>(
                userPreviousEntityPropertyData.size() + lessonInstanceData.getEntityPropertyData().size()
        );
        //this stores the IDs we created for this instance.
        //since we already saved them in the database, we need to
        //add these so we can avoid them
        entityPropertyDataToAvoid.addAll(lessonInstanceData.getEntityPropertyData());
        entityPropertyDataToAvoid.addAll(userPreviousEntityPropertyData);
        db.getRandomEntityPropertyData(networkConnectionChecker, lessonKey, entityPropertyDataToAvoid,
                uniqueEntitiesLeft, onDBResultListener);
    }

    private void addEntityPropertyDataFromUserHistory(){
        //last resort, populate with already created questions.
        //this happens when the user has every question
        // stocked in the database.

        //make it a list so we can shuffle
        List<EntityPropertyData> userHistoryList = new ArrayList<>(userPreviousEntityPropertyData);
        Collections.shuffle(userHistoryList);
        //no need to check if the user's question history is more than the remaining question count
        //because it is guaranteed to be at least equal
        for (EntityPropertyData data : userHistoryList){
            //just making sure.
            //we don't want duplicate questions
            if (lessonInstanceData.isUniqueEntity(data)) {
                lessonInstanceData.addEntityPropertyData(data);
                uniqueEntitiesLeft--;
            }

            //we are finished populating questions
            if (uniqueEntitiesLeft == 0)
                break;
        }
    }

    private void saveInstance(){
        lessonInstanceData.setLessonKey(lessonKey);
        lessonInstanceData.setTimeStamp(System.currentTimeMillis());

        //should never happen
        // as long as there are entities saved in the database
        if (lessonInstanceData.getEntityPropertyData().size() == 0){
            return;
        }

        OnDBResultListener onLessonInstanceAddedResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstanceAdded() {
                System.out.println("added lesson instance");
                //the lesson instance data adds
                lessonListener.onLessonCreated(lessonInstanceData);
            }

            @Override
            public void onNoConnection(){
                //this will still queue the lesson instance to be updated (FireBase).
                //so when the user regains connection,
                // the new lesson instance show up
                lessonListener.onNoConnection();
            }
        };
        System.out.println("adding lesson instance");
        db.addLessonInstance(networkConnectionChecker, lessonInstanceData,
                lessonInstanceVocabularyWordIDs,
                onLessonInstanceAddedResultListener);
    }

    //will we ever have multiple entities per query?
    private String addEntityToQuery(String entity){
        String query = this.getSPARQLQuery();
        return String.format(query, entity);
    }
}
