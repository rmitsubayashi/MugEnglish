package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;


import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class How_are_you_doing extends Lesson {
    public static final String KEY = "How_are_you_doing";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personJP;
        private final String firstNameEN;

        private QueryResult(
                String personID,
                String personJP,
                String firstNameEN)
        {
            this.personID = personID;
            this.personJP = personJP;
            this.firstNameEN = firstNameEN;
        }
    }

    public How_are_you_doing(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?person ?personLabel ?firstNameEN " +
                "WHERE " +
                "{" +


                "    ?person wdt:P735 ?firstName . " + //has a first name
                "    ?firstName rdfs:label ?firstNameEN . " +
                "    FILTER (LANG(?firstNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?person
                "} ";

    }

    @Override
    protected synchronized void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String firstNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "firstNameEN");

            QueryResult qr = new QueryResult(personID, personJP, firstNameEN);
            queryResults.add(qr);
        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> chatQuestion = createChatQuestion(qr);
            questionSet.add(chatQuestion);
            List<QuestionData> chatQuestion2 = createChatQuestion2(qr);
            questionSet.add(chatQuestion2);
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, null));
        }
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "How are you doing?","元気ですか",
                "How are you doing?\nGood.","元気ですか。元気です。", KEY));
        words.add(new VocabularyWord("", "good","元気です",
                "How are you doing?\nGood.","元気ですか。元気です。", KEY));
        return words;
    }

    private List<QuestionData> createChatQuestion(QueryResult qr){
        String from = qr.personJP;
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "hello");
        ChatQuestionItem chatItem2 = new ChatQuestionItem(true, "hello " + qr.firstNameEN);
        ChatQuestionItem chatItem3 = new ChatQuestionItem(false, "how are you doing?");
        ChatQuestionItem chatItem4 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(4);
        chatItems.add(chatItem1);
        chatItems.add(chatItem2);
        chatItems.add(chatItem3);
        chatItems.add(chatItem4);
        String question = QuestionSerializer.serializeChatQuestion(from, chatItems);
        String answer = "good";
        List<String> choices = new ArrayList<>(2);
        choices.add("good");
        choices.add("how are you doing");
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createChatQuestion2(QueryResult qr){
        String from = qr.personJP;
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "hello");
        ChatQuestionItem chatItem2 = new ChatQuestionItem(true, "hello " + qr.firstNameEN);
        ChatQuestionItem chatItem3 = new ChatQuestionItem(true, " how are you doing?");
        ChatQuestionItem chatItem4 = new ChatQuestionItem(false, "good. how are you doing?");
        ChatQuestionItem chatItem5 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(5);
        chatItems.add(chatItem1);
        chatItems.add(chatItem2);
        chatItems.add(chatItem3);
        chatItems.add(chatItem4);
        chatItems.add(chatItem5);
        String question = QuestionSerializer.serializeChatQuestion(from, chatItems);
        String answer = "good";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.CHAT);
        data.setQuestion(question);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion();
        List<QuestionData> translateQuestion = createTranslateQuestion();
        List<QuestionData> spelling = spellingQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(3);
        questionSet.add(sentencePuzzleQuestion);
        questionSet.add(translateQuestion);
        questionSet.add(spelling);

        return questionSet;
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(){
        List<String> pieces = new ArrayList<>();
        pieces.add("how");
        pieces.add("are");
        pieces.add("you");
        pieces.add("doing");
        pieces.add("?");
        return pieces;
    }

    private String genericQuestionJP(){
        return "元気ですか";
    }

    private String genericAnswerEN(){
        return "how are you doing";
    }

    private String puzzlePiecesAnswer(){
        return QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces());
    }

    private List<QuestionData> createSentencePuzzleQuestion(){
        String question = this.genericQuestionJP();
        List<String> choices = this.puzzlePieces();
        String answer = puzzlePiecesAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createTranslateQuestion(){
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(genericQuestionJP());
        data.setChoices(null);
        data.setAnswer(genericAnswerEN());


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> spellingQuestion(){
        String answer = "good";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion("元気です");
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> instructionsQuestion = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(instructionsQuestion);
        return questionSet;
    }

    //lets the user freely introduce themselves
    private String instructionQuestionQuestion(){
        return "私が元気であるか聞いてください";
    }

    private String instructionQuestionAnswer(){
        return "How are you doing?";
    }

    private List<QuestionData> createInstructionQuestion(){
        String question = this.instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}