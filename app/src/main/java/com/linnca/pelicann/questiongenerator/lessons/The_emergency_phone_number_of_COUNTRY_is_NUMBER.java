package com.linnca.pelicann.questiongenerator.lessons;



import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;

import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;

import com.linnca.pelicann.db.database2classmappings.QuestionTypeMappings;

import com.linnca.pelicann.db.datawrappers.FeedbackPair;
import com.linnca.pelicann.db.datawrappers.QuestionData;

import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;
import com.linnca.pelicann.questiongenerator.GrammarRules;

import com.linnca.pelicann.questiongenerator.QGUtils;

import com.linnca.pelicann.questiongenerator.QuestionDataWrapper;

import com.linnca.pelicann.questiongenerator.QuestionUtils;

import com.linnca.pelicann.questiongenerator.Lesson;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.List;



public class The_emergency_phone_number_of_COUNTRY_is_NUMBER extends Lesson {
    public static final String KEY = "The_emergency_phone_number_of_COUNTRY_is_NUMBER";
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



    public The_emergency_phone_number_of_COUNTRY_is_NUMBER(WikiBaseEndpointConnector connector, LessonListener listener){

        super(connector, listener);
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PLACE;
        super.questionSetsLeftToPopulate = 2;
        super.lessonKey = KEY;

    }



    @Override

    protected String getSPARQLQuery(){

        //find city name

        return "SELECT ?countryName ?countryNameEN ?countryNameLabel " +
                " ?phoneNumberLabel " +
                "WHERE " +

                "{" +

                "    ?countryName wdt:P31 wd:Q6256 . " + //is a country
                "    ?countryName wdt:P2852 ?phoneNumber . " + //has a emergency phone number
                "    ?countryName rdfs:label ?countryNameEN . " +
                "    FILTER (LANG(?countryNameEN) = '" +
                    WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?countryName) . " + //binding the ID of entity as ?city

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
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(firstHead, "countryName");

            countryID = QGUtils.stripWikidataID(countryID);

            String countryNameEN = SPARQLDocumentParserHelper.findValueByNodeName(firstHead, "countryNameEN");

            String countryNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(firstHead, "countryNameLabel");

