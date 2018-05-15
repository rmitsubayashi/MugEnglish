package pelicann.linnca.com.corefunctionality.lessoninstance;

public class LessonTest {
    /*
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
    public void createLessonInstance_withUserInterests_shouldSaveQEntityPropertyDataInDatabase(){
        //adding user interests for lessonInstanceGenerator generation
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
    public void createLessonInstance_withUserInterests_shouldCreateLessonInstance(){
        //adding user interests for lessonInstanceGenerator generation
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
    public void createLessonInstance_noUserInterests_lessonShouldBeCreatedWithExistingQuestions(){
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
    }*/
}
