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
public abstract class InstanceGenerator {
    protected static final String TAG = "lessongeneration";
    //if there are lessons that need to access the database,
    //make this protected
    private Database db;
    //each lesson will have a unique key
    protected String lessonKey;
    //how many unique entities we need entity property data for
    protected int uniqueEntities;
    //how many unique entities we have left
    private int uniqueEntitiesLeft;
    //whether we need to reference another lesson
    protected String referenceLesson = null;
    //which property of the lesson we need to reference
    protected int referencePropertyIndex = 0;
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
    //what to do after we finish creating an instance
    private LessonInstanceGeneratorListener lessonListener;

    private EndpointConnectorReturnsXML connector = null;
    //to detect network interruptions
    private NetworkConnectionChecker networkConnectionChecker;

    public interface LessonInstanceGeneratorListener {
        void onLessonCreated(LessonInstanceData lessonInstanceData);
        void onNoConnection();
    }

    protected InstanceGenerator(){
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

        if (!requiresOtherLessonReference()) {
            //we look into the user's interests to find an entity
            startFlow();
        } else {
            //we are using the same entity as another lesson
            // (for story-telling consistency)
            getOtherLessonReference();
        }
    }

    private void getOtherLessonReference(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstanceData) {
                if (lessonInstanceData.size() != 0) {
                    LessonInstanceData instance = lessonInstanceData.get(0);
                    List<EntityPropertyData> data = instance.getEntityPropertyData();
                    //can we always assume 0?
                    EntityPropertyData referenceEntityPropertyData = data.get(0);
                    Translation referenceProperty = referenceEntityPropertyData.getPropertyAt(referencePropertyIndex);
                    WikiDataEntity referenceWikidata = new WikiDataEntity(referenceProperty.getJapanese(),"", referenceProperty.getWikidataID(), referenceProperty.getJapanese());
                    userInterests = new HashSet<>(1);
                    userInterests.add(referenceWikidata);
                    getEntityDataFromDatabase();
                } else {
                    //should not happen because this lesson is after
                    // the lesson it is referencing,
                    // but just in case
                    lessonListener.onNoConnection();
                }
            }
            @Override
            public void onNoConnection() {

            }
        };
        db.getMostRecentLessonInstance(networkConnectionChecker, referenceLesson, onDBResultListener);
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
                userInterests = Collections.synchronizedSet(new HashSet<>(queriedUserInterests));
                if (userInterests.size() == 0){
                    //no need to get similar interests
                    getEntityDataFromDatabase();
                } else {
                    populateSimilarUserInterests();
                }
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
                InstanceGenerator.this.userInterests.addAll(userInterests);
                for (WikiDataEntity entity : userInterests){
                    System.out.println(entity.getLabel());
                }
                if (userInterestsQueried.incrementAndGet() == userInterestSize){
                    getEntityDataFromDatabase();
                }
            }
        };
        for (WikiDataEntity interest  : userInterests){
            String interestID = interest.getWikiDataID();
            db.getSimilarInterest(interestID, onDBResultListener);
        }
    }

    //we should try fetching already created data from the database
    // by matching the user's interests. if the user has not had that data yet,
    // add it to the list of entity data.
    private void getEntityDataFromDatabase(){
        for (WikiDataEntity entity : userInterests){
            System.out.println(entity.getLabel());
        }
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
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataSearched(List<EntityPropertyData> entityPropertyDataFound, List<WikiDataEntity> userInterestsSearched) {
                System.out.println("size" + entityPropertyDataFound.size());
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
                    searchWikiData(copy);
                }
            }

            @Override
            public void onNoConnection(){
                lessonListener.onNoConnection();
            }
        };

        db.searchEntityPropertyData(networkConnectionChecker, lessonKey, userInterestList, uniqueEntitiesLeft,
                onDBResultListener);
    }

    //検索するのは特定のentityひとつに対するクエリー
    //UNIONしてまとめて検索してもいいけど時間が異常にかかる
    protected abstract String getSPARQLQuery();
    //一つ一つのクエリーを送って、まとめる
    private void searchWikiData(Set<WikiDataEntity> interests){
        System.out.println("searching");
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
            final AtomicInteger DOMsFetched = new AtomicInteger(0);
            final AtomicBoolean onStoppedCalled = new AtomicBoolean(false);
            final AtomicBoolean error = new AtomicBoolean(false);
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
                    System.out.println("fetched DOM");
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
                //now we are done
                saveInstance();
            }

            @Override
            public void onNoConnection(){
                lessonListener.onNoConnection();
            }
        };
        List<EntityPropertyData> entityPropertyDataToAvoid = new ArrayList<>(
                lessonInstanceData.getEntityPropertyData().size()
        );
        //this stores the IDs we created for this instance.
        //since we already saved them in the database, we need to
        //add these so we can avoid them
        entityPropertyDataToAvoid.addAll(lessonInstanceData.getEntityPropertyData());
        db.getRandomEntityPropertyData(networkConnectionChecker, lessonKey, entityPropertyDataToAvoid,
                uniqueEntitiesLeft, onDBResultListener);
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
        db.addLessonInstance(networkConnectionChecker, lessonInstanceData,
                lessonInstanceVocabularyWordIDs,
                onLessonInstanceAddedResultListener);
    }

    //will we ever have multiple entities per query?
    private String addEntityToQuery(String entity){
        String query = this.getSPARQLQuery();
        return String.format(query, entity);
    }

    private boolean requiresOtherLessonReference(){
        return referenceLesson != null;
    }
}
