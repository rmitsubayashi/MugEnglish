package com.linnca.pelicann.db;


import android.content.Context;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//abstract class for all requests we need to make to the database.
//easier to read code and makes migrating databases easier.

//instead of returning values that we need, we are using listeners
// to trigger events that occur once we fetch/update the data.
//this is because of Firebase's real-time database nature.
//(we can't return values from a Firebase request)

public abstract class Database implements Serializable{
    List<NetworkConnectionChecker> networkConnections = new ArrayList<>();

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

    //only used in initial run by admin, not called by client
    public abstract void addGenericQuestions(List<QuestionData> questions, List<VocabularyWord> vocabulary);
    //using 'search' to distinguish from getting questions for answering
    public abstract void searchQuestions(Context context, String lessonKey, List<WikiDataEntity> userInterests,
                                                  int toPopulate, List<String> questionSetIDsToAvoid,
                                                  OnDBResultListener onDBResultListener);
    public abstract void addQuestions(String lessonKey, List<QuestionSetData> questions, OnDBResultListener onDBResultListener);
    public abstract void getQuestionSets(Context context, String lessonKey, List<String> questionSetIDs, OnDBResultListener onDBResultListener);
    public abstract void changeQuestionSetCount(String lessonKey, String questionSetID, int amount, OnDBResultListener onDBResultListener);
    public abstract void getPopularQuestionSets(Context context, String lessonKey, List<String> questionSetsToAvoid,
                                                int questionSetsToPopulate, OnDBResultListener onDBResultListener);

    public abstract void getQuestion(Context context, String questionID, OnDBResultListener onDBResultListener);

    public abstract void addLessonInstance(Context context, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs,
                                           OnDBResultListener onDBResultListener);
    public abstract void getLessonInstances(Context context, String lessonKey, boolean persistentConnection, OnDBResultListener onDBResultListener);
    public abstract void getLessonInstanceDetails(String lessonKey, String instanceID, OnDBResultListener onDBResultListener);
    public abstract void removeLessonInstance(String lessonKey, LessonInstanceData instance, OnDBResultListener onDBResultListener);

    public abstract void getVocabularyDetails(Context context, String vocabularyItemID, OnDBResultListener onDBResultListener);
    public abstract void getVocabularyList(Context context, OnDBResultListener onDBResultListener);
    public abstract void addVocabularyWord(VocabularyWord word, OnDBResultListener onDBResultListener);
    public abstract void removeVocabularyListItems(List<String> vocabularyListItemKeys, OnDBResultListener onDBResultListener);
    public abstract void getLessonVocabulary(Context context, String lessonInstanceKey, OnDBResultListener onDBResultListener);

    public abstract void getUserInterests(Context context, boolean persistentConnection, OnDBResultListener onDBResultListener);
    public abstract void removeUserInterests(List<WikiDataEntity> userInterests, OnDBResultListener onDBResultListener);
    //note that this is just to update.
    //when adding a new interest, we most likely have to fetch pronunciation/classification info
    public abstract void addUserInterests(Context context, List<WikiDataEntity> userInterest, OnDBResultListener onDBResultListener);
    //fetching pronunciation/classification info will be handled with these.
    //add user interest -> onResultListener -> add pronunciation/classification concurrently
    public abstract void setPronunciation(String userInterestID, String pronunciation);
    public abstract void setClassification(String userInterestID, int classification);

    public abstract void changeUserInterestRanking(WikiDataEntity data, int count);
    public abstract void getPopularUserInterests(Context context, int count, OnDBResultListener onDBResultListener);

    public abstract void addInstanceRecord(InstanceRecord record, OnDBResultListener onDBResultListener);

    public abstract void getClearedLessons(Context context, int lessonLevel, boolean persistentConnection, OnDBResultListener onDBResultListener);
    public abstract void addClearedLesson(int lessonLevel, String lessonKey, OnDBResultListener onDBResultListener);
    //for debugging
    public abstract void clearAllLessons(List<List<LessonListRow>> lessonLevels);

    public abstract void addReviewQuestion(List<String> questionKeys, OnDBResultListener onDBResultListener);
    public abstract void removeReviewQuestions(OnDBResultListener onDBResultListener);
    public abstract void getReviewQuestions(OnDBResultListener onDBResultListener);

    public abstract void getReportCard(int level, OnDBResultListener onDBResultListener);
    public abstract void addReportCard(int level, String lessonKey, int correctCt, int totalCt, OnDBResultListener onDBResultListener);

    public abstract void addAppUsageLog(AppUsageLog log);
    public abstract void getFirstAppUsageDate(OnDBResultListener onDBResultListener);
    public abstract void getAppUsageForMonths(Context context, String startMonthKey, String endMonthKey, OnDBResultListener onDBResultListener);

    public abstract void addDailyLesson(String date, OnDBResultListener onDBResultListener);

    //for admin use only
    public abstract void addSport(String sportWikiDataID, String verb, String object);
    public abstract void getSports(Collection<String> sportWikiDataIDs, OnDBResultListener onDBResultListener);
}

