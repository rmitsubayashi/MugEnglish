package com.linnca.pelicann.db;

import android.content.Context;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//we are creating a mock database for testing purposes.
//some of the data is narrowed down.
//for example, since we will only ever be testing one lesson at a tmie,
// we don't separate lesson instances by lesson ID (where in the actual database, it does)
public class MockFirebaseDB extends Database {
    //all the data will be in public variables
    //so we can easily access them when testing
    public List<WikiDataEntity> userInterests = new ArrayList<>();
    //ID -> question
    public Map<String, QuestionData> questions = new HashMap<>();
    //ID -> question set
    public Map<String, QuestionSet> questionSets = new HashMap<>();
    //ID -> lesson instance
    public Map<String, LessonInstanceData > lessonInstances = new HashMap<>();
    //ID -> word
    public Map<String, VocabularyWord> questionVocabularyWords = new HashMap<>();
    //ID -> word
    public Map<String, VocabularyWord> vocabularyWords = new HashMap<>();
    //ID -> word
    public Map<String, VocabularyListWord> vocabularyListWords = new HashMap<>();
    public List<WikiDataEntity> recommendations = new ArrayList<>();

    @Override
    public String getUserID() {
        return "user1";
    }

    @Override
    public void cleanup() {}

    @Override
    public void cleanupDB(){}

    @Override
    public void addGenericQuestions(List<QuestionData> questionsToAdd, List<VocabularyWord> vocabulary) {
        for (QuestionData questionData : questionsToAdd){
            questions.put(questionData.getId(), questionData);
        }
        for (VocabularyWord word : vocabulary){
            questionVocabularyWords.put(word.getId(), word);
        }
    }

    @Override
    public void searchQuestions(Context context, String lessonKey, List<WikiDataEntity> userInterests, int toPopulate,
                                List<String> questionSetIDsToAvoid, OnDBResultListener onDBResultListener) {
        List<QuestionSet> questionSetsToReturn = new ArrayList<>(toPopulate);
        List<WikiDataEntity> userInterestsChecked = new ArrayList<>(userInterests.size());
        if (toPopulate == 0){
            onDBResultListener.onQuestionsQueried(questionSetsToReturn, userInterestsChecked);
        }
        for (WikiDataEntity userInterest : userInterests){
            for (Map.Entry<String, QuestionSet> setEntry : questionSets.entrySet()) {
                QuestionSet set = setEntry.getValue();
                if (set.getInterestID().equals(userInterest.getWikiDataID()) &&
                        !questionSetIDsToAvoid.contains(set.getKey()) &&
                        toPopulate != 0) {
                    questionSetsToReturn.add(set);
                    toPopulate--;

                }
            }
            userInterestsChecked.add(userInterest);
            if (toPopulate == 0) {
                break;
            }
        }

        onDBResultListener.onQuestionsQueried(questionSetsToReturn, userInterestsChecked);

    }

    @Override
    public void addQuestions(String lessonKey, List<QuestionSetData> questions, OnDBResultListener onDBResultListener) {
        int mockQuestionID = 1;
        int mockQuestionSetID = 1;
        int mockVocabularyID = 1;
        int mockDateTime = 1;
        for (QuestionSetData wrapper : questions){
            List<List<String>> questionIDs = new ArrayList<>();
            List<String> vocabularyIDs = new ArrayList<>();
            List<List<QuestionData>> set = wrapper.getQuestionSet();
            for (List<QuestionData> questionVariations : set){
                List<String> questionVariationIDs = new ArrayList<>();
                for (QuestionData question : questionVariations){
                    String questionID = "questionID" + mockQuestionID++;
                    question.setId(questionID);
                    this.questions.put(questionID, question);
                    questionVariationIDs.add(questionID);
                }
                questionIDs.add(questionVariationIDs);

                List<VocabularyWord> setVocabulary = wrapper.getVocabulary();
                for (VocabularyWord word : setVocabulary){
                    String wordID = "vocabularyID" + mockVocabularyID++;
                    word.setId(wordID);
                    questionVocabularyWords.put(wordID, word);
                    vocabularyIDs.add(wordID);
                }

            }
            String setID = "setID" + mockQuestionSetID++;
            QuestionSet questionSet = new QuestionSet(setID, wrapper.getInterestID(),
                    wrapper.getInterestLabel(),
                    questionIDs, vocabularyIDs, 0);
            questionSets.put(setID, questionSet);

            onDBResultListener.onQuestionSetAdded(questionSet);

        }

        onDBResultListener.onQuestionsAdded();
    }

    @Override
    public void getQuestionSets(Context context, String lessonKey, List<String> questionSetIDs, OnDBResultListener onDBResultListener) {
        //don't care about lesson key
        List<QuestionSet> questionSets = new ArrayList<>(questionSetIDs.size());
        for (String id : questionSetIDs){
            QuestionSet match = this.questionSets.get(id);
            if (match != null){
                questionSets.add(match);
            }
        }
        onDBResultListener.onQuestionSetsQueried(questionSets);
    }

    @Override
    public void changeQuestionSetCount(String lessonKey, String questionSetID, int amount, OnDBResultListener onDBResultListener){

    }

