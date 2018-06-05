package pelicann.linnca.com.corefunctionality.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

//abstract class for all requests we need to make to the database.
//easier to read code and makes migrating databases easier.

//instead of returning values that we need, we are using listeners
// to trigger events that occur once we fetch/update the data.
//this is because of Firebase's real-time database nature.
//(we can't return values from a Firebase request)

public abstract class Database implements Serializable{
    protected List<NetworkConnectionChecker> networkConnections = new ArrayList<>();

    public abstract String getUserID();
    public void cleanup(){
        cleanupDB();
        cleanupNetworkConnections();
    }
    public abstract void cleanupDB();

    protected void cleanupNetworkConnections(){
        for (NetworkConnectionChecker connection : networkConnections){
            connection.stop();
        }
        networkConnections.clear();
    }

    //using 'search' to distinguish from getting questions for answering
    public abstract void searchEntityPropertyData(NetworkConnectionChecker networkConnectionChecker, String lessonKey, List<WikiDataEntity> userInterests,
                                                  int toPopulate,
                                                  OnDBResultListener onDBResultListener);
    public abstract void addEntityPropertyData(String lessonKey, List<EntityPropertyData> data, OnDBResultListener onDBResultListener);
    public abstract void getRandomEntityPropertyData(NetworkConnectionChecker networkConnectionChecker, String lessonKey, List<EntityPropertyData> toAvoid, int toPopulate, OnDBResultListener onDBResultListener);

    public abstract void addLessonInstance(NetworkConnectionChecker networkConnectionChecker, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs,
                                           OnDBResultListener onDBResultListener);
    public abstract void getLessonInstances(NetworkConnectionChecker networkConnectionChecker, String lessonKey, boolean persistentConnection, OnDBResultListener onDBResultListener);
    public abstract void getLessonInstanceDetails(String lessonKey, String instanceID, OnDBResultListener onDBResultListener);
    public abstract void removeLessonInstance(String lessonKey, LessonInstanceData instance, OnDBResultListener onDBResultListener);
    public abstract void getMostRecentLessonInstance(NetworkConnectionChecker networkConnectionChecker, String lessonKey,
                                                     OnDBResultListener onDBResultListener);

    public abstract void getUserInterests(NetworkConnectionChecker networkConnectionChecker, boolean persistentConnection, OnDBResultListener onDBResultListener);
    public abstract void removeUserInterests(List<WikiDataEntity> userInterests, OnDBResultListener onDBResultListener);
    //note that this is just to update.
    //when adding a new interest, we most likely have to fetch pronunciation/classification info
    public abstract void addUserInterests(NetworkConnectionChecker networkConnectionChecker, List<WikiDataEntity> userInterest, OnDBResultListener onDBResultListener);
    //fetching pronunciation/classification info will be handled with these.
    //add user interest -> onResultListener -> add pronunciation/classification concurrently
    public abstract void setPronunciation(String userInterestID, String pronunciation);
    public abstract void setClassification(String userInterestID, int classification);

    public abstract void addSimilarInterest(String fromID, WikiDataEntity toEntity);
    public abstract void getSimilarInterest(String id, OnDBResultListener onDBResultListener);

    public abstract void changeUserInterestRanking(WikiDataEntity data, int count);
    public abstract void getPopularUserInterests(NetworkConnectionChecker networkConnectionChecker, int count, OnDBResultListener onDBResultListener);

    public abstract void addInstanceRecord(InstanceRecord record, OnDBResultListener onDBResultListener);

    public abstract void getClearedLessons(NetworkConnectionChecker networkConnectionChecker, int lessonLevel, boolean persistentConnection, OnDBResultListener onDBResultListener);
    public abstract void addClearedLesson(int lessonLevel, String lessonKey, OnDBResultListener onDBResultListener);

    public abstract void addReviewQuestion(List<String> questionKeys, OnDBResultListener onDBResultListener);
    public abstract void removeReviewQuestions(OnDBResultListener onDBResultListener);
    public abstract void getReviewQuestions(OnDBResultListener onDBResultListener);

    public abstract void getReportCard(int level, OnDBResultListener onDBResultListener);
    public abstract void addReportCard(int level, String lessonKey, int correctCt, int totalCt, OnDBResultListener onDBResultListener);

    public abstract void addAppUsageLog(AppUsageLog log);
    public abstract void getFirstAppUsageDate(OnDBResultListener onDBResultListener);
    public abstract void getAppUsageForMonths(NetworkConnectionChecker networkConnectionChecker, String startMonthKey, String endMonthKey, OnDBResultListener onDBResultListener);

    public abstract void addDailyLesson(String date, OnDBResultListener onDBResultListener);

    //for admin use only
    public abstract void addSport(String sportWikiDataID, String verb, String object);
    public abstract void getSports(Collection<String> sportWikiDataIDs, OnDBResultListener onDBResultListener);
}

