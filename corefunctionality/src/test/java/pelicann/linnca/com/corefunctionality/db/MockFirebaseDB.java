package pelicann.linnca.com.corefunctionality.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

//we are creating a mock database for testing purposes.
//some of the data is narrowed down.
//for example, since we will only ever be testing one lesson at a time,
// we don't separate lesson instances by lesson ID (where in the actual database, it does)
public class MockFirebaseDB extends Database {
    //all the data will be in public variables
    //so we can easily access them when testing
    public List<WikiDataEntity> userInterests = new ArrayList<>();
    //Wikidata ID, all data pertaining to the wikidata id.
    public final Map<String, List<EntityPropertyData>> entityPropertyData = new HashMap<>();
    //ID -> lesson instance
    public final List<LessonInstanceData> lessonInstances = new ArrayList<>();

    @Override
    public String getUserID() {
        return "user1";
    }

    @Override
    public void cleanup() {}

    @Override
    public void cleanupDB(){}

    @Override
    public void searchEntityPropertyData(NetworkConnectionChecker networkConnectionChecker, String lessonKey, List<WikiDataEntity> userInterests, int toPopulate,
                                OnDBResultListener onDBResultListener) {
        List<EntityPropertyData> entitiesToReturn = new ArrayList<>(toPopulate);
        List<WikiDataEntity> userInterestsChecked = new ArrayList<>(userInterests.size());
        if (toPopulate == 0){
            onDBResultListener.onEntityPropertyDataSearched(entitiesToReturn, userInterestsChecked);
        }
        for (WikiDataEntity userInterest : userInterests){
            List<EntityPropertyData> set = entityPropertyData.get(userInterest.getWikiDataID());
            if (set != null &&
                    set.size() > 0 &&
                    toPopulate != 0) {
                entitiesToReturn.add(set.get(0));
                toPopulate--;
                userInterestsChecked.add(userInterest);
            }
            if (toPopulate == 0) {
                break;
            }
        }

        onDBResultListener.onEntityPropertyDataSearched(entitiesToReturn, userInterestsChecked);

    }

    @Override
    public void addEntityPropertyData(String lessonKey, List<EntityPropertyData> entities, OnDBResultListener onDBResultListener) {
        for (EntityPropertyData dataToAdd : entities){
            List<EntityPropertyData> addList = entityPropertyData.get(dataToAdd.getWikidataID());
            if (addList == null){
                addList = new ArrayList<>();
                entityPropertyData.put(dataToAdd.getWikidataID(), addList);
            }
            addList.add(dataToAdd);
            onDBResultListener.onEntityPropertyDataAdded(dataToAdd);
        }
        onDBResultListener.onAllEntityPropertyDataAdded();
    }

    @Override
    public void getRandomEntityPropertyData(NetworkConnectionChecker networkConnectionChecker,
                                            String lessonKey, final List<EntityPropertyData> toAvoid,
                                           final int toPopulate,
                                           final OnDBResultListener onDBResultListener){
        List<EntityPropertyData> result = new ArrayList<>(toPopulate);
        for (Map.Entry<String, List<EntityPropertyData>> entry : entityPropertyData.entrySet()){
            List<EntityPropertyData> set = entry.getValue();
            if (set != null &&
                    set.size() != 0 &&
                    Collections.disjoint(set, toAvoid) ){
                result.add(set.get(0));
            }
            if (result.size() == toPopulate){
                break;
            }
        }
        onDBResultListener.onRandomEntityPropertyDataQueried(result);
    }

    private int incrementLessonInstance = 1;
    @Override
    public void addLessonInstance(NetworkConnectionChecker networkConnectionChecker, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs, OnDBResultListener onDBResultListener) {
        String id = "testID" + incrementLessonInstance++;
        lessonInstanceData.setId(id);
        lessonInstances.add(lessonInstanceData);
        onDBResultListener.onLessonInstanceAdded();
    }

    @Override
    public void getLessonInstances(NetworkConnectionChecker networkConnectionChecker, String lessonKey, boolean persistentConnection, OnDBResultListener onDBResultListener) {
        // we are assuming a single lesson so we don't need to filter by lesson key
        List<LessonInstanceData> instancesList = new ArrayList<>(lessonInstances);
        onDBResultListener.onLessonInstancesQueried(instancesList);
    }

    @Override
    public void getMostRecentLessonInstance(NetworkConnectionChecker checker, String lessonKey,
                                                OnDBResultListener onDBResultListener){
        //first one = most recent
        onDBResultListener.onLessonInstancesQueried(lessonInstances.subList(0,1));
    }

    @Override
    public void getUserInterests(NetworkConnectionChecker networkConnectionChecker, boolean persistentConnection, OnDBResultListener onDBResultListener) {
        onDBResultListener.onUserInterestsQueried(userInterests);
    }

    @Override
    public void removeUserInterests(List<WikiDataEntity> userInterests, OnDBResultListener onDBResultListener) {
        this.userInterests.removeAll(userInterests);
        onDBResultListener.onUserInterestsRemoved();
    }

    @Override
    public void addUserInterests(NetworkConnectionChecker networkConnectionChecker, List<WikiDataEntity> userInterest, OnDBResultListener onDBResultListener) {
        this.userInterests.addAll(userInterest);
        onDBResultListener.onUserInterestsAdded();
    }

    @Override
    public void setPronunciation(String userInterestID, String pronunciation){

    }

    @Override
    public void addSimilarInterest(String fromID, WikiDataEntity toEntity){

    }

    @Override
    public void getSimilarInterest(String  wikidataID, OnDBResultListener onDBResultListener){
        onDBResultListener.onSimilarUserInterestsQueried(new ArrayList<WikiDataEntity>());
    }

    @Override
    public void addInstanceRecord(InstanceRecord record, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addAppUsageLog(AppUsageLog log) {

    }

    @Override
    public void getFirstAppUsageDate(OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getAppUsageForMonths(NetworkConnectionChecker networkConnectionChecker, String startMonthKey, String endMonthKey, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addDailyLesson(String date, OnDBResultListener onDBResultListener){}

    @Override
    public void addSport(String sportWikiDataID, String verb, String object) {

    }

    @Override
    public void getSports(Collection<String> sportWikiDataIDs, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addReportCard(String lessonKey, int correctCt, int totalCt, OnDBResultListener onDBResultListener){

    }

}