            //there may be more than one emergency phone number for a country
            //ie 110 & 119
            List<String> phoneNumbers = new ArrayList<>(resultLength);
            for (int i = 0; i < resultLength; i++) {
                Node head = allResults.item(i);
                String phoneNumber = SPARQLDocumentParserHelper.findValueByNodeName(head, "phoneNumberLabel");
                //since we may return something like 100番
                //remove all non-digits.
                //we don't need to worry about hyphens because these are all emergency numbers
                phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
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

    protected void createQuestionsFromResults(){

        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> spellingSuggestiveQuestion = spellingSuggestiveQuestion(qr);
            questionSet.add(spellingSuggestiveQuestion);

            List<QuestionData> translateWordQuestion = createTranslateWordQuestion(qr);
            questionSet.add(translateWordQuestion);

            List<QuestionData> fillInBlankInput1Question = createFillInBlankInputQuestion1(qr);
            questionSet.add(fillInBlankInput1Question);

            List<QuestionData> fillInBlankInput2Question = createFillInBlankInputQuestion2(qr);
            questionSet.add(fillInBlankInput2Question);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.countryID, qr.countryNameForeign));
        }



    }

    private List<QuestionData> spellingSuggestiveQuestion(QueryResult qr){
        String question = qr.countryNameForeign;

        String answer = qr.countryNameEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryNameForeign);
        data.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
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

    private FeedbackPair fillInBlankInputFeedback(QueryResult qr){
        String lowercaseCountry = qr.countryNameEN.toLowerCase();
        List<String> responses = new ArrayList<>();
        responses.add(lowercaseCountry);
        String feedback = "国の名前は大文字で始まります。\n" + lowercaseCountry + " → " + qr.countryNameEN;
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createTranslateWordQuestion(QueryResult qr){
        String question = qr.countryNameForeign;
        String answer = qr.countryNameEN;
        FeedbackPair feedbackPair = fillInBlankInputFeedback(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>();
        feedbackPairs.add(feedbackPair);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryNameForeign);
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

    private String fillInBlankInput1Question(QueryResult qr, int phoneNumberIndex){
        String phoneNumber = qr.phoneNumbers.get(phoneNumberIndex);
        String supportingSentence = "英単語を記入してください";
        String sentence1 = qr.countryNameForeign + "の緊急通報用電話番号は" + phoneNumber + "です。";
        String sentence2 = "The emergency phone number of " + qr.countryNameEN + " is " + QuestionUtils.FILL_IN_BLANK_TEXT + ".";
        return supportingSentence + "\n\n" + sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankInput1Answer(QueryResult qr, int phoneNumberIndex){
        String phoneNumber = qr.phoneNumbers.get(phoneNumberIndex);
        List<String> phoneNumberWords = QGUtils.convertPhoneNumberToPhoneNumberWords(phoneNumber);
        return phoneNumberWords.get(0);
    }

    private List<String> fillInBlankInput1AcceptableAnswers(QueryResult qr, int phoneNumberIndex){
        String phoneNumber = qr.phoneNumbers.get(phoneNumberIndex);
        List<String> phoneNumberWords = QGUtils.convertPhoneNumberToPhoneNumberWords(phoneNumber);
        //remove the actual answer
        phoneNumberWords.remove(0);
        return phoneNumberWords;
    }

    private FeedbackPair fillInBlankInput1Feedback(QueryResult qr, int phoneNumberIndex){
        List<String> responses = new ArrayList<>();
        String phoneNumberNumber = qr.phoneNumbers.get(phoneNumberIndex);
        //if the user types nine hundred eleven for 911
        String numberWords = QGUtils.convertIntToWord(phoneNumberNumber);
        responses.add(numberWords);
        List<String> phoneNumberWords = QGUtils.convertPhoneNumberToPhoneNumberWords(phoneNumberNumber);
        String feedback = "電話番号は一桁ずつ分けて言います。つまり、 " + numberWords +
                " ではなく " + phoneNumberWords.get(0) + " が正解です。";

        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private FeedbackPair fillInBlankInput1Feedback2(QueryResult qr, int phoneNumberIndex){
        //even if the user makes a correct choice,
        //let him know if there are other options available
        List<String> responses = new ArrayList<>();
        String phoneNumber = qr.phoneNumbers.get(phoneNumberIndex);
        List<String> phoneNumberWords = QGUtils.convertPhoneNumberToPhoneNumberWords(phoneNumber);
        if (phoneNumberWords.size() == 1){
            return null;
        }
        String feedback = "正解は複数あります。\n";
        for (String phoneNumberWord : phoneNumberWords){
            responses.add(phoneNumberWord);
            //cleaner alignment
            feedback += "  " + phoneNumberWord + "\n";
        }
        feedback += "詳しい解説は説明文に書いてあります。";


        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private List<QuestionData> createFillInBlankInputQuestion1(QueryResult qr){
        int phoneNumbersSize = qr.phoneNumbers.size();
        List<QuestionData> questionDataList = new ArrayList<>(phoneNumbersSize);

        for (int phoneNumberIndex=0; phoneNumberIndex<phoneNumbersSize; phoneNumberIndex++) {
            String question = this.fillInBlankInput1Question(qr, phoneNumberIndex);

            String answer = fillInBlankInput1Answer(qr, phoneNumberIndex);
            List<String> acceptableAnswers = fillInBlankInput1AcceptableAnswers(qr, phoneNumberIndex);
            FeedbackPair feedbackPair = fillInBlankInput1Feedback(qr, phoneNumberIndex);
            FeedbackPair feedbackPair2 = fillInBlankInput1Feedback2(qr, phoneNumberIndex);
            List<FeedbackPair> feedbackPairs = new ArrayList<>();
            feedbackPairs.add(feedbackPair);
            feedbackPairs.add(feedbackPair2);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(super.lessonKey);
            data.setTopic(qr.countryNameForeign);
            data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
            data.setQuestion(question);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);
            data.setVocabulary(null);
            data.setFeedback(feedbackPairs);
            questionDataList.add(data);
        }
        return questionDataList;

    }

    private String fillInBlankInputQuestion2(QueryResult qr, int phoneNumberIndex){
        String phoneNumber = qr.phoneNumbers.get(phoneNumberIndex);
        List<String> phoneNumberWords = QGUtils.convertPhoneNumberToPhoneNumberWords(phoneNumber);
        String phoneNumberWord = phoneNumberWords.get(0);
        String countryName = GrammarRules.definiteArticleBeforeCountry(qr.countryNameEN);

        String sentence1 = "The emergency phone number of " + countryName + " is " + phoneNumberWord + ".";

        String sentence2 = qr.countryNameForeign + "の緊急通報用電話番号は" + QuestionUtils.FILL_IN_BLANK_NUMBER +
                "です。";
        return sentence1 + "\n\n" + sentence2;

    }



    private String fillInBlankInputAnswer2(QueryResult qr, int phoneNumberIndex){
        //no need to worry about non-digits (we removed all of them)
        return qr.phoneNumbers.get(phoneNumberIndex);
    }

    private List<QuestionData> createFillInBlankInputQuestion2(QueryResult qr){
        int phoneNumbersSize = qr.phoneNumbers.size();
        List<QuestionData> questionDataList = new ArrayList<>(phoneNumbersSize);

        for (int phoneNumberIndex=0; phoneNumberIndex<phoneNumbersSize; phoneNumberIndex++) {
            String question = this.fillInBlankInputQuestion2(qr, phoneNumberIndex);

            String answer = fillInBlankInputAnswer2(qr, phoneNumberIndex);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(super.lessonKey);
            data.setTopic(qr.countryNameForeign);
            data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
            data.setQuestion(question);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setVocabulary(null);
            data.setFeedback(null);
            questionDataList.add(data);
        }
        return questionDataList;

    }

}