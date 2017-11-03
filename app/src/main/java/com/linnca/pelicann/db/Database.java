package com.linnca.pelicann.db;

import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.util.List;

//abstract class for all requests we need to make to the database.
//easier to read code and makes migrating databases easier.

//instead of returning values that we need, we are using listeners
// to trigger events that occur once we fetch/update the data.
//this is because of Firebase's real-time database nature.
//(we can't return values from a Firebase request)

public abstract class Database {

    public abstract String getUserID();
    public abstract void cleanup();

    public abstract void getVocabularyDetails(String vocabularyItemID, OnResultListener onResultListener);
    public abstract void getVocabularyList(OnResultListener onResultListener);
    public abstract void addVocabularyWord(VocabularyWord word, OnResultListener onResultListener);
    public abstract void removeVocabularyListItems(List<String> vocabularyListItemKeys, OnResultListener onResultListener);
    public abstract void getLessonVocabulary(String lessonInstanceKey, OnResultListener onResultListener);

    public abstract void getUserInterests(OnResultListener onResultListener);
    public abstract void removeUserInterests(List<WikiDataEntryData> userInterests, OnResultListener onResultListener);
    //note that this is just to update.
    //when adding a new interest, we most likely have to fetch pronunciation/ category info
    public abstract void addUserInterests(List<WikiDataEntryData> userInterest, OnResultListener onResultListener);

    public abstract void addInstanceRecord(InstanceRecord record, OnResultListener onResultListener);

    public abstract void getClearedLessons(int lessonLevel, OnResultListener onResultListener);
    public abstract void addClearedLesson(int lessonLevel, String lessonKey, OnResultListener onResultListener);

    public abstract void getReportCard(int level, OnResultListener onResultListener);
    public abstract void addReportCard(int level, String lessonKey, int correctCt, int totalCt, OnResultListener onResultListener);

    public abstract void getFirstAppUsageDate(OnResultListener onResultListener);
    public abstract void getAppUsageForMonths(String startMonthKey, String endMonthKey, OnResultListener onResultListener);
}