    @Override
    public void getPopularQuestionSets(Context context, String lessonKey, final List<String> questionSetsToAvoid,
                                       final int questionSetsToPopulate,
                                       final OnDBResultListener onDBResultListener){
        //don't care about popularity (just get enough questions0
        List<QuestionSet> result = new ArrayList<>(questionSetsToPopulate);
        for (Map.Entry<String, QuestionSet> entry : questionSets.entrySet()){
            QuestionSet set = entry.getValue();
            if (!questionSetsToAvoid.contains(set.getKey())){
                result.add(set);
            }
            if (result.size() == questionSetsToPopulate){
                break;
            }
        }
        onDBResultListener.onPopularQuestionSetsQueried(result);
    }

    @Override
    public void getQuestion(Context context, String questionID, OnDBResultListener onDBResultListener) {
        QuestionData questionData = questions.get(questionID);
        onDBResultListener.onQuestionQueried(questionData);
    }

    private int incrementLessonInstance = 1;
    @Override
    public void addLessonInstance(Context context, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs, OnDBResultListener onDBResultListener) {
        String id = "testID" + incrementLessonInstance++;
        lessonInstanceData.setId(id);
        lessonInstances.put(lessonInstanceData.getLessonKey(), lessonInstanceData);
        onDBResultListener.onLessonInstanceAdded();
    }

    @Override
    public void getLessonInstances(Context context, String lessonKey, boolean persistentConnection, OnDBResultListener onDBResultListener) {
        // we are assuming a single lesson so we don't need to filter by lesson key
        List<LessonInstanceData> instancesList = new ArrayList<>();

        for (Map.Entry<String, LessonInstanceData> entry : lessonInstances.entrySet()){
            instancesList.add(entry.getValue());
        }
        onDBResultListener.onLessonInstancesQueried(instancesList);
    }

    @Override
    public void getLessonInstanceDetails(String lessonKey, String instanceID, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void removeLessonInstance(String lessonKey, LessonInstanceData instanceData, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getVocabularyDetails(Context context, String vocabularyItemID, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getVocabularyList(Context context, OnDBResultListener onDBResultListener) {

    }

    private int incrementVocabularyWord = 1;
    @Override
    public void addVocabularyWord(VocabularyWord word, OnDBResultListener onDBResultListener) {
        String id = "id" + incrementVocabularyWord++;
        word.setId(id);
        vocabularyListWords.put(id, new VocabularyListWord(word, id));
        vocabularyWords.put(word.getId(), word);
        onDBResultListener.onVocabularyWordAdded();
    }

    @Override
    public void removeVocabularyListItems(List<String> vocabularyListItemKeys, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getLessonVocabulary(Context context, String lessonInstanceKey, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getUserInterests(Context context, boolean persistentConnection, OnDBResultListener onDBResultListener) {
        onDBResultListener.onUserInterestsQueried(userInterests);
    }

    @Override
    public void removeUserInterests(List<WikiDataEntity> userInterests, OnDBResultListener onDBResultListener) {
        this.userInterests.removeAll(userInterests);
        onDBResultListener.onUserInterestsRemoved();
    }

    @Override
    public void addUserInterests(Context context, List<WikiDataEntity> userInterest, OnDBResultListener onDBResultListener) {
        this.userInterests.addAll(userInterest);
        onDBResultListener.onUserInterestsAdded();
    }

    @Override
    public void changeUserInterestRanking(WikiDataEntity data, int count){}

    @Override
    public void getPopularUserInterests(Context context, int count, OnDBResultListener onDBResultListener){
        if (count > recommendations.size()){
            count = recommendations.size();
        }
        //we don't care about order
        List<WikiDataEntity> popularUserInterestList = new ArrayList<>(
                recommendations.subList(0, count)
        );
        onDBResultListener.onUserInterestRankingsQueried(popularUserInterestList);
    }

    @Override
    public void setPronunciation(String userInterestID, String pronunciation){

    }

    @Override
    public void setClassification(String userInterestID, int classification){

    }

    @Override
    public void addInstanceRecord(InstanceRecord record, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getClearedLessons(Context context, int lessonLevel, boolean persistentConnection, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addClearedLesson(int lessonLevel, String lessonKey, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void clearAllLessons(List<List<LessonListRow>> lessonLevels) {

    }

    @Override
    public void addReviewQuestion(List<String> questionKeys, OnDBResultListener onDBResultListener){

    }

    @Override
    public void removeReviewQuestions(OnDBResultListener onDBResultListener){

    }

    @Override
    public void getReviewQuestions(OnDBResultListener onDBResultListener){

    }

    @Override
    public void getReportCard(int level, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addReportCard(int level, String lessonKey, int correctCt, int totalCt, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addAppUsageLog(AppUsageLog log) {

    }

    @Override
    public void getFirstAppUsageDate(OnDBResultListener onDBResultListener) {

    }

    @Override
    public void getAppUsageForMonths(Context context, String startMonthKey, String endMonthKey, OnDBResultListener onDBResultListener) {

    }

    @Override
    public void addDailyLesson(String date, OnDBResultListener onDBResultListener){}

    @Override
    public void addSport(String sportWikiDataID, String verb, String object) {

    }

    @Override
    public void getSports(Collection<String> sportWikiDataIDs, OnDBResultListener onDBResultListener) {

    }
}
