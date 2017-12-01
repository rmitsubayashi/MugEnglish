package com.linnca.pelicann.db;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

//we are creating a mock database for testing purposes.
//some of the data is narrowed down.
//for example, since we will only ever be testing one lesson at a tmie,
// we don't separate lesson instances by lesson ID (where in the actual database, it does)
public class MockFirebaseDB extends Database {
    //all the data will be in public variables
    //so we can easily access them when testing
    public List<WikiDataEntryData> userInterests = new ArrayList<>();
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
    public List<WikiDataEntryData> recommendations = new ArrayList<>();

    @Override
    public String getUserID() {
        return "user1";
    }

    @Override
    public void cleanup() {
    }

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
    public void searchQuestions(String lessonKey, List<WikiDataEntryData> userInterests, int toPopulate,
                                List<String> questionSetIDsToAvoid, OnResultListener onResultListener) {
        List<QuestionSet> questionSetsToReturn = new ArrayList<>(toPopulate);
        List<WikiDataEntryData> userInterestsChecked = new ArrayList<>(userInterests.size());
        if (toPopulate == 0){
            onResultListener.onQuestionsQueried(questionSetsToReturn, userInterestsChecked);
        }
        for (WikiDataEntryData userInterest : userInterests){
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

        onResultListener.onQuestionsQueried(questionSetsToReturn, userInterestsChecked);

    }

    @Override
    public void addQuestions(String lessonKey, List<QuestionDataWrapper> questions, OnResultListener onResultListener) {
        int mockQuestionID = 1;
        int mockQuestionSetID = 1;
        int mockVocabularyID = 1;
        int mockDateTime = 1;
        for (QuestionDataWrapper wrapper : questions){
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
            QuestionSet questionSet = new QuestionSet(setID, wrapper.getWikiDataID(),
                    wrapper.getInterestLabel(),
                    questionIDs, vocabularyIDs, 0);
            questionSets.put(setID, questionSet);

            onResultListener.onQuestionSetAdded(questionSet);

        }

        onResultListener.onQuestionsAdded();
    }

    @Override
    public void getQuestionSets(String lessonKey, List<String> questionSetIDs, OnResultListener onResultListener) {
        //don't care about lesson key
        List<QuestionSet> questionSets = new ArrayList<>(questionSetIDs.size());
        for (String id : questionSetIDs){
            QuestionSet match = this.questionSets.get(id);
            if (match != null){
                questionSets.add(match);
            }
        }
        onResultListener.onQuestionSetsQueried(questionSets);
    }

    @Override
    public void changeQuestionSetCount(String lessonKey, String questionSetID, int amount, OnResultListener onResultListener){

    }

    @Override
    public void getPopularQuestionSets(String lessonKey, final List<String> questionSetsToAvoid,
                                       final int questionSetsToPopulate,
                                       final OnResultListener onResultListener){
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
        onResultListener.onPopularQuestionSetsQueried(result);
    }

    @Override
    public void getQuestion(String questionID, OnResultListener onResultListener) {
        QuestionData questionData = questions.get(questionID);
        onResultListener.onQuestionQueried(questionData);
    }

    private int incrementLessonInstance = 1;
    @Override
    public void addLessonInstance(String lessonKey, LessonInstanceData lessonInstanceData, List<String> lessonInstanceVocabularyIDs, OnResultListener onResultListener) {
        String id = "testID" + incrementLessonInstance++;
        lessonInstanceData.setId(id);
        lessonInstances.put(lessonKey, lessonInstanceData);
        onResultListener.onLessonInstanceAdded();
    }

    @Override
    public void getLessonInstances(String lessonKey, boolean persistentConnection, OnResultListener onResultListener) {
        // we are assuming a single lesson so we don't need to filter by lesson key
        List<LessonInstanceData> instancesList = new ArrayList<>();

        for (Map.Entry<String, LessonInstanceData> entry : lessonInstances.entrySet()){
            instancesList.add(entry.getValue());
        }
        onResultListener.onLessonInstancesQueried(instancesList);
    }

    @Override
    public void getLessonInstanceDetails(String lessonKey, String instanceID, OnResultListener onResultListener) {

    }

    @Override
    public void removeLessonInstance(String lessonKey, LessonInstanceData instanceData, OnResultListener onResultListener) {

    }

    @Override
    public void getVocabularyDetails(String vocabularyItemID, OnResultListener onResultListener) {

    }

    @Override
    public void getVocabularyList(OnResultListener onResultListener) {

    }

    private int incrementVocabularyWord = 1;
    @Override
    public void addVocabularyWord(VocabularyWord word, OnResultListener onResultListener) {
        String id = "id" + incrementVocabularyWord++;
        word.setId(id);
        vocabularyListWords.put(id, new VocabularyListWord(word, id));
        vocabularyWords.put(word.getId(), word);
        onResultListener.onVocabularyWordAdded();
    }

    @Override
    public void removeVocabularyListItems(List<String> vocabularyListItemKeys, OnResultListener onResultListener) {

    }

    @Override
    public void getLessonVocabulary(String lessonInstanceKey, OnResultListener onResultListener) {

    }

    @Override
    public void getUserInterests(OnResultListener onResultListener) {
        onResultListener.onUserInterestsQueried(userInterests);
    }

    @Override
    public void removeUserInterests(List<WikiDataEntryData> userInterests, OnResultListener onResultListener) {
        this.userInterests.removeAll(userInterests);
        onResultListener.onUserInterestsRemoved();
    }

    @Override
    public void addUserInterests(List<WikiDataEntryData> userInterest, OnResultListener onResultListener) {
        this.userInterests.addAll(userInterest);
        onResultListener.onUserInterestsAdded();
    }

    @Override
    public void changeUserInterestRanking(WikiDataEntryData data, int count){}

    @Override
    public void getPopularUserInterests(int count, OnResultListener onResultListener){
        if (count > recommendations.size()){
            count = recommendations.size();
        }
        //we don't care about order
        List<WikiDataEntryData> popularUserInterestList = new ArrayList<>(
                recommendations.subList(0, count)
        );
        onResultListener.onUserInterestRankingsQueried(popularUserInterestList);
    }

    @Override
    public void setPronunciation(String userInterestID, String pronunciation){

    }

    @Override
    public void setClassification(String userInterestID, int classification){

    }

    @Override
    public void addInstanceRecord(InstanceRecord record, OnResultListener onResultListener) {

    }

    @Override
    public void getClearedLessons(int lessonLevel, boolean persistentConnection, OnResultListener onResultListener) {

    }

    @Override
    public void addClearedLesson(int lessonLevel, String lessonKey, OnResultListener onResultListener) {

    }

    @Override
    public void clearAllLessons(List<List<LessonListRow>> lessonLevels) {

    }

    @Override
    public void addReviewQuestion(List<String> questionKeys, OnResultListener onResultListener){

    }

    @Override
    public void removeReviewQuestions(OnResultListener onResultListener){

    }

    @Override
    public void getReviewQuestions(OnResultListener onResultListener){

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
