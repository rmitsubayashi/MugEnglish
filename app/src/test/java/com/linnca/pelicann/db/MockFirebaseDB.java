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
    //timeStamp -> question set ID
    public Map<String, String> randomQuestionSets = new HashMap<>();
    //ID -> lesson instance
    public Map<String, LessonInstanceData > lessonInstances = new HashMap<>();
    //ID -> word
    public Map<String, VocabularyWord> questionVocabularyWords = new HashMap<>();
    //ID -> word
    public Map<String, VocabularyWord> vocabularyWords = new HashMap<>();
    //ID -> word
    public Map<String, VocabularyListWord> vocabularyListWords = new HashMap<>();
    //user interest ID -> ID -> set IDs
    public Map<String, List<String>> questionSetsPerUserInterestPerQuestion = new HashMap<>();
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
        List<String> questionSetIDsToReturn = new ArrayList<>(toPopulate);
        List<WikiDataEntryData> userInterestsChecked = new ArrayList<>(userInterests.size());
        if (toPopulate == 0){
            onResultListener.onQuestionsQueried(questionSetIDsToReturn, userInterestsChecked);
        }
        for (WikiDataEntryData userInterest : userInterests){
            if (questionSetsPerUserInterestPerQuestion.containsKey(userInterest.getWikiDataID())){
                List<String> questionSetIDs = questionSetsPerUserInterestPerQuestion.get(userInterest.getWikiDataID());
                for (String id : questionSetIDs){
                    if (!questionSetIDsToAvoid.contains(id) &&
                            toPopulate != 0){
                        questionSetIDsToReturn.add(id);
                        toPopulate--;
                    }
                }
                userInterestsChecked.add(userInterest);
            }
            if (toPopulate == 0){
                break;
            }
        }

        onResultListener.onQuestionsQueried(questionSetIDsToReturn, userInterestsChecked);

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
            List<String> questionSetsPerUserInterest =
                    questionSetsPerUserInterestPerQuestion.get(wrapper.getInterestLabel()) == null ?
                            new ArrayList<String>() :
                            questionSetsPerUserInterestPerQuestion.get(wrapper.getInterestLabel()) ;
            questionSetsPerUserInterest.add(setID);
            questionSetsPerUserInterestPerQuestion.put(wrapper.getWikiDataID(), questionSetsPerUserInterest);
            QuestionSet questionSet = new QuestionSet(setID, wrapper.getInterestLabel(),
                    questionIDs, vocabularyIDs);
            questionSets.put(setID, questionSet);
            String dateTime = "dateTime" + mockDateTime++;
            randomQuestionSets.put(dateTime, setID);

            onResultListener.onQuestionSetAdded(setID, questionIDs, wrapper.getInterestLabel(),vocabularyIDs);

        }

        onResultListener.onQuestionsAdded();
    }

    @Override
    public void getRelatedUserInterests(Collection<WikiDataEntryData> userInterests, int categoryOfQuestion, int searchCtPerUserInterest, OnResultListener onResultListener) {
        onResultListener.onRelatedUserInterestsQueried(new ArrayList<WikiDataEntryData>());
    }

    @Override
    public void getRandomQuestions(String lessonKey, int userQuestionHistorySize, List<String> questionSetIDsToAvoid, int totalQuestionSetsToPopulate, OnResultListener onResultListener) {
        List<String> randomQuestionSetIDs = new ArrayList<>(randomQuestionSets.size());
        for (Map.Entry<String, String> entry : randomQuestionSets.entrySet()){
            if (!questionSetIDsToAvoid.contains(entry.getValue()))
                randomQuestionSetIDs.add(entry.getValue());
        }
        onResultListener.onRandomQuestionsQueried(randomQuestionSetIDs);
    }

    @Override
    public void getQuestionSets(List<String> questionSetIDs, OnResultListener onResultListener) {
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
    public void getLessonInstances(String lessonKey, OnResultListener onResultListener) {
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
    public void removeLessonInstance(String lessonKey, String instanceID, OnResultListener onResultListener) {

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
    public void setPronunciation(String userInterestID, String pronunciation){

    }

    @Override
    public void setClassification(String userInterestID, int classification){

    }

    @Override
    public void getRecommendations(Collection<WikiDataEntryData> userInterests, String targetUserInterestID, int recommendationCt, OnResultListener onResultListener) {
        //don't care about filtering out user interests for the mock database.
        //also don't care about the weight of each recommendation edge for mocking.
        //but make sure we retrieve the right number of recommendations
        List<WikiDataEntryData> result;
        recommendationCt = recommendationCt + userInterests.size();
        if (recommendationCt >= recommendations.size()){
            result = new ArrayList<>(recommendations);
        } else {
            result = new ArrayList<>(recommendations.subList(0, recommendationCt));
        }
        onResultListener.onRecommendationsQueried(result);
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
