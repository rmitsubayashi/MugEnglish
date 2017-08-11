package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.Lesson;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_around_HEIGHT_tall extends Lesson {
    public static final String KEY = "NAME_is_around_HEIGHT_tall";
    //placeholders
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String heightPH = "height";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private String height;
        private String roundedHeight;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameForeign,
                String height)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            this.height = adjustHeight(height);
            this.roundedHeight = getRoundedHeight();
        }

        //I don't know how to get the unit for height,
        //so estimate based on logical values
        private String adjustHeight(String height){
            Double heightDouble = Double.parseDouble(height);
            //should be in meters
            if (heightDouble < 10){
                //adjust to centimeters
                heightDouble *= 100;
            }
            //should be in millimeters
            if (heightDouble > 1000){
                //adjust to centimeters
                heightDouble /= 10;
            }

            int removeDecimal = heightDouble.intValue();
            return Integer.toString(removeDecimal);
        }

        private String getRoundedHeight(){
            int exactHeight = Integer.parseInt(height);
            int rem = exactHeight % 10;
            int rounded = rem >= 5 ? (exactHeight - rem + 10) : (exactHeight - rem);
            return Integer.toString(rounded);
        }

    }

    public NAME_is_around_HEIGHT_tall(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 5;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name
        return "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
                " ?" + heightPH + " " +
                "WHERE " +
                "{" +
                "    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is a person
                "    ?" + personNamePH + " wdt:P2048 ?" + heightPH + " . " + //height
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + "  } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
                "                           } . " + //English translation
                "    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
            personID = QGUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
            String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
            String height = SPARQLDocumentParserHelper.findValueByNodeName(head, heightPH);

            QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, height);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.personNameForeign);
        }
    }


    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
        }

    }

    private String NAME_is_a_height_EN_correct(QueryResult qr){
        String sentence = qr.personNameEN + " is around " + qr.roundedHeight + "cm tall.";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "の背はおよそ" + qr.height + "センチです。";
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = qr.personNameEN + " is " + qr.height + "cm tall.\n";
        sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
        String sentence2 = qr.personNameEN + " is around " + QuestionUtils.FILL_IN_BLANK_NUMBER + "cm tall.\n";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(QueryResult qr){
        return qr.roundedHeight;
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }
}
