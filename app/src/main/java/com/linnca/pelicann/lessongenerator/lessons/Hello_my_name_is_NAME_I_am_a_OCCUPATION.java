package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.lessongenerator.TermAdjuster;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class Hello_my_name_is_NAME_I_am_a_OCCUPATION extends Lesson{
    public static final String KEY = "Hello_my_name_is_NAME_I_am_a_OCCUPATION";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personNameEN;
        private final String personNameJP;
        private final String firstNameEN;
        private final String occupationEN;
        private final String occupationJP;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameJP,
                String firstNameEN,
                String occupationEN,
                String occupationJP)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameJP = personNameJP;
            this.firstNameEN = firstNameEN;
            this.occupationEN = occupationEN;
            this.occupationJP = occupationJP;
        }
    }

    public Hello_my_name_is_NAME_I_am_a_OCCUPATION(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?personName ?personNameLabel ?personNameEN " +
                " ?occupationEN ?occupationLabel ?firstNameEN " +
                "WHERE " +
                "{" +
                "    {?personName wdt:P31 wd:Q5} UNION " + //is human
                "    {?personName wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?personName wdt:P106 ?occupation . " + //has an occupation
                "    ?personName wdt:P735 ?firstName . " + //has a first name
                "    ?personName rdfs:label ?personNameEN . " +
                "    ?firstName rdfs:label ?firstNameEN . " +
                "    ?occupation rdfs:label ?occupationEN . " +
                "    FILTER (LANG(?personNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?firstNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?occupationEN) = '" +
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
            String firstNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "firstNameEN");
            String occupationEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationEN");
            occupationEN = TermAdjuster.adjustOccupationEN(occupationEN);
            String occupationJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationLabel");

            QueryResult qr = new QueryResult(personID, personNameEN, personNameJP, firstNameEN, occupationEN, occupationJP);
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

            List<QuestionData> chooseCorrectSpellingQuestion = createChooseCorrectSpellingQuestion(qr);
            questionSet.add(chooseCorrectSpellingQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personNameJP, null));
        }

    }

    private String formatSentenceJP(QueryResult qr){
        return "こんにちは、私の名前は" + qr.personNameJP + "です。私は" + qr.occupationJP + "です。";
    }

    private List<QuestionData> createChatQuestion(QueryResult qr){
        String from = qr.personNameJP;
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "Hello my name is " + qr.personNameEN);
        ChatQuestionItem chatItem2 = new ChatQuestionItem(true, "Hello " + qr.firstNameEN);
        ChatQuestionItem chatItem3 = new ChatQuestionItem(false, "I am a ...");
        ChatQuestionItem chatItem4 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(4);
        chatItems.add(chatItem1);
        chatItems.add(chatItem2);
        chatItems.add(chatItem3);
        chatItems.add(chatItem4);
        String question = QuestionUtils.formatChatQuestion(from, chatItems);
        String answer = qr.occupationEN;
        List<String> choices = new ArrayList<>(1);
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
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
        pieces.add("I");
        pieces.add("am");
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN));
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

    private List<QuestionData> createChooseCorrectSpellingQuestion(QueryResult qr){
        String question = qr.occupationJP;
        String answer = qr.occupationEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.CHOOSE_CORRECT_SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}