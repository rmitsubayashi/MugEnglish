package pelicann.linnca.com.corefunctionality.lessoninstance;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.MockFirebaseDB;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lesson.LessonFactory;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Goodbye_bye;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Hello_my_name_is_NAME;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LessonTest {
    //this is a mock database
    private MockFirebaseDB db;

    @Before
    public void setUp(){
        db = new MockFirebaseDB();
    }

    @Test
    public void lessonFactory_parseLesson_shouldCreateLessonClass(){
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Goodbye_bye.KEY,null, db,null);
        assertTrue(lessonInstanceGenerator instanceof Goodbye_bye);
    }

    @Test
    public void lessonFactory_parseLesson_shouldReturnNullOnInvalidLessonKey(){
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson("invalid key", null, db, null);
        assertNull(lessonInstanceGenerator);
    }

    @Test
    public void lessonWithOnlyGenericQuestions_saveQuestions_DBShouldHaveQuestionCountEqualToNumberOfGenericQuestions() throws Exception{
        LessonInstanceGenerator lessonInstanceGeneratorWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, null);
        lessonInstanceGeneratorWithOnlyGenericQuestions.saveGenericQuestions();
        List<List<QuestionData>> preGenericQuestions = lessonInstanceGeneratorWithOnlyGenericQuestions.getPreGenericQuestions();
        int questionCt = 0;
        for (List<QuestionData> questionVariations : preGenericQuestions){
            questionCt += questionVariations.size();
        }
        assertEquals(questionCt, db.questions.size());
    }


    @Test
    public void lessonWithOnlyGenericQuestions_createInstance_lessonShouldContainGenericQuestions() throws Exception{
        LessonInstanceGenerator.LessonListener lessonListener = new LessonInstanceGenerator.LessonListener() {
            @Override
            public void onLessonCreated() {
                OnDBResultListener onDBResultListener = new OnDBResultListener() {
                    @Override
                    public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, null);
                        List<List<QuestionData>> preGenericQuestions = lessonInstanceGenerator.getPreGenericQuestions();
                        boolean noMatch = false;
                        for (LessonInstanceData instance : lessonInstances){
                            List<String> questions = instance.allQuestionIds();
                            assertEquals(preGenericQuestions.size(), questions.size());
                        }
                        assertFalse(noMatch);
                    }
                };
                db.getLessonInstances(null, Goodbye_bye.KEY, false, onDBResultListener);
            }
            @Override
            public void onNoConnection(){}
        };
        LessonInstanceGenerator lessonInstanceGeneratorWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, lessonListener);
        lessonInstanceGeneratorWithOnlyGenericQuestions.createInstance(null);
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithUserInterests_shouldSaveQuestionSetsInDatabase(){
        //adding user interests for lessonInstanceGenerator generation
        List<WikiDataEntity> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntity("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntity.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntity("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntity.CLASSIFICATION_PERSON));
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

        LessonInstanceGenerator.LessonListener lessonListener = new LessonInstanceGenerator.LessonListener() {
            @Override
            public void onLessonCreated() {
                assertTrue(db.questionSets.size() > 0);
            }
            @Override
            public void onNoConnection(){}
        };
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        assertEquals("the question count for this question is not two. Need to change the test to fit the new version of the lessonInstanceGenerator",
                2, 2);
        lessonInstanceGenerator.createInstance(null);
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithUserInterests_shouldCreateOneLessonInstance(){
        //adding user interests for lessonInstanceGenerator generation
        List<WikiDataEntity> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntity("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntity.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntity("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntity.CLASSIFICATION_PERSON));
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

        LessonInstanceGenerator.LessonListener lessonListener = new LessonInstanceGenerator.LessonListener() {
            @Override
            public void onLessonCreated() {
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                assertEquals(1, instanceDataMap.size());
            }
            @Override
            public void onNoConnection(){}
        };
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lessonInstanceGenerator.createInstance(null);
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithUserInterests_lessonInstanceShouldHaveDynamicQuestions(){
        //adding user interests for lessonInstanceGenerator generation
        List<WikiDataEntity> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntity("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntity.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntity("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntity.CLASSIFICATION_PERSON));
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

        LessonInstanceGenerator.LessonListener lessonListener = new LessonInstanceGenerator.LessonListener() {
            @Override
            public void onLessonCreated() {
                LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, null, db, null);
                List<List<QuestionData>> preGenericQuestions = lessonInstanceGenerator.getPreGenericQuestions();
                List<List<QuestionData>> postGenericQuestions = lessonInstanceGenerator.getPostGenericQuestions();
                //we only want to know how many generic questions will be in the lessonInstanceGenerator instance
                int genericQuestionCt = preGenericQuestions.size() + postGenericQuestions.size();

                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                //only one loop (assertion of only one lessonInstanceGenerator instance is in a different test)
                for (Map.Entry<String, LessonInstanceData> entry : instanceDataMap.entrySet()) {
                    int instanceQuestionSize = entry.getValue().allQuestionIds().size();
                    assertTrue("instance size:" + instanceQuestionSize +
                            " generic question size:" + genericQuestionCt,
                            instanceQuestionSize > genericQuestionCt);
                }
            }
            @Override
            public void onNoConnection(){}
        };
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lessonInstanceGenerator.createInstance(null);
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithNoUserInterests_lessonShouldBeCreatedWithExistingQuestions(){
        //adding preset data
        List<List<String>> questionSet1 = new ArrayList<>(1);
        List<String> questions1 = new ArrayList<>(1);
        questions1.add("questionID1");
        questionSet1.add(questions1);
        db.questionSets.put("questionSetID1", new QuestionSet("questionSetID1", "wikiDataLabel1", "interestLabel1", questionSet1, new ArrayList<String>(),1));
        QuestionData questionData1 = new QuestionData("questionID1","lessonID1", QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        db.questions.put("questionID1", questionData1);
        List<List<String>> questionSet2 = new ArrayList<>(1);
        List<String> questions2 = new ArrayList<>(1);
        questions2.add("questionID2");
        questionSet2.add(questions2);
        db.questionSets.put("questionSetID2", new QuestionSet("questionSetID2", "wikiDataLabel2", "interestLabel2", questionSet2, new ArrayList<String>(),1));
        QuestionData questionData2 = new QuestionData("questionID2","lessonID2", QuestionTypeMappings.TRUEFALSE,
                "question2", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
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

        LessonInstanceGenerator.LessonListener lessonListener = new LessonInstanceGenerator.LessonListener() {
            @Override
            public void onLessonCreated() {
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                //only one loop (assertion of only one lessonInstanceGenerator instance is in a different test)
                for (Map.Entry<String, LessonInstanceData> entry : instanceDataMap.entrySet()) {
                    List<String> questionSetIds = entry.getValue().questionSetIds();
                    assertTrue(questionSetIds.contains("questionSetID1"));
                    //this means a dynamic question set was created
                    // (random question set) + @
                    assertTrue(questionSetIds.size() > 1);
                }
            }

            @Override
            public void onNoConnection(){}
        };
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lessonInstanceGenerator.createInstance(null);
    }

    @Test
    public void lessonWithDynamicQuestions_createInstanceWithOneUserInterest_lessonShouldBeCreatedWithUserInterestAndRandomQuestions(){
        //adding one user interest for lessonInstanceGenerator generation
        List<WikiDataEntity> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntity("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntity.CLASSIFICATION_PERSON));
        db.userInterests = userInterests;
        //adding preset data
        List<List<String>> questionSet1 = new ArrayList<>(1);
        List<String> questions1 = new ArrayList<>(1);
        questions1.add("questionID1");
        questionSet1.add(questions1);
        db.questionSets.put("questionSetID1", new QuestionSet("questionSetID1", "WikiDataLabel1", "interestLabel1", questionSet1, new ArrayList<String>(),1));
        QuestionData questionData1 = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        db.questions.put("questionID1", questionData1);

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
            }
        };

        LessonInstanceGenerator.LessonListener lessonListener = new LessonInstanceGenerator.LessonListener() {
            @Override
            public void onLessonCreated() {
                Map<String, LessonInstanceData> instanceDataMap = db.lessonInstances;
                //only one loop (assertion of only one lessonInstanceGenerator instance is in a different test)
                for (Map.Entry<String, LessonInstanceData> entry : instanceDataMap.entrySet()) {
                    List<String> questionSetIds = entry.getValue().questionSetIds();
                    assertTrue(questionSetIds.contains("questionSetID1"));
                    assertTrue(questionSetIds.contains("questionSetID2"));
                }
            }
            @Override
            public void onNoConnection(){}
        };
        LessonInstanceGenerator lessonInstanceGenerator = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lessonInstanceGenerator.createInstance(null);
    }
}
