package pelicann.linnca.com.corefunctionality.lessoninstance;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.MockFirebaseDB;
import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lesson.LessonFactory;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_class;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

import static org.junit.Assert.assertEquals;
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
        Lesson lesson = LessonFactory.getLesson(Food_class.KEY);
        assertTrue(lesson instanceof Food_class);
    }

    @Test
    public void lessonFactory_parseLesson_shouldReturnNullOnInvalidLessonKey(){
        Lesson lesson = LessonFactory.getLesson("invalid key");
        assertNull(lesson);
    }

    @Test
    public void createLessonInstance_withUserInterests_shouldSaveEntityPropertyDataInDatabase(){
        //adding user interests for instanceGenerator generation
        List<WikiDataEntity> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntity("安倍晋三", "desc", "Q132345", "あべしんぞう"));
        userInterests.add(new WikiDataEntity("バラク・オバマ ", "desc", "Q76", "ばらくおばま"));
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

        InstanceGenerator.LessonInstanceGeneratorListener lessonListener = new InstanceGenerator.LessonInstanceGeneratorListener() {
            @Override
            public void onLessonCreated(LessonInstanceData data) {
                assertTrue(db.entityPropertyData.size() == 1);
            }
            @Override
            public void onNoConnection(){
                assertTrue(false);
            }
        };
        InstanceGenerator instanceGenerator = new MockInstanceGenerator();
        instanceGenerator.createInstance(mockConnector, db, lessonListener, null);
    }


    @Test
    public void createLessonInstance_withUserInterests_shouldCreateLessonInstance(){
        //adding user interests for instanceGenerator generation
        List<WikiDataEntity> userInterests = new ArrayList<>(2);
        userInterests.add(new WikiDataEntity("安倍晋三", "desc", "Q132345", "あべしんぞう"));
        userInterests.add(new WikiDataEntity("バラク・オバマ ", "desc", "Q76", "ばらくおばま"));
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

        InstanceGenerator.LessonInstanceGeneratorListener lessonListener = new InstanceGenerator.LessonInstanceGeneratorListener() {
            @Override
            public void onLessonCreated(LessonInstanceData data) {
                assertTrue(db.lessonInstances.size() == 1);
            }
            @Override
            public void onNoConnection(){}
        };
        InstanceGenerator instanceGenerator = new MockInstanceGenerator();
        instanceGenerator.createInstance(mockConnector, db, lessonListener, null);
    }


    @Test
    public void createLessonInstance_noUserInterests_lessonShouldBeCreatedWithExistingEntityPropertyData(){
        //adding preset data
        EntityPropertyData data = new EntityPropertyData();
        data.setWikidataID("Q132345");
        List<EntityPropertyData> dataList = new ArrayList<>();
        dataList.add(data);
        db.addEntityPropertyData("lessonKey", dataList, new OnDBResultListener() {});
        EndpointConnectorReturnsXML mockConnector = new EndpointConnectorReturnsXML() {
            @Override
            public void fetchDOMFromGetRequest(OnFetchDOMListener listener, List<String> queryList) {
                //shouldn't be called
                assertTrue(false);
            }
        };

        InstanceGenerator.LessonInstanceGeneratorListener lessonListener = new InstanceGenerator.LessonInstanceGeneratorListener() {
            @Override
            public void onLessonCreated(LessonInstanceData data) {
                assertEquals(db.lessonInstances.size(),1);
            }

            @Override
            public void onNoConnection(){}
        };
        InstanceGenerator instanceGenerator = new MockInstanceGenerator();
        instanceGenerator.createInstance(mockConnector, db, lessonListener, null);
    }
}
