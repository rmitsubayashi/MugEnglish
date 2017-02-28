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
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CITY_is_in_TERRITORY extends Theme{
    //placeholders
    private final String cityNamePH = "cityName";
    private final String cityNameForeignPH = "cityNameForeign";
    private final String cityNameENPH = "cityNameEN";
    private final String territoryENPH = "territoryEN";
    private final String territoryForeignPH = "territoryForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String cityID;
        private String cityNameEN;
        private String cityNameForeign;
        private String territoryEN;
        private String territoryForeign;

        private QueryResult(
                String cityID,
                String cityNameEN,
                String cityNameForeign,
                String territoryEN,
                String territoryForeign)
        {
            this.cityID = cityID;
            this.cityNameEN = cityNameEN;
            this.cityNameForeign = cityNameForeign;
            this.territoryEN = territoryEN;
            this.territoryForeign = territoryForeign;
        }
    }

    public CITY_is_in_TERRITORY(EndpointConnectorReturnsXML connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    protected String getSPARQLQuery(){
        //find city name
        return "SELECT DISTINCT ?" + cityNamePH + " ?" + cityNameForeignPH + " ?" + cityNameENPH + //Kyoto returns 2 results? so use distinct
                " ?" + territoryENPH + " ?" + territoryForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + cityNamePH + " wdt:P31/wdt:P279* wd:Q515 . " + //is a city or subclass of city (ie 'city of Japan')
                "    ?" + cityNamePH + " wdt:P131 ?territory . " + //located in territory
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + cityNamePH + " rdfs:label ?" + cityNameForeignPH + " . " +
                "                           ?territory rdfs:label ?" + territoryForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + cityNamePH + " rdfs:label ?" + cityNameENPH + " . " +
                "                           ?territory rdfs:label ?" + territoryENPH + " . " +
                "                           } . " + //English translation
                "    BIND (wd:%s as ?" + cityNamePH + ") . " + //binding the ID of entity as ?city
                "} ";

    }

    protected void processResultsIntoClassWrappers() {
        Document document = super.documentOfTopics;
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String cityID = SPARQLDocumentParserHelper.findValueByNodeName(head, cityNamePH);
            cityID = QGUtils.stripWikidataID(cityID);
            String cityNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, cityNameENPH);
            String cityNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, cityNameForeignPH);
            String territoryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, territoryENPH);
            String territoryForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, territoryForeignPH);

            QueryResult qr = new QueryResult(cityID, cityNameEN, cityNameForeign, territoryEN, territoryForeign);
            queryResults.add(qr);
        }
    }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.cityNameForeign);
        }
    }


    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            QuestionData trueFalseQuestionTrue = createTrueFalseQuestion(qr, QuestionUtils.TRUE_FALSE_QUESTION_TRUE);
            QuestionData trueFalseQuestionFalse = createTrueFalseQuestion(qr, QuestionUtils.TRUE_FALSE_QUESTION_FALSE);
            int i = new Random().nextInt();
            if (i%2 == 0) {
                questionSet.add(trueFalseQuestionTrue);
                questionSet.add(trueFalseQuestionFalse);
            } else {
                questionSet.add(trueFalseQuestionFalse);
                questionSet.add(trueFalseQuestionTrue);
            }

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.cityID));
        }

    }

    private String CITY_is_in_TERRITORY_EN_correct(QueryResult qr){
        String sentence = qr.cityNameEN + " is in " + qr.territoryEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.cityNameForeign + "は" + qr.territoryForeign + "の中にあります。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.cityNameEN);
        pieces.add("is");
        pieces.add("in");
        pieces.add(qr.territoryEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceForeign(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.cityNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.cityNameEN + " is " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.territoryEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private List<String> fillInBlankChoices(){
        List<String> prepositions = new ArrayList<>();
        prepositions.add("on");
        prepositions.add("at");
        prepositions.add("from");
        prepositions.add("around");
        prepositions.add("by");
        prepositions.add("for");
        Collections.shuffle(prepositions);

        List<String> choices = new ArrayList<>();
        choices.add(prepositions.get(0));
        choices.add(prepositions.get(1));

        return choices;
    }

    private String fillInBlankAnswer(){
        return "in";
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<String> choices = fillInBlankChoices();
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.cityNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence = qr.cityNameEN + " is " + QuestionUtils.FILL_IN_BLANK_TEXT +
                " " + qr.territoryEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankInputAnswer(){
        return "in";
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.cityNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private String CITY_is_in_TERRITORY_EN_incorrect(QueryResult qr){
        String sentence = qr.territoryEN + " is in " + qr.cityNameEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private QuestionData createTrueFalseQuestion(QueryResult qr, String answer){
        String question;
        if (answer.equals(QuestionUtils.TRUE_FALSE_QUESTION_TRUE))
            question = CITY_is_in_TERRITORY_EN_correct(qr);
        else
            question = CITY_is_in_TERRITORY_EN_incorrect(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.cityNameForeign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
