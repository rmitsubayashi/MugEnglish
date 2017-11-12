package com.linnca.pelicann.lessongenerator;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.db.MockFirebaseDB;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.lessons.Goodbye_bye;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
                db.getLessonInstances(Goodbye_bye.KEY, onResultListener);
            }
        };
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, null, db, lessonListener);
        lessonWithOnlyGenericQuestions.createInstance();
    }

    @Test
    public void lessonWithDynamicQuestions_createInstance_makingSureRequiredUserInterestCountIsTwo(){
        //since we are only adding user interests sufficient enough for the test,
        //if we change the lesson we are changing to require more interests,
        //all the tests will fail because we don't have enough user interests
        Lesson lessonToTest = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, null, null, null);
        assertEquals(lessonToTest.questionSetsToPopulate, 2);
    }


    /*
    @Test
    public void lessonWithDynamicQuestions_createInstance_lessonShouldContainDynamicQuestions(){
        //we can either pick a question with only dynamic questions or with a mix and subtract
        // the generic questions.
        //od the latter so it's less worrisome to play around with questions.

        //adding user interests for lesson generation
        List<WikiDataEntryData> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntryData("安倍晋三", "desc", "Q132345", "あべしんぞう", WikiDataEntryData.CLASSIFICATION_PERSON));
        userInterests.add(new WikiDataEntryData("バラク・オバマ ", "desc", "Q76", "ばらくおばま", WikiDataEntryData.CLASSIFICATION_PERSON));
        db.userInterests = userInterests;

        //the query returns a result with 'person', 'personEN', and 'personLabel'
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public Document fetchDOMFromGetRequest(String... query) throws Exception {
                String actualQuery = query[0];
                assertTrue("query did not contain requested wikiDataID",
                        actualQuery.contains("Q132345") || actualQuery.contains("Q76"));
                InputStream inputStream;
                if (actualQuery.contains("Q132345")) {
                    inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_shinzo_abe.txt");
                } else if (actualQuery.contains("Q76")){
                    inputStream = this.getClass().getClassLoader().getResourceAsStream("person_query_successful_example_barack_obama.txt");
                } else {
                    return null;
                }

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);
                document.getDocumentElement().normalize();

                return document;
            }
        };

        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                int lessonGenericQuestionCt = LessonFactory.parseLesson(
                        Hello_my_name_is_NAME.KEY, null,
                        null, null
                ).getGenericQuestions().size();

                assertTrue(db.questions.size() > lessonGenericQuestionCt);
                assertTrue(db.questions.size() < lessonGenericQuestionCt);
            }
        };
        Lesson lesson = LessonFactory.parseLesson(Hello_my_name_is_NAME.KEY, mockConnector, db, lessonListener);
        lesson.createInstance();
    }*/
}
