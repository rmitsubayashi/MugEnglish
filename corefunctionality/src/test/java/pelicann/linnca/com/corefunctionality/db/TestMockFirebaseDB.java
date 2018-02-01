package pelicann.linnca.com.corefunctionality.db;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyListWord;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void searchEntityPropertyData_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataSearched(List<EntityPropertyData> quesdatationSets, List<WikiDataEntity> userInterestsSearched) {
                called[0] = true;
            }
        };
        //OnDBResultListener onDBResultListener = mock(OnDBResultListener.class);
        db.searchEntityPropertyData(null, "", new ArrayList<WikiDataEntity>(),
                0, new ArrayList<EntityPropertyData>(), onDBResultListener);
        assertTrue(called[0]);
        //verify(onDBResultListener, times(2)).onQuestionsQueried(new ArrayList<String>(), new ArrayList<WikiDataEntity>());
    }

    @Test
    public void addEntityPropertyData_call_resultListenerShouldBeCalled(){
        final boolean[] called = new boolean[]{false, false};
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onEntityPropertyDataAdded(EntityPropertyData data) {
                called[0] = true;
            }
        };
        //need at least one entity property data for the listener to be called
        List<EntityPropertyData> data = new ArrayList<>();
        data.add(new EntityPropertyData("","","wikiDataID1",null,null));
        db.addEntityPropertyData("", data,
                onDBResultListener);
        assertTrue(called[0] && called[1]);
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
    public void entityPropertyData_addEntityPropertyData_shouldAddData(){
        List<EntityPropertyData> data = new ArrayList<>(1);
        String id = "wikiDataID1";
        data.add(new EntityPropertyData("","",id,null,null));
        db.addEntityPropertyData("", data,
                new OnDBResultListener() {
        });
        assertEquals(1, db.entityPropertyData.size());
        assertEquals(1, db.entityPropertyData.get(id).size());
    }

    @Test
    public void entityPropertyData_addMoreThanOnePerWikidataID_shouldAddBoth(){
        List<EntityPropertyData> data = new ArrayList<>(2);
        String id = "wikiDataID1";
        data.add(new EntityPropertyData("1","",id,null,null));
        data.add(new EntityPropertyData("2","",id,null,null));


        OnDBResultListener onDBResultListener = new OnDBResultListener() {};
        db.addEntityPropertyData("lessonID1", data, onDBResultListener);
        assertEquals(2, db.entityPropertyData.get(id).size());
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
        QuestionData questionData = new QuestionData(questionID,"lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
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
