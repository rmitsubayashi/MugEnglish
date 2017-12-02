package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_Chat;
import com.linnca.pelicann.questions.Question_Chat_MultipleChoice;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_Instructions;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.QuestionResponseChecker;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
* Goals for this lesson:
* The user should be able to greet each other
* and introduce themselves
* */

public class Hello_my_name_is_NAME extends Lesson {
    public static final String KEY = "Hello_my_name_is_NAME";

    private final List<QueryResult> queryResults = Collections.synchronizedList(
            new ArrayList<QueryResult>()
    );
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
        }
    }

    public Hello_my_name_is_NAME(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?person ?personLabel ?personEN " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?person rdfs:label ?personEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?person
                "} ";

    }

    @Override
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");

            QueryResult qr = new QueryResult(personID, personEN, personJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> chatMultipleChoice = createChatMultipleChoiceQuestion(qr);
            questionSet.add(chatMultipleChoice);
            List<QuestionData> chat = createChatQuestion(qr);
            questionSet.add(chat);
            List<QuestionData> sentencePuzzle = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzle);
            List<QuestionData> fillInBlank = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlank);
            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private String formatSentenceEN(QueryResult qr){
        return "Hello. My name is " + qr.personEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return "こんにちは。私の名前は" + qr.personJP + "です。";
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord hello = new VocabularyWord("","hello", "こんにちは",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord my = new VocabularyWord("","my", "私の",
                "My name is " + qr.personEN + ".", "私の名前は" + qr.personJP + "です。", KEY);
        VocabularyWord name = new VocabularyWord("", "name","名前",
                "My name is " + qr.personEN + ".", "私の名前は" + qr.personJP + "です。", KEY);
        VocabularyWord is = new VocabularyWord("", "is","~は",
                "My name is " + qr.personEN + ".", "私の名前は" + qr.personJP + "です。", KEY);
        List<VocabularyWord> words = new ArrayList<>(4);
        words.add(hello);
        words.add(my);
        words.add(name);
        words.add(is);
        return words;
    }

    //straight-forward question so the user understands that this is a natural response
    private List<QuestionData> createChatMultipleChoiceQuestion(QueryResult qr){
        String from = qr.personJP;
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "hello");
        ChatQuestionItem chatItem2 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(chatItem2);
        String question = QuestionUtils.formatChatQuestion(from, chatItems);
        String answer = "hello";
        List<String> choices = new ArrayList<>(1);
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_Chat_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    //enforces the idea of the previous chat question and also practices spelling
    private List<QuestionData> createChatQuestion(QueryResult qr){
        String from = qr.personJP;
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "hello");
        ChatQuestionItem chatItem2 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(chatItem2);
        String question = QuestionUtils.formatChatQuestion(from, chatItems);
        String answer = "hello";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_Chat.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    //this introduces the whole phrase
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add("hello");
        pieces.add("my name is");
        pieces.add(qr.personEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    //lets users practice  the latter part of introductions
    private String fillInBlankQuestion(QueryResult qr){
        return "Hello. " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + " " + qr.personEN + ".";
    }

    private String fillInBlankAnswer(){
        return "My name is";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<String> questionIDs = new ArrayList<>();
        questionIDs.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, 1));
        List<String> questionIDs2 = new ArrayList<>();
        questionIDs2.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, 2));
        List<String> questionIDs3 = new ArrayList<>();
        questionIDs3.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, 3));
        List<List<String>> questionSets = new ArrayList<>();
        questionSets.add(questionIDs);
        questionSets.add(questionIDs2);
        questionSets.add(questionIDs3);
        return questionSets;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        QuestionData toSave1 = createSpellingSuggestiveQuestion();
        String id1 = LessonGeneratorUtils.formatGenericQuestionID(KEY, 1);
        toSave1.setId(id1);
        QuestionData toSave2 = createSpellingQuestion();
        String id2 = LessonGeneratorUtils.formatGenericQuestionID(KEY, 2);
        toSave2.setId(id2);
        QuestionData toSave3 = createInstructionQuestion();
        String id3 = LessonGeneratorUtils.formatGenericQuestionID(KEY, 3);
        toSave3.setId(id3);

        List<QuestionData> questions = new ArrayList<>(3);
        questions.add(toSave1);
        questions.add(toSave2);
        questions.add(toSave3);
        return questions;

    }

    private QuestionData createSpellingSuggestiveQuestion(){
        String question = "こんにちは";
        String answer = "hello";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        return data;
    }

    private QuestionData createSpellingQuestion(){
        String question = "こんにちは";
        String answer = "hello";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Spelling.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        return data;
    }

    //lets the user freely introduce themselves
    private String instructionQuestionQuestion(){
        return "自己紹介をしてください";
    }

    private String instructionQuestionAnswer(){
        return "Hello. My name is " + QuestionResponseChecker.ANYTHING + ".";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        String acceptableAnswer1 = "Hello my name is " + QuestionResponseChecker.ANYTHING + ".";
        String acceptableAnswer2 = "Hello, my name is " + QuestionResponseChecker.ANYTHING + ".";
        String acceptableAnswer3 = "My name is " + QuestionResponseChecker.ANYTHING + ".";
        List<String> acceptableAnswers = new ArrayList<>(3);
        acceptableAnswers.add(acceptableAnswer1);
        acceptableAnswers.add(acceptableAnswer2);
        acceptableAnswers.add(acceptableAnswer3);
        return acceptableAnswers;

    }

    private QuestionData createInstructionQuestion(){
        String question = this.instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        List<String> acceptableAnswers = instructionQuestionAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Instructions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        return data;
    }
}