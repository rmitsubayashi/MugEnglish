package com.linnca.pelicann.db;


import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

//abstract class for all requests we need to make to the database.
//easier to read code and makes migrating databases easier.

//instead of returning values that we need, we are using listeners
// to trigger events that occur once we fetch/update the data.
//this is because of Firebase's real-time database nature.
//(we can't return values from a Firebase request)

public abstract class Database implements Serializable{

    public abstract String getUserID();
    public abstract void cleanup();

    //only used in initial run by admin, not called by client
    public abstract void addGenericQuestions(List<QuestionData> questions, List<VocabularyWord> vocabulary);
    //using 'search' to distinguish from getting questions for answering
    public abstract void searchQuestions(String lessonKey, List<WikiDataEntryData> userInterests,
                                                  int toPopulate, List<String> questionSetIDsToAvoid,
                                                  OnResultListener onResultListener);
    public abstract void addQuestions(String lessonKey, List<QuestionDataWrapper> questions, OnResultListener onResultListener);
    public abstract void getRelatedUserInterests(Collection<WikiDataEntryData> userInterests, int categoryOfQuestion, int searchCtPerUserInterest,
                                                 OnResultListener onResultListener);
    public abstract void getRandomQuestions(String lessonKey, int userQuestionHistorySize, List<String> questionSetIDsToAvoid,
                                                        int totalQuestionSetsToPopulate,
                                                        OnResultListener onResultListener);
    public abstract void getQuestionSets(List<String> questionSetIDs, OnResultListener onResultListener);

    public abstract void getQuestion(String questionID, OnResultListener onResultListener);

    public abstract void addLessonInstance(String lessonKey, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs,
                                           OnResultListener onResultListener);
    public abstract void getLessonInstances(String lessonKey, OnResultListener onResultListener);
    public abstract void getLessonInstanceDetails(String lessonKey, String instanceID, OnResultListener onResultListener);
    public abstract void removeLessonInstance(String lessonKey, String instanceID, OnResultListener onResultListener);

    public abstract void getVocabularyDetails(String vocabularyItemID, OnResultListener onResultListener);
    public abstract void getVocabularyList(OnResultListener onResultListener);
    public abstract void addVocabularyWord(VocabularyWord word, OnResultListener onResultListener);
    public abstract void removeVocabularyListItems(List<String> vocabularyListItemKeys, OnResultListener onResultListener);
    public abstract void getLessonVocabulary(String lessonInstanceKey, OnResultListener onResultListener);

    public abstract void getUserInterests(OnResultListener onResultListener);
    public abstract void removeUserInterests(List<WikiDataEntryData> userInterests, OnResultListener onResultListener);
    //note that this is just to update.
    //when adding a new interest, we most likely have to fetch pronunciation/classification info
    public abstract void addUserInterests(List<WikiDataEntryData> userInterest, OnResultListener onResultListener);
    //fetching pronunciation/classification info will be handled with these.
    //add user interest -> onResultListener -> add pronunciation/classification concurrently
    public abstract void setPronunciation(String userInterestID, String pronunciation);
    public abstract void setClassification(String userInterestID, int classification);

    public abstract void getRecommendations(Collection<WikiDataEntryData> userInterests, String targetUserInterestID, int recommendationCt, OnResultListener onResultListener);

    public abstract void addInstanceRecord(InstanceRecord record, OnResultListener onResultListener);

    public abstract void getClearedLessons(int lessonLevel, boolean persistentConnection, OnResultListener onResultListener);
    public abstract void addClearedLesson(int lessonLevel, String lessonKey, OnResultListener onResultListener);
    //for debugging
    public abstract void clearAllLessons(List<List<LessonListRow>> lessonLevels);

    public abstract void addReviewQuestion(List<String> questionKeys, OnResultListener onResultListener);
    public abstract void removeReviewQuestions(OnResultListener onResultListener);
    public abstract void getReviewQuestions(OnResultListener onResultListener);

    public abstract void getReportCard(int level, OnResultListener onResultListener);
    public abstract void addReportCard(int level, String lessonKey, int correctCt, int totalCt, OnResultListener onResultListener);

    public abstract void addAppUsageLog(AppUsageLog log);
    public abstract void getFirstAppUsageDate(OnResultListener onResultListener);
    public abstract void getAppUsageForMonths(String startMonthKey, String endMonthKey, OnResultListener onResultListener);

    //for admin use only
    public abstract void addSport(String sportWikiDataID, String verb, String object);
    public abstract void getSports(Collection<String> sportWikiDataIDs, OnResultListener onResultListener);
}

