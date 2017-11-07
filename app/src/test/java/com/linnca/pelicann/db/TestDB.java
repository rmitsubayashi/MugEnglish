package com.linnca.pelicann.db;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDB extends Database {
    //all the data will be in public variables
    //so we can easily access them when testing
    public final List<WikiDataEntryData> userInterests = new ArrayList<>();
    //ID -> question
    public final Map<String, QuestionData> questions = new HashMap<>();
    public final Map<String, VocabularyWord> vocabularyWords = new HashMap<>();

    @Override
    public String getUserID() {
        return null;
    }

    @Override
    public void cleanup() {
        userInterests.clear();
    }

    @Override
    public void addGenericQuestions(List<QuestionData> questionsToAdd, List<VocabularyWord> vocabulary) {
        for (QuestionData questionData : questionsToAdd){
            questions.put(questionData.getId(), questionData);
        }
        for (VocabularyWord word : vocabulary){
            addVocabularyWord(word, new OnResultListener() {
                @Override
                public void onVocabularyWordAdded() {
                    super.onVocabularyWordAdded();
                }
            });
        }
    }

    @Override
    public void searchQuestions(String lessonKey, List<WikiDataEntryData> userInterests, int toPopulate, List<String> questionSetIDsToAvoid, OnResultListener onResultListener) {

    }

    @Override
    public void addQuestions(String lessonKey, List<QuestionDataWrapper> questions, OnResultListener onResultListener) {

    }

    @Override
    public void getRelatedUserInterests(Collection<WikiDataEntryData> userInterests, int categoryOfQuestion, int searchCtPerUserInterest, OnResultListener onResultListener) {

    }

    @Override
    public void getRandomQuestions(String lessonKey, int userQuestionHistorySize, List<String> questionSetIDsToAvoid, int totalQuestionSetsToPopulate, OnResultListener onResultListener) {

    }

    @Override
    public void getQuestionSets(List<String> questionSetIDs, OnResultListener onResultListener) {

    }

    @Override
    public void getQuestion(String questionID, OnResultListener onResultListener) {

    }

    @Override
    public void addLessonInstance(String lessonKey, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs, OnResultListener onResultListener) {

    }

    @Override
    public void getLessonInstances(String lessonKey, OnResultListener onResultListener) {

    }

    @Override
    public void getLessonInstanceDetails(String lessonKey, String instanceID, OnResultListener onResultListener) {

    }

    @Override
    public void removeLessonInstance(String lessonKey, String instanceID, OnResultListener onResultListener) {

    }

    @Override
    public void getVocabularyDetails(String vocabularyItemID, OnResultListener onResultListener) {

    }

    @Override
    public void getVocabularyList(OnResultListener onResultListener) {

    }

    @Override
    public void addVocabularyWord(VocabularyWord word, OnResultListener onResultListener) {
        vocabularyWords.put(word.getId(), word);
    }

    @Override
    public void removeVocabularyListItems(List<String> vocabularyListItemKeys, OnResultListener onResultListener) {

    }

    @Override
    public void getLessonVocabulary(String lessonInstanceKey, OnResultListener onResultListener) {

    }

    @Override
    public void getUserInterests(OnResultListener onResultListener) {

    }

    @Override
    public void removeUserInterests(List<WikiDataEntryData> userInterests, OnResultListener onResultListener) {

    }

    @Override
    public void addUserInterests(List<WikiDataEntryData> userInterest, OnResultListener onResultListener) {

    }

    @Override
    public void getRecommendations(Collection<WikiDataEntryData> userInterests, String targetUserInterestID, int recommendationCt, OnResultListener onResultListener) {

    }

    @Override
    public void addInstanceRecord(InstanceRecord record, OnResultListener onResultListener) {

    }

    @Override
    public void getClearedLessons(int lessonLevel, OnResultListener onResultListener) {

    }

    @Override
    public void addClearedLesson(int lessonLevel, String lessonKey, OnResultListener onResultListener) {

    }

    @Override
    public void clearAllLessons(List<List<LessonListRow>> lessonLevels) {

    }

    @Override
    public void getReportCard(int level, OnResultListener onResultListener) {

    }

    @Override
    public void addReportCard(int level, String lessonKey, int correctCt, int totalCt, OnResultListener onResultListener) {

    }

    @Override
    public void addAppUsageLog(AppUsageLog log) {

    }

    @Override
    public void getFirstAppUsageDate(OnResultListener onResultListener) {

    }

    @Override
    public void getAppUsageForMonths(String startMonthKey, String endMonthKey, OnResultListener onResultListener) {

    }

    @Override
    public void addSport(String sportWikiDataID, String verb, String object) {

    }

    @Override
    public void getSports(Collection<String> sportWikiDataIDs, OnResultListener onResultListener) {

    }
}
