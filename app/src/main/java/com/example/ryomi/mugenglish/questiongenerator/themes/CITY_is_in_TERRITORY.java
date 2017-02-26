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
import java.util.List;

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

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.cityID));
        }

    }

    private String NAME_is_in_territory_EN_correct(QueryResult qr){
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
}
