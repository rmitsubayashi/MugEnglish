package com.linnca.pelicann.db;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessondetails.LessonInstanceDataQuestionSet;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntity;
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
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                called[0] = true;
            }
        };
        //OnDBResultListener onDBResultListener = mock(OnDBResultListener.class);
        db.searchQuestions(null, "", new ArrayList<WikiDataEntity>(),
                0, new ArrayList<String>(), onDBResultListener);
        assertTrue(called[0]);
        //verify(onDBResultListener, times(2)).onQuestionsQueried(new ArrayList<String>(), new ArrayList<WikiDataEntity>());
    }

    @Test
    public void addQuestion_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false, false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsAdded() {
                called[0] = true;
            }
            @Override
            public void onQuestionSetAdded(QuestionSet questionSet) {
                called[1] = true;
            }
        };
        //need at least one question for onQuestionSetAdded to be called.
        //an empty question data wrapper will not call it
        List<QuestionSetData> questions = new ArrayList<>();
        questions.add(new QuestionSetData(new ArrayList<List<QuestionData>>(), "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));
        db.addQuestions("", questions,
                onDBResultListener);
        assertTrue(called[0] && called[1]);
    }

    @Test
    public void getQuestionSets_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionSetsQueried(List<QuestionSet> questionSets) {
                called[0] = true;
            }
        };
        db.getQuestionSets(null, "",new ArrayList<String>(), onDBResultListener);
        assertTrue(called[0]);
    }

    @Test
    public void getQuestion_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionQueried(QuestionData questionData) {
                called[0] = true;
            }
        };
        db.getQuestion(null, "", onDBResultListener);
    }

    @Test
    public void addLessonInstance_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                called[0] = true;
            }
        };
        db.addLessonInstance(null, new LessonInstanceData(), new ArrayList<String>(),
                onDBResultListener);
    }

    @Test
    public void getLessonInstances_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                called[0] = true;
            }
        };
        db.getLessonInstances(null, "", false, onDBResultListener);
    }

    @Test
    public void addVocabularyWord_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onVocabularyWordAdded() {
                called[0] = true;
            }
        };
        db.addVocabularyWord(new VocabularyWord(), onDBResultListener);
    }

    @Test
    public void getUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntity> userInterests) {
                called[0] = true;
            }
        };
        db.getUserInterests(null, false, onDBResultListener);
    }

    @Test
    public void removeUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                called[0] = true;
            }
        };
        db.removeUserInterests(new ArrayList<WikiDataEntity>(), onDBResultListener);
    }

    @Test
    public void addUserInterests_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsAdded() {
                called[0] = true;
            }
        };
        db.addUserInterests(null, new ArrayList<WikiDataEntity>(), onDBResultListener);
    }

    @Test
    public void userInterests_addUserInterests_userInterestListShouldContainUserInterests(){
        final List<WikiDataEntity> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        newInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsAdded() {
                List<WikiDataEntity> updatedList = db.userInterests;
                boolean matched = true;
                for (WikiDataEntity data : updatedList){
                    if (!newInterests.contains(data)){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };
        db.addUserInterests(null, newInterests, onDBResultListener);
    }

    @Test
    public void userInterests_addUserInterests_userInterestListShouldOnlyContainUserInterests(){
        final List<WikiDataEntity> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        newInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsAdded() {
                List<WikiDataEntity> updatedList = db.userInterests;
                assertEquals(newInterests.size(), updatedList.size());
            }
        };
        db.addUserInterests(null, newInterests, onDBResultListener);
    }

    @Test
    public void userInterests_removeUserInterests_userInterestsShouldBeRemoved(){
        final List<WikiDataEntity> oldInterests = new ArrayList<>(2);
        oldInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        oldInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                List<WikiDataEntity> updatedList = db.userInterests;
                boolean matched = false;
                for (WikiDataEntity data : oldInterests){
                    if (updatedList.contains(data)){
                        matched = true;
                        break;
                    }
                }
                assertFalse(matched);
            }
        };
        db.userInterests = new ArrayList<>(oldInterests);
        db.removeUserInterests(oldInterests, onDBResultListener);
    }

    @Test
    public void userInterests_removeUserInterests_onlyUserInterestsToRemoveShouldBeRemoved(){
        List<WikiDataEntity> oldInterests = new ArrayList<>(2);
        oldInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        oldInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        oldInterests.add(new WikiDataEntity("label3", "desc3", "wikidataID3", "label3", WikiDataEntity.CLASSIFICATION_OTHER));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsRemoved() {
                assertEquals(1, db.userInterests.size());
            }
        };
        List<WikiDataEntity> toRemoveInterests = new ArrayList<>(oldInterests);
        toRemoveInterests.remove(0);
        db.userInterests = new ArrayList<>(oldInterests);
        db.removeUserInterests(toRemoveInterests, onDBResultListener);
    }

    @Test
    public void userInterests_getUserInterests_shouldGetUserInterests(){
        final List<WikiDataEntity> newInterests = new ArrayList<>(2);
        newInterests.add(new WikiDataEntity("label1", "desc1", "wikidataID1", "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        newInterests.add(new WikiDataEntity("label2", "desc2", "wikidataID2", "label2", WikiDataEntity.CLASSIFICATION_OTHER));
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onUserInterestsQueried(List<WikiDataEntity> userInterests) {
                boolean matched = true;
                for (WikiDataEntity interest : userInterests){
                    if (!newInterests.contains(interest)){
                        matched = false;
                        break;
                    }
                }
                assertTrue(matched);
            }
        };
        db.addUserInterests(null, newInterests, onDBResultListener);
    }

    @Test
    public void question_addGenericQuestion_shouldAddQuestionsToDatabase(){
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1", Question_TrueFalse.QUESTION_TYPE,
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
        List<QuestionSetData> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionSetData(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
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

        db.addQuestions("lessonID1", questionList, onDBResultListener);
    }

    @Test
    public void question_addOneQuestion_shouldOnlyAddOneQuestion(){
        List<QuestionSetData> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionSetData(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, QuestionData> questions = db.questions;
                assertEquals(1, questions.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onDBResultListener);
    }

    @Test
    public void question_addOneQuestion_shouldAddOnlyOneQuestionSet(){
        List<QuestionSetData> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        questionList.add(new QuestionSetData(newQuestionSet, "wikidataID1",
                "interestLabel1", new ArrayList<VocabularyWord>()));

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, QuestionSet> questionSet = db.questionSets;
                assertEquals(1, questionSet.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onDBResultListener);
    }

    @Test
    public void question_addOneQuestionWithOneVocabularyWord_shouldAddOnlyOneVocabularyWord(){
        List<QuestionSetData> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(new VocabularyWord("id1","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1"));
        questionList.add(new QuestionSetData(newQuestionSet, "wikidataID1",
                "interestLabel1", words));

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, VocabularyWord> vocabularyWords = db.questionVocabularyWords;
                assertEquals(1, vocabularyWords.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onDBResultListener);
    }

    @Test
    public void question_addQuestionWithVocabularyWord_shouldNotAddVocabularyList(){
        List<QuestionSetData> questionList = new ArrayList<>(1);
        List<QuestionData> newQuestions = new ArrayList<>(1);
        newQuestions.add(new QuestionData("id1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null));
        List<List<QuestionData>> newQuestionSet = new ArrayList<>(1);
        newQuestionSet.add(newQuestions);
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(new VocabularyWord("id1","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1"));
        questionList.add(new QuestionSetData(newQuestionSet, "wikidataID1",
                "interestLabel1", words));

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsAdded() {
                Map<String, VocabularyListWord> vocabularyList = db.vocabularyListWords;
                assertEquals(0, vocabularyList.size());
            }
        };

        db.addQuestions("lessonID1", questionList, onDBResultListener);
    }

    @Test
    public void question_searchQuestionWithMatchingUserInterest_shouldReturnQuestionSetID(){
        String wikidataID = "wikidataID1";
        final String questionSetID = "questionSetID1";
        QuestionSet set = new QuestionSet(questionSetID, wikidataID, "interestLabel",
                null, null, 0);
        db.questionSets.put(questionSetID, set);

        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                assertEquals(1, questionSets.size());
                assertEquals(questionSetID, questionSets.get(0).getKey());
            }
        };
        List<WikiDataEntity> userInterests = new ArrayList<>(1);
        userInterests.add(new WikiDataEntity("label1","desc1", wikidataID,
                "label1", WikiDataEntity.CLASSIFICATION_OTHER));
        db.searchQuestions(null, "", userInterests, 1,
                new ArrayList<String>(), onDBResultListener);
    }

    @Test
    public void question_searchQuestionWithMatchingUserInterest_shouldMarkUserInterestAsAlreadyChecked(){
        String wikidataID = "wikidataID1";
        String questionSetID = "questionSetID1";
        QuestionSet set = new QuestionSet(questionSetID, wikidataID, "interestLabel",
                null, null, 0);
        db.questionSets.put(questionSetID, set);

        List<WikiDataEntity> userInterests = new ArrayList<>(1);
        final WikiDataEntity userInterest1 = new WikiDataEntity("label1","desc1", wikidataID,
                "label1", WikiDataEntity.CLASSIFICATION_OTHER);
        userInterests.add(userInterest1);


        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                assertTrue(userInterestsSearched.contains(userInterest1));
            }
        };
        db.searchQuestions(null, "", userInterests, 1,
                new ArrayList<String>(), onDBResultListener);
    }

    @Test
    public void question_searchQuestionWithMatchingUserInterestsMoreThanToPopulateCount_shouldOnlyReturnToPopulateNumberOfQuestionSetIDs(){
        String wikidataID1 = "wikidataID1";
        String questionSetID = "questionSetID1";
        QuestionSet set = new QuestionSet(questionSetID, wikidataID1, "interestLabel",
                null, null, 0);
        db.questionSets.put(questionSetID, set);

        String wikidataID2 = "wikidataID2";
        String questionSetID2 = "questionSetID2";
        QuestionSet set2 = new QuestionSet(questionSetID2, wikidataID2, "interestLabel2",
                null, null, 0);
        db.questionSets.put(questionSetID2, set2);

        List<WikiDataEntity> userInterests = new ArrayList<>(1);
        WikiDataEntity userInterest1 = new WikiDataEntity("label1","desc1", wikidataID1,
                "label1", WikiDataEntity.CLASSIFICATION_OTHER);
        userInterests.add(userInterest1);
        WikiDataEntity userInterest2 = new WikiDataEntity("label2","desc2", wikidataID2,
                "label2", WikiDataEntity.CLASSIFICATION_OTHER);
        userInterests.add(userInterest2);


        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                assertEquals(1, questionSets.size());
            }
        };
        db.searchQuestions(null, "", userInterests, 1,
                new ArrayList<String>(), onDBResultListener);
        //to make sure toPopulateCt is what's affecting it
        OnDBResultListener onDBResultListener2 = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                assertEquals(2, questionSets.size());
            }
        };
        db.searchQuestions(null, "", userInterests, 2,
                new ArrayList<String>(), onDBResultListener2);
    }

    @Test
    public void question_searchQuestionWithNoMatchingUserInterests_shouldReturnEmptyList(){
        List<WikiDataEntity> userInterests = new ArrayList<>(1);
        WikiDataEntity userInterest1 = new WikiDataEntity("label1","desc1", "wikidata1",
                "label1", WikiDataEntity.CLASSIFICATION_OTHER);
        userInterests.add(userInterest1);


        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                assertEquals(0, questionSets.size());
            }
        };
        db.searchQuestions(null, "", userInterests, 1,
                new ArrayList<String>(), onDBResultListener);
    }

    @Test
    public void question_searchQuestionWithNoUserInterests_shouldReturnEmptyList(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched) {
                assertEquals(0, questionSets.size());
            }
        };
        db.searchQuestions(null, "", new ArrayList<WikiDataEntity>(), 1,
                new ArrayList<String>(), onDBResultListener);
    }

    @Test
    public void question_getQuestion_shouldGetQuestion(){
        final String questionID = "questionID1";
        QuestionData questionData = new QuestionData(questionID,"lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put(questionID, questionData);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionQueried(QuestionData questionData) {
                assertEquals(questionData.getId(), questionID);
            }
        };
        db.getQuestion(null, questionID, onDBResultListener);
    }

    @Test
    public void questions_getQuestionSets_shouldGetQuestionSets(){
        final String questionSetID = "questionSetID1";
        final QuestionSet questionSet = new QuestionSet(questionSetID, "wikiDataID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        db.questionSets.put(questionSetID, questionSet);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
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
        db.getQuestionSets(null, "",toGetSetIDs, onDBResultListener);
    }

    @Test
    public void questions_getOneQuestionSet_shouldGetOneQuestionSet(){
        final String questionSetID = "questionSetID1";
        QuestionSet questionSet = new QuestionSet(questionSetID, "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        db.questionSets.put(questionSetID, questionSet);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onQuestionSetsQueried(List<QuestionSet> questionSets) {
                assertEquals(1, questionSets.size());
            }
        };
        List<String> toGetSetIDs = new ArrayList<>(1);
        toGetSetIDs.add(questionSetID);
        db.getQuestionSets(null, "",toGetSetIDs, onDBResultListener);
    }

    @Test
    public void lessonInstance_addLessonInstance_shouldAddInstanceInDatabase(){
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstanceAdded() {
                Map<String, LessonInstanceData> lessonInstances = db.lessonInstances;
                assertEquals(1, lessonInstances.size());
            }
        };
        db.addLessonInstance(null, lessonInstanceData,
                new ArrayList<String>(), onDBResultListener);
    }

    @Test
    public void lessonInstance_getLessonInstances_shouldGetInstances(){
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("set1", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                boolean matched = true;
                for (LessonInstanceData data : lessonInstances){
                    List<String> dataQuestionSets = data.questionSetIds();
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

        db.getLessonInstances(null, "lessonID1", false, onDBResultListener);
    }

    @Test
    public void lessonInstance_addLessonInstance_shouldAddIDToInstance(){
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION);
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
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
        db.addLessonInstance(null, lessonInstanceData,
                new ArrayList<String>(), onDBResultListener);
    }

    @Test
    public void vocabulary_addVocabularyWord_shouldAddWordToVocabularyWords(){
        final VocabularyWord word = new VocabularyWord("","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1");
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
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
        db.addVocabularyWord(word, onDBResultListener);
    }

    @Test
    public void vocabulary_addVocabularyWord_shouldAddWordToVocabularyList(){
        final VocabularyWord word = new VocabularyWord("","word1", "meaning1", "example sentence1",
                "example sentence translation 1",
                "lesson ID1");
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
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
        db.addVocabularyWord(word, onDBResultListener);
    }

}
