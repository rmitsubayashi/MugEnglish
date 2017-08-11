package com.example.ryomi.mugenglish.questiongenerator.lessons;

import android.util.Log;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class The_DEMONYM_flag_is_COLORS extends Lesson {
    public static final String KEY = "The_DEMONYM_flag_is_COLORS";
    //placeholders
    private final String countryPH = "country";
    private final String colorForeignPH = "colorForeign";
    private final String colorENPH = "colorEN";
    //since there aren't that many Japanese demonyms available,
    //just get the country name and convert it to a demonym by adding "~人"
    private final String countryForeignPH = "countryForeign";
    private final String demonymENPH = "demonymEN";

    private List<QueryResult> queryResults = new ArrayList<>();
    private Map<String, List<QueryResult>> queryResultMap = new HashMap<>();
    private class QueryResult {
        private String countryID;
        private String colorEN;
        private String colorForeign;
        private String demonymEN;
        private String countryForeign;

        private QueryResult(
                String countryID,
                String colorEN,
                String colorForeign,
                String countryForeign,
                String demonymEN)
        {
            this.countryID = countryID;
            this.colorEN = colorEN;
            this.colorForeign = colorForeign;
            this.demonymEN = demonymEN;
            this.countryForeign = countryForeign;
        }
    }

    private List<String> allColors = new ArrayList<>(8);

    public The_DEMONYM_flag_is_COLORS(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;
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
        return "SELECT ?" + countryPH + " ?" + colorForeignPH + " ?" + colorENPH +
                " ?" + demonymENPH + " ?" + countryForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?flag wdt:P31 wd:Q186516; " + //is a flag
                "          wdt:P1001 ?" + countryPH + "; " + //of country
                "          wdt:P462 ?color . " + //colors of flag
                "    ?country wdt:P1549 ?" + demonymENPH + " . " + //grab country's demonym demonym
                "    FILTER (LANG(?" + demonymENPH + ") = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " + //just get the English demonym
                "    FILTER (STR(?" + demonymENPH + ") != 'United States') . " + //United States is noted as a demonym (can't edit out?)
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?color rdfs:label ?" + colorForeignPH + " . " +
                "                           ?country rdfs:label ?" + countryForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?color rdfs:label ?" + colorENPH + "} . " + //English translation
                "    BIND (wd:%s as ?" + countryPH + ") . " + //binding the ID of entity as ?person
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
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, countryPH);
            countryID = QGUtils.stripWikidataID(countryID);
            String colorEN = SPARQLDocumentParserHelper.findValueByNodeName(head, colorENPH);
            String colorForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, colorForeignPH);
            String countryForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, countryForeignPH);
            String demonymEN = SPARQLDocumentParserHelper.findValueByNodeName(head, demonymENPH);

            QueryResult qr = new QueryResult(countryID, colorEN, colorForeign, countryForeign, demonymEN);
            queryResults.add(qr);

            //we want all the colors for a country
            if (queryResultMap.containsKey(countryID)){
                List<QueryResult> value = queryResultMap.get(countryID);
                value.add(qr);
            } else {
                List<QueryResult> list = new ArrayList<>();
                list.add(qr);
                queryResultMap.put(countryID, list);
            }
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.countryForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.countryID));
        }

    }

    //need to store in a private variable so we can access it in the answer
    private String fillInBlankAnswer;
    private String fillInBlankQuestion(QueryResult qr){
        List<QueryResult> colors = queryResultMap.get(qr.countryID);
        List<String> colorStrings = new ArrayList<>(colors.size());
        for (QueryResult color : colors){
            colorStrings.add(color.colorEN);
            Log.d(color.countryForeign, color.colorEN);
        }

        int random = new Random().nextInt(colorStrings.size());
        fillInBlankAnswer = colorStrings.get(random);
        colorStrings.set(random,QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE);
        Collections.shuffle(colorStrings);
        for (String col : colorStrings){
            Log.d(getClass().getCanonicalName(), col);
        }
        System.out.println();
        String colorSeries = GrammarRules.commasInASeries(colorStrings, "and");

        Log.d(getClass().getCanonicalName(),colorSeries);
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

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        //saved in a temp variable
        String answer = fillInBlankAnswer();
        List<String> choices = fillInBlankChoices(qr);
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.countryForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
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

        List<String> colorStringsForeign = new ArrayList<>(colors.size());
        for (QueryResult color : colors){
            colorStringsForeign.add(color.colorForeign);
        }

        StringBuilder colorSeriesForeignSB = new StringBuilder();
        for (String colorForeign : colorStringsForeign){
            String toAdd = colorForeign + "・";
            colorSeriesForeignSB.append(toAdd);
        }
        String colorSeriesForeign = colorSeriesForeignSB.substring(0, colorSeriesForeignSB.length()-1);
        String sentence1 = qr.countryForeign + "の旗の色は" + colorSeriesForeign + "です。\n\n";
        String sentence2 = "The " + qr.demonymEN + " flag is " + countrySeriesEN + ".";

        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return fillInBlankInputAnswer;
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.countryForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setVocabulary(null);

        return data;
    }
}
