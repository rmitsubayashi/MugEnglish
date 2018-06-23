package pelicann.linnca.com.corefunctionality.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceAttemptRecord;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

//abstract class for all requests we need to make to the database.
//easier to read code and makes migrating databases easier.

//instead of returning values that we need, we are using listeners
// to trigger events that occur once we fetch/update the data.
//this is because of Firebase's real-time database nature.
//(we can't return values from a Firebase request)

public abstract class Database implements Serializable{
    protected final List<NetworkConnectionChecker> networkConnections = new ArrayList<>();
    //whatever db we use, we will always need some sort of ID
    // to identify users
    public abstract String getUserID();
    public void cleanup(){
        cleanupDB();
        cleanupNetworkConnections();
    }
    public abstract void cleanupDB();

    //since the NetworkConnectionChecker is abstract,
    // no need to make the whole method abstract
    private void cleanupNetworkConnections(){
        for (NetworkConnectionChecker connection : networkConnections){
            connection.stop();
        }
        networkConnections.clear();
    }

    //ENTITY PROPERTY DATA
    //using 'search' to distinguish from getting questions for answering.
    public abstract void searchEntityPropertyData(String lessonKey, List<WikiDataEntity> userInterests,
                                                  int toPopulate,
                                                  DBEntityPropertyDataResultListener entityPropertyDataResultListener,
                                                  DBConnectionResultListener connectionResultListener,
                                                  NetworkConnectionChecker networkConnectionChecker);
    public abstract void addEntityPropertyData(List<EntityPropertyData> data, String lessonKey, DBEntityPropertyDataResultListener entityPropertyDataResultListener);
    public abstract void getRandomEntityPropertyData(String lessonKey, List<EntityPropertyData> toAvoid, int toPopulate,
                                                     DBEntityPropertyDataResultListener entityPropertyDataResultListener,
                                                     DBConnectionResultListener connectionResultListener,
                                                     NetworkConnectionChecker networkConnectionChecker);

    //LESSON INSTANCE
    //the lesson instances are all of one user
    public abstract void addLessonInstance(LessonInstanceData lessonInstanceData,
                                           DBLessonInstanceResultListener lessonInstanceResultListener,
                                           DBConnectionResultListener connectionResultListener,
                                           NetworkConnectionChecker networkConnectionChecker);
    public abstract void getLessonInstances(String lessonKey, boolean persistentConnection,
                                            DBLessonInstanceResultListener lessonInstanceResultListener,
                                            DBConnectionResultListener connectionResultListener,
                                            NetworkConnectionChecker networkConnectionChecker);
    public abstract void getMostRecentLessonInstance(String lessonKey,
                                                     DBLessonInstanceResultListener lessonInstanceResultListener,
                                                     DBConnectionResultListener connectionResultListener,
                                                     NetworkConnectionChecker networkConnectionChecker);

    //USER INTEREST
    public abstract void getUserInterests(boolean persistentConnection,
                                          DBUserInterestListener userInterestListener,
                                          DBConnectionResultListener connectionResultListener,
                                          NetworkConnectionChecker networkConnectionChecker);
    public abstract void removeUserInterests(List<WikiDataEntity> userInterests,
                                             DBUserInterestListener userInterestListener);
    //note that this is just to update.
    //when adding a new interest, we most likely have to fetch pronunciation/classification info
    public abstract void addUserInterests(List<WikiDataEntity> userInterests,
                                          DBUserInterestListener userInterestListener,
                                          DBConnectionResultListener connectionResultListener,
                                          NetworkConnectionChecker networkConnectionChecker);
    public void addUserInterests(WikiDataEntity userInterest, DBUserInterestListener userInterestListener,
                                 DBConnectionResultListener connectionResultListener,
                                 NetworkConnectionChecker networkConnectionChecker){
        List<WikiDataEntity> userInterests = new ArrayList<>(1);
        userInterests.add(userInterest);
        addUserInterests(userInterests, userInterestListener, connectionResultListener,
                networkConnectionChecker);
    }
    //fetching pronunciation info will be handled with this.
    //add user interest -> onResultListener -> add pronunciation concurrently
    public abstract void setPronunciation(String userInterestID, String pronunciation);
    public abstract void addSimilarInterest(String fromID, WikiDataEntity toEntity);
    public abstract void getSimilarInterest(String id,
                                            DBSimilarUserInterestResultListener similarUserInterestResultListener);

    //INSTANCE ATTEMPT RECORD
    public abstract void addInstanceAttemptRecord(InstanceAttemptRecord record,
                                                  DBInstanceRecordResultListener instanceRecordResultListener);

    //APP USAGE
    public abstract void addAppUsageLog(AppUsageLog log);
    public abstract void getFirstAppUsageDate(DBAppUsageResultListener appUsageResultListener);
    public abstract void getAppUsageForMonths(int startMonth, int startYear, int endMonth, int endYear,
                                              DBAppUsageResultListener appUsageResultListener,
                                              DBConnectionResultListener connectionResultListener,
                                              NetworkConnectionChecker networkConnectionChecker);
    public abstract void incrementDailyLesson(String date, DBDailyLessonResultListener dailyLessonResultListener);

    //for admin use only
    public abstract void addSport(String sportWikiDataID, String verb, String object);
    public abstract void getSports(Collection<String> sportWikiDataIDs, DBSportResultListener sportResultListener);
}

