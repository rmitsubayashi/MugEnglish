package pelicann.linnca.com.corefunctionality.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceAttemptRecord;
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
    public void searchEntityPropertyData(String lessonKey, List<WikiDataEntity> userInterests, int toPopulate,
                                DBEntityPropertyDataResultListener entityPropertyDataResultListener,
                                         DBConnectionResultListener connectionResultListener,
                                         NetworkConnectionChecker networkConnectionChecker) {
        List<EntityPropertyData> entitiesToReturn = new ArrayList<>(toPopulate);
        List<WikiDataEntity> userInterestsChecked = new ArrayList<>(userInterests.size());
        if (toPopulate == 0){
            entityPropertyDataResultListener.onEntityPropertyDataSearched(entitiesToReturn, userInterestsChecked);
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

        entityPropertyDataResultListener.onEntityPropertyDataSearched(entitiesToReturn, userInterestsChecked);

    }

    @Override
    public void addEntityPropertyData(List<EntityPropertyData> entities, String lessonKey,
                                      DBEntityPropertyDataResultListener entityPropertyDataResultListener) {
        for (EntityPropertyData dataToAdd : entities){
            List<EntityPropertyData> addList = entityPropertyData.get(dataToAdd.getWikidataID());
            if (addList == null){
                addList = new ArrayList<>();
                entityPropertyData.put(dataToAdd.getWikidataID(), addList);
            }
            addList.add(dataToAdd);
            entityPropertyDataResultListener.onEntityPropertyDataAdded(dataToAdd);
        }
        entityPropertyDataResultListener.onAllEntityPropertyDataAdded();
    }

    @Override
    public void getRandomEntityPropertyData(
            String lessonKey, final List<EntityPropertyData> toAvoid,
                                           final int toPopulate,
                                           final DBEntityPropertyDataResultListener entityPropertyDataResultListener,
            DBConnectionResultListener connectionResultListener,
            NetworkConnectionChecker networkConnectionChecker){
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
        entityPropertyDataResultListener.onRandomEntityPropertyDataQueried(result);
    }

    private int incrementLessonInstance = 1;
    @Override
    public void addLessonInstance(LessonInstanceData lessonInstanceData,
                                  DBLessonInstanceResultListener lessonInstanceResultListener,
                                  DBConnectionResultListener connectionResultListener,
                                  NetworkConnectionChecker networkConnectionChecker) {
        String id = "testID" + incrementLessonInstance++;
        lessonInstanceData.setId(id);
        lessonInstances.add(lessonInstanceData);
        lessonInstanceResultListener.onLessonInstanceAdded();
    }

    @Override
    public void getLessonInstances(String lessonKey, boolean persistentConnection,
                                   DBLessonInstanceResultListener lessonInstanceResultListener,
                                   DBConnectionResultListener connectionResultListener,
                                   NetworkConnectionChecker networkConnectionChecker) {
        // we are assuming a single lesson so we don't need to filter by lesson key
        List<LessonInstanceData> instancesList = new ArrayList<>(lessonInstances);
        lessonInstanceResultListener.onLessonInstancesQueried(instancesList);
    }

    @Override
    public void getMostRecentLessonInstance(String lessonKey,
                                            DBLessonInstanceResultListener lessonInstanceResultListener,
                                            DBConnectionResultListener connectionResultListener,
                                            NetworkConnectionChecker checker){
        //first one = most recent
        lessonInstanceResultListener.onLessonInstancesQueried(lessonInstances.subList(0,1));
    }

    @Override
    public void getUserInterests(boolean persistentConnection, DBUserInterestListener userInterestListener,
                                 DBConnectionResultListener connectionResultListener,
                                 NetworkConnectionChecker networkConnectionChecker) {
        userInterestListener.onUserInterestsQueried(userInterests);
    }

    @Override
    public void removeUserInterests(List<WikiDataEntity> userInterests,
                                    DBUserInterestListener userInterestListener) {
        this.userInterests.removeAll(userInterests);
        userInterestListener.onUserInterestsRemoved();
    }

    @Override
    public void addUserInterests(List<WikiDataEntity> userInterest,
                                 DBUserInterestListener userInterestListener,
                                 DBConnectionResultListener connectionResultListener,
                                 NetworkConnectionChecker networkConnectionChecker) {
        this.userInterests.addAll(userInterest);
        userInterestListener.onUserInterestsAdded();
    }

    @Override
    public void setPronunciation(String userInterestID, String pronunciation){

    }

    @Override
    public void addSimilarInterest(String fromID, WikiDataEntity toEntity){

    }

    @Override
    public void getSimilarInterest(String  wikidataID, DBSimilarUserInterestResultListener similarUserInterestResultListener){
        similarUserInterestResultListener.onSimilarUserInterestsQueried(new ArrayList<WikiDataEntity>());
    }

    @Override
    public void addInstanceAttemptRecord(InstanceAttemptRecord record,
                                         DBInstanceRecordResultListener instanceRecordResultListener) {

    }

    @Override
    public void addAppUsageLog(AppUsageLog log) {

    }

    @Override
    public void getFirstAppUsageDate(DBAppUsageResultListener appUsageResultListener) {

    }

    @Override
    public void getAppUsageForMonths(int startMonth, int startYear, int endMonth, int endYear,
                                     DBAppUsageResultListener appUsageResultListener,
                                     DBConnectionResultListener connectionResultListener,
                                     NetworkConnectionChecker networkConnectionChecker) {

    }

    @Override
    public void incrementDailyLesson(String date, DBDailyLessonResultListener dbDailyLessonResultListener){}

    @Override
    public void addSport(String sportWikiDataID, String verb, String object) {

    }

    @Override
    public void getSports(Collection<String> sportWikiDataIDs, DBSportResultListener sportResultListener) {

    }

}
