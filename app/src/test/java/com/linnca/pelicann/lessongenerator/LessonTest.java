package com.linnca.pelicann.lessongenerator;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.db.MockFirebaseDB;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.lessons.Goodbye_bye;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class LessonTest {
    //this is a mock database
    private MockFirebaseDB db;

    @Before
    public void setUp(){
        db = new MockFirebaseDB();
    }

    @Test
    public void lessonFactory_parseLesson_shouldCreateLessonClass(){
        Lesson lesson = LessonFactory.parseLesson(Goodbye_bye.KEY,null, db,null);
        assertTrue(lesson instanceof Goodbye_bye);
    }

    @Test
    public void lessonFactory_parseLesson_shouldReturnNullOnInvalidLessonKey(){
        Lesson lesson = LessonFactory.parseLesson("invalid key", null, db, null);
        assertNull(lesson);
    }

    @Test
    public void lessonWithOnlyGenericQuestions_saveQuestions_DBShouldSaveGenericQuestions() throws Exception{
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, null);
        lessonWithOnlyGenericQuestions.saveGenericQuestions();
        List<List<String>> questionIDSets = lessonWithOnlyGenericQuestions.getGenericQuestionIDSets();
        boolean noMatch = false;
        for (List<String> questionVariations : questionIDSets){
            for (String questionID : questionVariations){
                if (!db.questions.containsKey(questionID)){
                    noMatch = true;
                    break;
                }
            }
        }
        assertFalse(noMatch);
    }

    @Test
    public void lessonWithOnlyGenericQuestions_saveQuestions_DBShouldHaveQuestionCountEqualToNumberOfGenericQuestions() throws Exception{
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, null);
        lessonWithOnlyGenericQuestions.saveGenericQuestions();
        List<List<String>> questionIDSets = lessonWithOnlyGenericQuestions.getGenericQuestionIDSets();
        int questionCt = 0;
        for (List<String> questionVariations : questionIDSets){
            questionCt += questionVariations.size();
        }
        assertEquals(questionCt, db.questions.size());
    }


    @Test
    public void lessonWithOnlyGenericQuestions_createInstance_lessonShouldContainGenericQuestions() throws Exception{
        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                OnResultListener onResultListener = new OnResultListener() {
                    @Override
                    public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                        Lesson lesson = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, null);
                        List<List<String>> questionIDSets = lesson.getGenericQuestionIDSets();
                        //flatten out
                        List<String> genericQuestionIDs = new ArrayList<>();
                        for (List<String> questionIDs : questionIDSets){
                            genericQuestionIDs.addAll(questionIDs);
                        }
                        boolean noMatch = false;
                        for (LessonInstanceData instance : lessonInstances){
                            List<String> questions = instance.getQuestionIds();
                            for (String id : questions){
                                if (!genericQuestionIDs.contains(id))
                                    noMatch = true;
                            }
                        }
                        assertFalse(noMatch);
                    }
                };
                db.getLessonInstances(Goodbye_bye.KEY, false, onResultListener);
            }
        };
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, lessonListener);
        lessonWithOnlyGenericQuestions.createInstance();
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithUserInterests_shouldSaveQuestionSetsInDatabase(){
        //adding user interests for lesson generation
        List<WikiDataEntryData> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntryData("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntryData.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntryData("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntryData.CLASSIFICATION_PERSON));
        db.userInterests = userInterests;

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
                for (String query : queryList) {
                    InputStream inputStream;
                    if (query.contains("Q132345")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_shinzo_abe");
                    } else if (query.contains("Q76")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_barack_obama");
                    } else {
                        assertTrue(false);
                        return;
                    }

                    try {
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        Document document = documentBuilder.parse(inputStream);
                        document.getDocumentElement().normalize();

                        listener.onFetchDOM(document);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (listener.shouldStop()){
                        listener.onStop();
                        return;
                    }
                }

            }
        };

        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                assertTrue(db.questionSets.size() > 0);
            }
        };
        Lesson lesson = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        assertEquals("the question count for this question is not two. Need to change the test to fit the new version of the lesson",
                lesson.questionSetsToPopulate, 2);
        lesson.createInstance();
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithUserInterests_shouldCreateOneLessonInstance(){
        //adding user interests for lesson generation
        List<WikiDataEntryData> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntryData("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntryData.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntryData("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntryData.CLASSIFICATION_PERSON));
        db.userInterests = userInterests;

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
                for (String query : queryList) {
                    InputStream inputStream;
                    if (query.contains("Q132345")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_shinzo_abe");
                    } else if (query.contains("Q76")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_barack_obama");
                    } else {
                        assertTrue(false);
                        return;
                    }

                    try {
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        Document document = documentBuilder.parse(inputStream);
                        document.getDocumentElement().normalize();

                        listener.onFetchDOM(document);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (listener.shouldStop()){
                        listener.onStop();
                        return;
                    }
                }

            }
        };

        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                assertEquals(1, instanceDataMap.size());
            }
        };
        Lesson lesson = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lesson.createInstance();
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithUserInterests_lessonInstanceShouldHaveDynamicQuestions(){
        //adding user interests for lesson generation
        List<WikiDataEntryData> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntryData("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntryData.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntryData("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntryData.CLASSIFICATION_PERSON));
        db.userInterests = userInterests;

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
                for (String query : queryList) {
                    InputStream inputStream;
                    if (query.contains("Q132345")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_shinzo_abe");
                    } else if (query.contains("Q76")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_barack_obama");
                    } else {
                        assertTrue(false);
                        return;
                    }

                    try {
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        Document document = documentBuilder.parse(inputStream);
                        document.getDocumentElement().normalize();

                        listener.onFetchDOM(document);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (listener.shouldStop()){
                        listener.onStop();
                        return;
                    }
                }

            }
        };

        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                List<List<String>> genericQuestions = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, null, db, null).getGenericQuestionIDSets();
                //we only want to know how many generic questions will be in the lesson instance
                int genericQuestionCt = genericQuestions.size();
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                //only one loop (assertion of only one lesson instance is in a different test)
                for (Map.Entry<String, LessonInstanceData> entry : instanceDataMap.entrySet()) {
                    int instanceQuestionSize = entry.getValue().getQuestionIds().size();
                    assertTrue("instance size:" + instanceQuestionSize +
                            " generic question size:" + genericQuestionCt,
                            instanceQuestionSize > genericQuestionCt);
                }
            }
        };
        Lesson lesson = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lesson.createInstance();
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithNoUserInterests_lessonShouldBeCreatedWithExistingQuestions(){
        //adding preset data
        List<List<String>> questionSet1 = new ArrayList<>(1);
        List<String> questions1 = new ArrayList<>(1);
        questions1.add("questionID1");
        questionSet1.add(questions1);
        db.questionSets.put("questionSetID1", new QuestionSet("questionSetID1", "wikiDataLabel1", "interestLabel1", questionSet1, new ArrayList<String>(),1));
        QuestionData questionData1 = new QuestionData("questionID1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID1", questionData1);
        List<List<String>> questionSet2 = new ArrayList<>(1);
        List<String> questions2 = new ArrayList<>(1);
        questions2.add("questionID2");
        questionSet2.add(questions2);
        db.questionSets.put("questionSetID2", new QuestionSet("questionSetID2", "wikiDataLabel2", "interestLabel2", questionSet2, new ArrayList<String>(),1));
        QuestionData questionData2 = new QuestionData("questionID2","lessonID2", "topic2", Question_TrueFalse.QUESTION_TYPE,
                "question2", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID2", questionData2);

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
                for (String query : queryList) {
                    InputStream inputStream;
                    if (query.contains("Q132345")) {
                        inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_shinzo_abe");
                    } else {
                        assertTrue(false);
                        return;
                    }
                    try {
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        Document document = documentBuilder.parse(inputStream);
                        document.getDocumentElement().normalize();

                        listener.onFetchDOM(document);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (listener.shouldStop()){
                        listener.onStop();
                        return;
                    }
                }
            }
        };

        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                //only one loop (assertion of only one lesson instance is in a different test)
                for (Map.Entry<String, LessonInstanceData> entry : instanceDataMap.entrySet()) {
                    List<String> questionSetIds = entry.getValue().getQuestionSetIds();
                    assertTrue(questionSetIds.contains("questionSetID1"));
                    //this means a dynamic question set was created
                    // (random question set) + @
                    assertTrue(questionSetIds.size() > 1);
                }
            }
        };
        Lesson lesson = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lesson.createInstance();
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithOneUserInterest_lessonShouldBeCreatedWithUserInterestAndRandomQuestions(){
        //adding one user interest for lesson generation
        List<WikiDataEntryData> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntryData("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntryData.CLASSIFICATION_PERSON));
        db.userInterests = userInterests;
        //adding preset data
        List<List<String>> questionSet1 = new ArrayList<>(1);
        List<String> questions1 = new ArrayList<>(1);
        questions1.add("questionID1");
        questionSet1.add(questions1);
        db.questionSets.put("questionSetID1", new QuestionSet("questionSetID1", "WikiDataLabel1", "interestLabel1", questionSet1, new ArrayList<String>(),1));
        QuestionData questionData1 = new QuestionData("questionID1","lessonID1", "topic1", Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID1", questionData1);

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
            }
        };

        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                //only one loop (assertion of only one lesson instance is in a different test)
                for (Map.Entry<String, LessonInstanceData> entry : instanceDataMap.entrySet()) {
                    List<String> questionSetIds = entry.getValue().getQuestionSetIds();
                    assertTrue(questionSetIds.contains("questionSetID1"));
                    assertTrue(questionSetIds.contains("questionSetID2"));
                }
            }
        };
        Lesson lesson = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lesson.createInstance();
    }
}
