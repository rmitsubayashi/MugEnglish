package com.linnca.pelicann.questiongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.database2classmappings.QuestionTypeMappings;
import com.linnca.pelicann.db.datawrappers.QuestionData;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;
import com.linnca.pelicann.questiongenerator.GrammarRules;
import com.linnca.pelicann.questiongenerator.Lesson;
import com.linnca.pelicann.questiongenerator.QGUtils;
import com.linnca.pelicann.questiongenerator.QuestionDataWrapper;
import com.linnca.pelicann.questiongenerator.QuestionUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class The_DEMONYM_flag_is_COLORS extends Lesson{
    public static final String KEY = "The_DEMONYM_flag_is_COLORS";
    private List<QueryResult> queryResults = new ArrayList<>();
    private Map<String, List<QueryResult>> queryResultMap = new HashMap<>();
    private class QueryResult {
        private String countryID;
        private String colorEN;
        private String colorJP;
        private String demonymEN;
        private String countryJP;

        private QueryResult(
                String countryID,
                String colorEN,
                String colorJP,
                String countryJP,
                String demonymEN)
        {
            this.countryID = countryID;
            this.colorEN = colorEN;
            this.colorJP = colorJP;
            this.demonymEN = demonymEN;
            this.countryJP = countryJP;
        }
    }

    private List<String> allColors = new ArrayList<>(8);

    public The_DEMONYM_flag_is_COLORS(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PLACE;
        super.lessonKey = KEY;
        populateColors();
    }

    private void populateColors(){
        allColors.add("red");
        allColors.add("white");
        allColors.add("blue");
        allColors.add("green");
        allColors.add("yellow");
        allColors.add("orange");
        allColors.add("black");
        allColors.add("light blue");
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?country ?colorLabel ?colorEN " +
                "?demonymEN ?countryLabel " +
                "WHERE " +
                "{" +
                "    ?flag wdt:P31 wd:Q186516; " + //is a flag
                "          wdt:P1001 ?country; " + //of country
                "          wdt:P462 ?color . " + //colors of flag
                "    ?country wdt:P1549 ?demonymEN . " + //grab country's demonym demonym
                "    ?color rdfs:label ?colorEN . " +
                "    FILTER (LANG(?demonymEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " + //just get the English demonym
                "    FILTER (STR(?demonymEN) != 'United States') . " + //United States is noted as a demonym (can't edit out?)
                "    FILTER (LANG(?colorEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                WikiBaseEndpointConnector.ENGLISH + "' } " +
                "    BIND (wd:%s as ?country) . " + //binding the ID of entity as ?country
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
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");
            countryID = QGUtils.stripWikidataID(countryID);
            String colorEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "colorEN");
            String colorJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "colorLabel");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            String demonymEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "demonymEN");

            QueryResult qr = new QueryResult(countryID, colorEN, colorJP, countryJP, demonymEN);

            //we want all the colors for a country
            if (queryResultMap.containsKey(countryID)){
                List<QueryResult> value = queryResultMap.get(countryID);
                value.add(qr);
            } else {
                //only add this to the results once for each country
                queryResults.add(qr);

                List<QueryResult> list = new ArrayList<>();
                list.add(qr);
                queryResultMap.put(countryID, list);
            }
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.countryID, qr.countryJP));
        }

    }

    //need to store in a private variable so we can access it in the answer
    private String fillInBlankAnswer;
    private String fillInBlankQuestion(QueryResult qr){
        List<QueryResult> colors = queryResultMap.get(qr.countryID);
        List<String> colorStrings = new ArrayList<>(colors.size());
        for (QueryResult color : colors){
            colorStrings.add(color.colorEN);
        }

        int random = new Random().nextInt(colorStrings.size());
        fillInBlankAnswer = colorStrings.get(random);
        colorStrings.set(random,QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE);
        Collections.shuffle(colorStrings);
        String colorSeries = GrammarRules.commasInASeries(colorStrings, "and");

        return "The " + qr.demonymEN + " flag is " + colorSeries + ".";
    }

    private String fillInBlankAnswer(){
        return fillInBlankAnswer;
    }

    private List<String> fillInBlankChoices(QueryResult qr){
        List<QueryResult> colors = queryResultMap.get(qr.countryID);
        List<String> colorStrings = new ArrayList<>(colors.size());
        for (QueryResult color : colors){
            colorStrings.add(color.colorEN);
        }
        List<String> allColorsCopy = new ArrayList<>(allColors);
        allColorsCopy.removeAll(colorStrings);
        Collections.shuffle(allColorsCopy);
        return allColorsCopy.subList(0,2);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        //saved in a temp variable
        String answer = fillInBlankAnswer();
        List<String> choices = fillInBlankChoices(qr);
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankInputAnswer;
    private String fillInBlankInputQuestion(QueryResult qr){
        List<QueryResult> colors = queryResultMap.get(qr.countryID);
        //not really relevant whether we make a copy before shuffling
        Collections.shuffle(colors);
        List<String> colorStringsEN = new ArrayList<>(colors.size());
        for (QueryResult color : colors){
            colorStringsEN.add(color.colorEN);
        }

        int random = new Random().nextInt(colorStringsEN.size());
        fillInBlankInputAnswer = colorStringsEN.get(random);
        colorStringsEN.set(random,QuestionUtils.FILL_IN_BLANK_TEXT);
        String countrySeriesEN = GrammarRules.commasInASeries(colorStringsEN,"and");

        List<String> colorStringsJP = new ArrayList<>(colors.size());
        for (QueryResult color : colors){
            colorStringsJP.add(color.colorJP);
        }

        StringBuilder colorSeriesJPSB = new StringBuilder();
        for (String colorJP : colorStringsJP){
            String toAdd = colorJP + "・";
            colorSeriesJPSB.append(toAdd);
        }
        String colorSeriesJP = colorSeriesJPSB.substring(0, colorSeriesJPSB.length()-1);
        String sentence1 = qr.countryJP + "の旗の色は" + colorSeriesJP + "です。\n\n";
        String sentence2 = "The " + qr.demonymEN + " flag is " + countrySeriesEN + ".";

        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return fillInBlankInputAnswer;
    }

    private List<QuestionData> createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setVocabulary(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }
}