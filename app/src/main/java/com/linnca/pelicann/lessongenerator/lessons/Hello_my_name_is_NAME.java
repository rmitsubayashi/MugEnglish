package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.vocabulary.VocabularyWord;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class Hello_my_name_is_NAME extends Lesson {
    public static final String KEY = "Hello_my_name_is_NAME";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personNameEN;
        private final String personNameJP;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameJP)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameJP = personNameJP;
        }
    }

    public Hello_my_name_is_NAME(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?personName ?personNameLabel ?personNameEN " +
                "WHERE " +
                "{" +
                "    {?personName wdt:P31 wd:Q5} UNION " + //is human
                "    {?personName wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?personName rdfs:label ?personNameEN . " +
                "    FILTER (LANG(?personNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?personName) . " + //binding the ID of entity as ?person
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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "personName");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameEN");
            String personNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameLabel");

            QueryResult qr = new QueryResult(personID, personNameEN, personNameJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> chatQuestion = createChatQuestion(qr);
            questionSet.add(chatQuestion);
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);
            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personNameJP, vocabularyWords));
        }

    }

    private String formatSentenceEN(QueryResult qr){
        return "Hello, my name is " + qr.personNameEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return "こんにちは、私の名前は" + qr.personNameJP + "です。";
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord hello = new VocabularyWord("","hello", "こんにちは",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord my = new VocabularyWord("","my", "私の",
                "My name is " + qr.personNameEN, "私の名前は" + qr.personNameJP + "です。", KEY);
        VocabularyWord name = new VocabularyWord("", "name","名前",
                "My name is " + qr.personNameEN, "私の名前は" + qr.personNameJP + "です。", KEY);
        VocabularyWord is = new VocabularyWord("", "is","~は",
                "My name is " + qr.personNameEN, "私の名前は" + qr.personNameJP + "です。", KEY);
        List<VocabularyWord> words = new ArrayList<>(4);
        words.add(hello);
        words.add(my);
        words.add(name);
        words.add(is);
        return words;
    }

    private List<QuestionData> createChatQuestion(QueryResult qr){
        String from = qr.personNameJP;
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
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.CHAT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add("hello");
        pieces.add("my name is");
        pieces.add(qr.personNameEN);
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
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<String> questionIDs = new ArrayList<>();
        questionIDs.add(KEY + "_generic1");
        List<String> questionIDs2 = new ArrayList<>();
        questionIDs2.add(KEY + "_generic2");
        List<List<String>> questionSets = new ArrayList<>();
        questionSets.add(questionIDs);
        questionSets.add(questionIDs2);
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

        List<QuestionData> questions = new ArrayList<>(2);
        questions.add(toSave1);
        questions.add(toSave2);
        return questions;

    }

    private QuestionData createSpellingSuggestiveQuestion(){
        String question = "こんにちは";
        String answer = "hello";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
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
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        return data;
    }
}