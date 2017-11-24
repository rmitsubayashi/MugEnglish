package com.linnca.pelicann.db;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

//tests the implementation of the mock database
public class TestMockFirebaseDB {
    private MockFirebaseDB db;

    @Before
    public void init(){
        db = new MockFirebaseDB();
    }

    //for the methods called,
    //we can use Mockito, but doing it without any frameworks works just as well
    // (a little less cleaner code and readability)
    // and with shorter run time
    // (one Mockito test can take ~500 ms,
    // plain JUnit test would take ~5 ms)
    @Test
    public void searchQuestions_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                called[0] = true;
            }
        };
        //OnResultListener onResultListener = mock(OnResultListener.class);
        db.searchQuestions("", new ArrayList<WikiDataEntryData>(),
                0, new ArrayList<String>(), onResultListener);
        assertTrue(called[0]);
        //verify(onResultListener, times(2)).onQuestionsQueried(new ArrayList<String>(), new ArrayList<WikiDataEntryData>());
    }

    @Test
    public void addQuestion_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false, false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                called[0] = true;
            }
            @Override
            public void onQuestionSetAdded(String questionSetKey, List<List<String>> questionIDs, String interestLabel, List<String> vocabularyWordKeys) {
                called[1] = true;
            }
        };
        //need at least one question for onQuestionSetAdded to be called.
        //an empty question data wrapper will not call it
        List<QuestionDataWrapper> questions = new ArrayList<>();
        questions.add(new QuestionDataWrapper(new ArrayList<List<QuestionData>>(), "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));
        db.addQuestions("", questions,
                 onResultListener);
        assertTrue(called[0] && called[1]);
    }

    @Test
    public void getRelatedInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRelatedUserInterestsQueried(List<WikiDataEntryData> userInterestsSearched) {
                called[0] = true;
            }
        };
        db.getRelatedUserInterests(new ArrayList<WikiDataEntryData>(),
                WikiDataEntryData.CLASSIFICATION_OTHER,
                0,
                onResultListener);
        assertTrue(called[0]);
    }

    @Test
    public void getRandomQuestions_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRandomQuestionsQueried(List<String> questionSetIDs) {
                called[0] = true;
            }
        };
        db.getRandomQuestions("",0,
                new ArrayList<String>(), 0,
                onResultListener);
        assertTrue(called[0]);
    }

    @Test
    public void getQuestionSets_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionSetsQueried(List<QuestionSet> questionSets) {
                called[0] = true;
            }
        };
        db.getQuestionSets(new ArrayList<String>(), onResultListener);
        assertTrue(called[0]);
    }

    @Test
    public void getQuestion_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionQueried(QuestionData questionData) {
                called[0] = true;
            }
        };
        db.getQuestion("", onResultListener);
    }

    @Test
    public void addLessonInstance_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                called[0] = true;
            }
        };
        db.addLessonInstance("", new LessonInstanceData(), new ArrayList<String>(),
                onResultListener);
    }

    @Test
    public void getLessonInstances_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                called[0] = true;
            }
        };
        db.getLessonInstances("", onResultListener);
    }

    @Test
    public void addVocabularyWord_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onVocabularyWordAdded() {
                called[0] = true;
            }
        };
        db.addVocabularyWord(new VocabularyWord(), onResultListener);
    }

    @Test
    public void getUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntryData> userInterests) {
                called[0] = true;
            }
        };
        db.getUserInterests(onResultListener);
    }

    @Test
    public void removeUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                called[0] = true;
            }
        };
        db.removeUserInterests(new ArrayList<WikiDataEntryData>(), onResultListener);
    }

    @Test
    public void addUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsAdded() {
                called[0] = true;
            }
        };
        db.addUserInterests(new ArrayList<WikiDataEntryData>(), onResultListener);
    }

    @Test
    public void getRecommendations_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRecommendationsQueried(List<WikiDataEntryData> recommendations) {
                called[0] = true;
            }
        };
        db.getRecommendations(new ArrayList<WikiDataEntryData>(), "userInterest1",
                0, onResultListener);
    }

    @Test
    public void userInterests_addUserInterests_userInterestListShouldContainUserInterests(){
        final List<WikiDataEntryData> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        newInterests.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsAdded() {
                List<WikiDataEntryData> updatedList = db.userInterests;
                boolean matched = true;
                for (WikiDataEntryData data : updatedList){
                    if (!newInterests.contains(data)){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };
        db.addUserInterests(newInterests, onResultListener);
    }

    @Test
    public void userInterests_addUserInterests_userInterestListShouldOnlyContainUserInterests(){
        final List<WikiDataEntryData> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        newInterests.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsAdded() {
                List<WikiDataEntryData> updatedList = db.userInterests;
                assertEquals(newInterests.size(), updatedList.size());
            }
        };
        db.addUserInterests(newInterests, onResultListener);
    }

    @Test
    public void userInterests_removeUserInterests_userInterestsShouldBeRemoved(){
        final List<WikiDataEntryData> oldInterests = new ArrayList<>(2);
        oldInterests.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        oldInterests.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                List<WikiDataEntryData> updatedList = db.userInterests;
                boolean matched = false;
                for (WikiDataEntryData data : oldInterests){
                    if (updatedList.contains(data)){
                        matched = true;
                        break;
                    }
                }
                assertFalse(matched);
            }
        };
        db.userInterests = new ArrayList<>(oldInterests);
        db.removeUserInterests(oldInterests, onResultListener);
    }

    @Test
    public void userInterests_removeUserInterests_onlyUserInterestsToRemoveShouldBeRemoved(){
        List<WikiDataEntryData> oldInterests = new ArrayList<>(2);
        oldInterests.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        oldInterests.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        oldInterests.add(new WikiDataEntryData("label3", "desc3", "wikidataID3", "label3", WikiDataEntryData.CLASSIFICATION_OTHER));
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                assertEquals(1, db.userInterests.size());
            }
        };
        List<WikiDataEntryData> toRemoveInterests = new ArrayList<>(oldInterests);
        toRemoveInterests.remove(0);
        db.userInterests = new ArrayList<>(oldInterests);
        db.removeUserInterests(toRemoveInterests, onResultListener);
    }

    @Test
    public void userInterests_getUserInterests_shouldGetUserInterests(){
        final List<WikiDataEntryData> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        newInterests.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntryData> userInterests) {
                boolean matched = true;
                for (WikiDataEntryData interest : userInterests){
                    if (!newInterests.contains(interest)){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };
        db.addUserInterests(newInterests, onResultListener);
    }

    @Test
    public void question_addGenericQuestion_shouldAddQuestionsToDatabase(){
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        db.addGenericQuestions(newQuestions, new ArrayList<VocabularyWord>());
        Map<String, QuestionData> questions = db.questions;
        assertTrue(questions.containsKey("id1"));
        assertEquals("id1",questions.get("id1").getId());
    }

    @Test
    public void question_addGenericQuestion_shouldAddVocabulary(){
        List<VocabularyWord> newWords = new ArrayList<>(1);
        newWords.add(new VocabularyWord("id1","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1"));
        db.addGenericQuestions(new ArrayList<QuestionData>(), newWords);
        Map<String, VocabularyWord> words = db.questionVocabularyWords;
        assertTrue(words.containsKey("id1"));
        assertEquals("id1",words.get("id1").getId());
    }

    @Test
    public void question_addGenericQuestion_shouldNotAddVocabularyToList(){
        List<VocabularyWord> newWords = new ArrayList<>(1);
        newWords.add(new VocabularyWord("id1","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1"));
        db.addGenericQuestions(new ArrayList<QuestionData>(), newWords);
        Map<String, VocabularyListWord> wordList = db.vocabularyListWords;
        assertEquals(0, wordList.size());
    }

    @Test
    public void question_addQuestion_shouldAddQuestion(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, QuestionData> questions = db.questions;
                boolean matched = true;
                for (Map.Entry<String, QuestionData> entry : questions.entrySet()){
                    if (!entry.getValue().getQuestion().equals("question1")){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_addOneQuestion_shouldOnlyAddOneQuestion(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, QuestionData> questions = db.questions;
                assertEquals(1, questions.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_addQuestion_shouldAddQuestionSetIDPerUserInterestPerLesson(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, List<String>> questionSetIDs = db.questionSetsPerUserInterestPerQuestion;
                assertTrue(questionSetIDs.containsKey("wikidataID1"));
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_addOneQuestion_shouldAddOnlyOneQuestionSet(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, QuestionSet> questionSet = db.questionSets;
                assertEquals(1, questionSet.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_addOneQuestion_shouldAddOnlyOneRandomQuestionSet(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, String> randomQuestionSet = db.randomQuestionSets;
                assertEquals(1, randomQuestionSet.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_addOneQuestionWithOneVocabularyWord_shouldAddOnlyOneVocabularyWord(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(new VocabularyWord("id1","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1"));
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", words));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, VocabularyWord> vocabularyWords = db.questionVocabularyWords;
                assertEquals(1, vocabularyWords.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_addQuestionWithVocabularyWord_shouldNotAddVocabularyList(){
        List<QuestionDataWrapper> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(new VocabularyWord("id1","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1"));
        questionList.add(new QuestionDataWrapper(newQuestionSet, "wikidataID1",
                "interestLabel1", words));

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, VocabularyListWord> vocabularyList = db.vocabularyListWords;
                assertEquals(0, vocabularyList.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onResultListener);
    }

    @Test
    public void question_searchQuestionWithMatchingUserInterest_shouldReturnQuestionSetID(){
        String wikidataID = "wikidataID1";
        List<String> questionSets = new ArrayList<>(1);
        final String questionSetID = "questionSetID1";
        questionSets.add(questionSetID);
        db.questionSetsPerUserInterestPerQuestion.put(wikidataID, questionSets);

        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                assertTrue(questionSetIDs.contains(questionSetID));
            }
        };
        List<WikiDataEntryData> userInterests = new ArrayList<>(1);
        userInterests.add(new WikiDataEntryData("label1","desc1", wikidataID,
                "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.searchQuestions("", userInterests, 1,
                new ArrayList<String>(), onResultListener);
    }

    @Test
    public void question_searchQuestionWithMatchingUserInterest_shouldMarkUserInterestAsAlreadyChecked(){
        String wikidataID = "wikidataID1";
        List<String> questionSets = new ArrayList<>(1);
        String questionSetID = "questionSetID1";
        questionSets.add(questionSetID);
        db.questionSetsPerUserInterestPerQuestion.put(wikidataID, questionSets);

        List<WikiDataEntryData> userInterests = new ArrayList<>(1);
        final WikiDataEntryData userInterest1 = new WikiDataEntryData("label1","desc1", wikidataID,
                "label1", WikiDataEntryData.CLASSIFICATION_OTHER);
        userInterests.add(userInterest1);


        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                assertTrue(userInterestsSearched.contains(userInterest1));
            }
        };
        db.searchQuestions("", userInterests, 1,
                new ArrayList<String>(), onResultListener);
    }

    @Test
    public void question_searchQuestionWithMatchingUserInterestsMoreThanToPopulateCount_shouldOnlyReturnToPopulateNumberOfQuestionSetIDs(){
        String wikidataID1 = "wikidataID1";
        List<String> questionSets = new ArrayList<>(1);
        String questionSetID = "questionSetID1";
        questionSets.add(questionSetID);
        db.questionSetsPerUserInterestPerQuestion.put(wikidataID1, questionSets);

        String wikidataID2 = "wikidataID2";
        List<String> questionSets2 = new ArrayList<>(1);
        String questionSetID2 = "questionSetID2";
        questionSets2.add(questionSetID2);
        db.questionSetsPerUserInterestPerQuestion.put(wikidataID2, questionSets2);

        List<WikiDataEntryData> userInterests = new ArrayList<>(1);
        WikiDataEntryData userInterest1 = new WikiDataEntryData("label1","desc1", wikidataID1,
                "label1", WikiDataEntryData.CLASSIFICATION_OTHER);
        userInterests.add(userInterest1);
        WikiDataEntryData userInterest2 = new WikiDataEntryData("label2","desc2", wikidataID2,
                "label2", WikiDataEntryData.CLASSIFICATION_OTHER);
        userInterests.add(userInterest2);


        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                assertEquals(1, questionSetIDs.size());
            }
        };
        db.searchQuestions("", userInterests, 1,
                new ArrayList<String>(), onResultListener);
        //to make sure toPopulateCt is what's affecting it
        OnResultListener onResultListener2 = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                assertEquals(2, questionSetIDs.size());
            }
        };
        db.searchQuestions("", userInterests, 2,
                new ArrayList<String>(), onResultListener2);
    }

    @Test
    public void question_searchQuestionWithNoMatchingUserInterests_shouldReturnEmptyList(){
        List<WikiDataEntryData> userInterests = new ArrayList<>(1);
        WikiDataEntryData userInterest1 = new WikiDataEntryData("label1","desc1", "wikidata1",
                "label1", WikiDataEntryData.CLASSIFICATION_OTHER);
        userInterests.add(userInterest1);


        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                assertEquals(0, questionSetIDs.size());
            }
        };
        db.searchQuestions("", userInterests, 1,
                new ArrayList<String>(), onResultListener);
    }

    @Test
    public void question_searchQuestionWithNoUserInterests_shouldReturnEmptyList(){
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched) {
                assertEquals(0, questionSetIDs.size());
            }
        };
        db.searchQuestions("", new ArrayList<WikiDataEntryData>(), 1,
                new ArrayList<String>(), onResultListener);
    }

    @Test
    public void question_getQuestion_shouldGetQuestion(){
        final String questionID = "questionID1";
        QuestionData questionData = new QuestionData(questionID,"lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put(questionID, questionData);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionQueried(QuestionData questionData) {
                assertEquals(questionData.getId(), questionID);
            }
        };
        db.getQuestion(questionID, onResultListener);
    }

    @Test
    public void questions_getQuestionSets_shouldGetQuestionSets(){
        final String questionSetID = "questionSetID1";
        final QuestionSet questionSet = new QuestionSet(questionSetID,"interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>());
        db.questionSets.put(questionSetID, questionSet);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionSetsQueried(List<QuestionSet> questionSets) {
                boolean match = true;
                for (QuestionSet set : questionSets){
                    if (!set.getKey().equals(questionSetID)){
                        match = false;
                    }
                }
                assertTrue(match);
            }
        };
        List<String> toGetSetIDs = new ArrayList<>(1);
        toGetSetIDs.add(questionSetID);
        db.getQuestionSets(toGetSetIDs, onResultListener);
    }

    @Test
    public void questions_getOneQuestionSet_shouldGetOneQuestionSet(){
        final String questionSetID = "questionSetID1";
        QuestionSet questionSet = new QuestionSet(questionSetID,"interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>());
        db.questionSets.put(questionSetID, questionSet);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onQuestionSetsQueried(List<QuestionSet> questionSets) {
                assertEquals(1, questionSets.size());
            }
        };
        List<String> toGetSetIDs = new ArrayList<>(1);
        toGetSetIDs.add(questionSetID);
        db.getQuestionSets(toGetSetIDs, onResultListener);
    }

    @Test
    public void lessonInstance_addLessonInstance_shouldAddInstanceInDatabase(){
        List<String> questionSetIDs = new ArrayList<>(1);
        questionSetIDs.add("set1");
        List<String> questionIDs = new ArrayList<>(1);
        questionIDs.add("question1");
        List<String> interestLabels = new ArrayList<>(1);
        interestLabels.add("interestLabel1");
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("", questionSetIDs, questionIDs,
                interestLabels);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onLessonInstanceAdded() {
                Map<String, LessonInstanceData> lessonInstances = db.lessonInstances;
                assertEquals(1, lessonInstances.size());
            }
        };
        db.addLessonInstance("lesson1", lessonInstanceData,
                new ArrayList<String>(), onResultListener);
    }

    @Test
    public void lessonInstance_getLessonInstances_shouldGetInstances(){
        List<String> questionSetIDs = new ArrayList<>(1);
        questionSetIDs.add("set1");
        List<String> questionIDs = new ArrayList<>(1);
        questionIDs.add("question1");
        List<String> interestLabels = new ArrayList<>(1);
        interestLabels.add("interestLabel1");
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id1", questionSetIDs, questionIDs,
                interestLabels);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                boolean matched = true;
                for (LessonInstanceData data : lessonInstances){
                    List<String> dataQuestionSets = data.getQuestionSetIds();
                    for (String setID : dataQuestionSets){
                        if (!setID.equals("set1")){
                            matched = false;
                        }
                    }
                }
                assertTrue(matched);
            }
        };
        db.lessonInstances.put(lessonInstanceData.getId(), lessonInstanceData);

        db.getLessonInstances("lessonID1",onResultListener);
    }

    @Test
    public void lessonInstance_addLessonInstance_shouldAddIDToInstance(){
        List<String> questionSetIDs = new ArrayList<>(1);
        questionSetIDs.add("set1");
        List<String> questionIDs = new ArrayList<>(1);
        questionIDs.add("question1");
        List<String> interestLabels = new ArrayList<>(1);
        interestLabels.add("interestLabel1");
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("", questionSetIDs, questionIDs,
                interestLabels);
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onLessonInstanceAdded() {
                Map<String, LessonInstanceData> lessonInstances = db.lessonInstances;
                boolean lessonIDSet = true;
                for (Map.Entry<String, LessonInstanceData> entrySet : lessonInstances.entrySet()){
                    LessonInstanceData data = entrySet.getValue();
                    if (data.getId() == null || data.getId().equals("")){
                        lessonIDSet = false;
                        break;
                    }
                }
                assertTrue(lessonIDSet);
            }
        };
        db.addLessonInstance("lesson1", lessonInstanceData,
                new ArrayList<String>(), onResultListener);
    }

    @Test
    public void vocabulary_addVocabularyWord_shouldAddWordToVocabularyWords(){
        final VocabularyWord word = new VocabularyWord("","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1");
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onVocabularyWordAdded() {
                Map<String, VocabularyWord> words = db.vocabularyWords;
                boolean match = true;
                for (Map.Entry<String, VocabularyWord> entry : words.entrySet()){
                    VocabularyWord entryWord = entry.getValue();
                    if (!entryWord.getWord().equals("word1")){
                        match = false;
                        break;
                    }
                }
                assertTrue(match);
            }
        };
        db.addVocabularyWord(word, onResultListener);
    }

    @Test
    public void vocabulary_addVocabularyWord_shouldAddWordToVocabularyList(){
        final VocabularyWord word = new VocabularyWord("","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1");
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onVocabularyWordAdded() {
                Map<String, VocabularyListWord> words = db.vocabularyListWords;
                boolean match = true;
                for (Map.Entry<String, VocabularyListWord> entry : words.entrySet()){
                    VocabularyListWord entryWord = entry.getValue();
                    if (!entryWord.getWord().equals("word1")){
                        match = false;
                        break;
                    }
                }
                assertTrue(match);
            }
        };
        db.addVocabularyWord(word, onResultListener);
    }

    @Test
    public void recommendations_getLessRecommendationsThanInDatabase_shouldReturnRequestCountOfRecommendations(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRecommendationsQueried(List<WikiDataEntryData> results) {
                assertEquals(1, results.size());
            }
        };
        db.getRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                1, onResultListener);
    }

    @Test
    public void recommendations_getMoreRecommendationsThanInDatabase_shouldReturnDatabaseCountOfRecommendations(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRecommendationsQueried(List<WikiDataEntryData> results) {
                assertEquals(2, results.size());
            }
        };
        db.getRecommendations(new ArrayList<WikiDataEntryData>(), "wikiDataID3",
                3, onResultListener);
    }

    @Test
    public void recommendations_getRecommendationsWithUserInterests_shouldReturnUserInterestPlusRecommendationCt(){
        List<WikiDataEntryData> recommendations = new ArrayList<>(2);
        recommendations.add(new WikiDataEntryData("label1", "desc1", "wikidataID1", "label1", WikiDataEntryData.CLASSIFICATION_OTHER));
        recommendations.add(new WikiDataEntryData("label2", "desc2", "wikidataID2", "label2", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.recommendations = recommendations;
        OnResultListener onResultListener = new OnResultListener() {
            @Override
            public void onRecommendationsQueried(List<WikiDataEntryData> results) {
                assertEquals(2, results.size());
            }
        };
        List<WikiDataEntryData> userInterests = new ArrayList<>(1);
        userInterests.add(new WikiDataEntryData("label3", "desc3", "wikidataID3", "label3", WikiDataEntryData.CLASSIFICATION_OTHER));
        db.getRecommendations(userInterests, "wikiDataID4",
                1, onResultListener);
    }

}
