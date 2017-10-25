package com.linnca.pelicann.lessongenerator.lessons;


import android.util.Log;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



public class COUNTRY_possessive_area_is_AREA extends Lesson {
    public static final String KEY = "COUNTRY_possessive_area_is_AREA";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String countryID;
        private final String countryNameEN;
        private final String countryNameJP;
        //highest area is 1.4 bil (ints can go up to 2~ bil)
        private final int area;

        private QueryResult(
                String countryID,
                String countryNameEN,
                String countryNameJP,
                int area
        ) {

            this.countryID = countryID;
            this.countryNameEN = countryNameEN;
            this.countryNameJP = countryNameJP;
            this.area = area;
        }
    }



    public COUNTRY_possessive_area_is_AREA(WikiBaseEndpointConnector connector, LessonListener listener){

        super(connector, listener);
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PLACE;
        super.questionSetsLeftToPopulate = 3;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?country ?countryEN ?countryLabel " +
                " ?area " +
                "WHERE " +
                "{" +
                "    ?country wdt:P31 wd:Q6256 . " + //is a country
                "    ?country wdt:P2046 ?area . " + //has area
                "    ?country rdfs:label ?countryEN . " +
                "    FILTER (LANG(?countryEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //JP label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?country) . " + //binding the ID of entity as ?country

                "} ";



    }



    @Override

    protected void processResultsIntoClassWrappers(Document document) {

        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );

        int resultLength = allResults.getLength();

        //there may be more than one emergency phone number for a country
        //ie 110 & 119
        for (int i=0; i<resultLength; i++) {
            Node head = allResults.item(i);
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");

            countryID = LessonGeneratorUtils.stripWikidataID(countryID);
            String countryNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryEN");
            String countryNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            String areaString = SPARQLDocumentParserHelper.findValueByNodeName(head, "area");
            //since most of the area has decimal, convert it to a double first
            int area = (int)Double.parseDouble(areaString);
            QueryResult qr = new QueryResult(countryID, countryNameEN, countryNameJP, area);

            queryResults.add(qr);

        }

    }



    @Override

    protected int getQueryResultCt(){
        return queryResults.size();
    }

    protected void createQuestionsFromResults(){

        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> translateWordQuestion = createTranslateWordQuestion(qr);
            questionSet.add(translateWordQuestion);

            List<QuestionData> fillInBlankInput1Question = createFillInBlankInputQuestion1(qr);
            questionSet.add(fillInBlankInput1Question);

            List<QuestionData> fillInBlankInput2Question = createFillInBlankInputQuestion2(qr);
            questionSet.add(fillInBlankInput2Question);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.countryID, qr.countryNameJP));
        }



    }

    private FeedbackPair fillInBlankInputFeedback(QueryResult qr){
        String lowercaseCountry = qr.countryNameEN.toLowerCase();
        List<String> responses = new ArrayList<>();
        responses.add(lowercaseCountry);
        String feedback = "国の名前は大文字で始まります。\n" + lowercaseCountry + " → " + qr.countryNameEN;
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createTranslateWordQuestion(QueryResult qr){
        String question = qr.countryNameJP;
        String answer = qr.countryNameEN;
        FeedbackPair feedbackPair = fillInBlankInputFeedback(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>();
        feedbackPairs.add(feedbackPair);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryNameJP);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);
        data.setFeedback(feedbackPairs);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankInputQuestion1(QueryResult qr){
        String sentence1 = qr.countryNameJP + "の面積は" + Integer.toString(qr.area) + " km²です。";

        String sentence2 = qr.countryNameEN + "'s " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " is " + LessonGeneratorUtils.convertIntToStringWithCommas(qr.area) + " km².";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankInputAnswer1(){
        return "area";
    }

    private List<QuestionData> createFillInBlankInputQuestion1(QueryResult qr){
        String question = this.fillInBlankInputQuestion1(qr);

        String answer = fillInBlankInputAnswer1();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);
        data.setFeedback(null);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;

    }

    private String fillInBlankInputQuestion2(QueryResult qr){
        String sentence1 = qr.countryNameEN + "'s area is " +
                LessonGeneratorUtils.convertIntToWord(qr.area) + " km².";
        String sentence2 = qr.countryNameJP + "の面積は" + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER + " km²です。";
        return sentence1 + "\n\n" + sentence2;
    }



    private String fillInBlankInputAnswer2(QueryResult qr){
        return Integer.toString(qr.area);
    }

    private List<QuestionData> createFillInBlankInputQuestion2(QueryResult qr){
        String question = this.fillInBlankInputQuestion2(qr);
        String answer = fillInBlankInputAnswer2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);
        data.setFeedback(null);

        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;
    }

    private List<QuestionData> spellingQuestionGeneric(){
        String question = "面積";

        String answer = "area";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);
        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<String> questionIDs = new ArrayList<>();
        questionIDs.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, 1));
        List<List<String>> questionSets = new ArrayList<>();
        questionSets.add(questionIDs);
        return questionSets;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = spellingQuestionGeneric();
        String id1 = LessonGeneratorUtils.formatGenericQuestionID(KEY, 1);
        questions.get(0).setId(id1);
        return questions;

    }

}