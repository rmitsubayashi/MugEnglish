package com.example.ryomi.mugenglish.questiongenerator.themes;

import com.example.ryomi.mugenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY extends Theme {
    //placeholders
    private final String countryNamePH = "countryPH";
    private final String countryNameENPH = "countryENPH";
    private final String countryNameForeignPH = "countryNameForeignPH";
    private final String phoneNumberPH = "phoneNumberPH";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String countryID;
        private String countryNameEN;
        private String countryNameForeign;
        private List<String> phoneNumbers;

        private QueryResult(
                String countryID,
                String countryNameEN,
                String countryNameForeign,
                List<String> phoneNumbers
                )
        {
            this.countryID = countryID;
            this.countryNameEN = countryNameEN;
            this.countryNameForeign = countryNameForeign;
            this.phoneNumbers = phoneNumbers;
        }
    }

    public I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY(EndpointConnectorReturnsXML connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    @Override
    protected String getSPARQLQuery(){
        //find city name
        return "SELECT ?" + countryNamePH + " ?" + countryNameENPH + " ?" + countryNameForeignPH +
                " ?" + phoneNumberPH + "Label " +
                "WHERE " +
                "{" +
                "    ?" + countryNamePH + " wdt:P31 wd:Q6256 . " + //is a city or subclass of city (ie 'city of Japan')
                "    ?" + countryNamePH + " wdt:P2852 ?" + phoneNumberPH + " . " + //located in territory
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + countryNamePH + " rdfs:label ?" + countryNameForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + countryNamePH + " rdfs:label ?" + countryNameENPH + " } . " + //English translation
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //everything else is in English
                "    BIND (wd:%s as ?" + countryNamePH + ") . " + //binding the ID of entity as ?city
                "} ";

    }

    @Override
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        if (resultLength != 0) {
            Node firstHead = allResults.item(0);
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(firstHead, countryNamePH);
            countryID = QGUtils.stripWikidataID(countryID);
            String countryNameEN = SPARQLDocumentParserHelper.findValueByNodeName(firstHead, countryNameENPH);
            String countryNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(firstHead, countryNameForeignPH);
            List<String> phoneNumbers = new ArrayList<>(resultLength);
            for (int i = 0; i < resultLength; i++) {
                Node head = allResults.item(i);
                
                String phoneNumber = SPARQLDocumentParserHelper.findValueByNodeName(head, phoneNumberPH+"Label");
                phoneNumbers.add(phoneNumber);

            }

            QueryResult qr = new QueryResult(countryID, countryNameEN, countryNameForeign, phoneNumbers);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){
        return queryResults.size();
    }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.countryNameForeign);
        }
    }


    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();

            QuestionData fillInBlankMCQuestion = createFillInBlankMCQuestion(qr);
            questionSet.add(fillInBlankMCQuestion);

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.countryID));
        }

    }

    private String I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY_Foreign(QueryResult qr){
        String firstPhoneNumber = qr.phoneNumbers.get(0);
        return qr.countryNameForeign + "での緊急時には" + firstPhoneNumber + "にかけるべきだ。";
    }

    private String fillInBlankMCQuestion(QueryResult qr){
        String sentence1 = I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY_Foreign(qr) + "\n";
        String firstPhoneNumber = qr.phoneNumbers.get(0);
        String sentence2 = "I " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE + " call " +
                firstPhoneNumber + " in an emergency in " +
                GrammarRules.definiteArticleBeforeCountry(qr.countryNameEN) + ".";
        return sentence1 + sentence2;
    }

    private List<String> fillInBlankMCChoices(){
        //leaving out 'must', 'have to' because they are too similar
        List<String> otherHelpingVerbs = new ArrayList<>(4);
        otherHelpingVerbs.add("would");
        otherHelpingVerbs.add("could");
        otherHelpingVerbs.add("can");
        otherHelpingVerbs.add("might");

        Collections.shuffle(otherHelpingVerbs);
        return otherHelpingVerbs.subList(0,3);

    }

    private String fillInBlankMCAnswer(){
        return "should";
    }

    private String The_emergency_phone_number_of_COUNTRY_is(QueryResult qr){

        String series = GrammarRules.commasInASeries(qr.phoneNumbers, "and");
        String sentence1;
        String countryName = GrammarRules.definiteArticleBeforeCountry(qr.countryNameEN);
        //separate single and plural sentences
        if (qr.phoneNumbers.size() == 1) {
            return "The emergency phone number of " + countryName + " is " + series + ".\n";
        } else {
            return "The emergency phone numbers of " + countryName + " are " + series + ".\n";
        }
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = The_emergency_phone_number_of_COUNTRY_is(qr);
        String sentence2 = "I should call " + QuestionUtils.FILL_IN_BLANK_NUMBER +
                " in an emergency in " +
                GrammarRules.definiteArticleBeforeCountry(qr.countryNameEN) + ".";

        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(QueryResult qr){
        return qr.phoneNumbers.get(0);
    }

    private List<String> fillInBlankAlternateAnswers(QueryResult qr){
        List<String> phoneNumbersCopy = new ArrayList<>(qr.phoneNumbers);
        phoneNumbersCopy.remove(0);
        return phoneNumbersCopy;
    }

    private QuestionData createFillInBlankMCQuestion(QueryResult qr){
        String question = this.fillInBlankMCQuestion(qr);
        String answer = fillInBlankMCAnswer();
        List<String> choices = fillInBlankMCChoices();
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.countryNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer(qr);
        List<String> acceptableAnswers = fillInBlankAlternateAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.countryNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

        return data;
    }
}
