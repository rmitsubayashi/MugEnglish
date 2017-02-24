package com.example.ryomi.myenglish.questiongenerator.themes;

import com.example.ryomi.myenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.questiongenerator.GrammarRules;
import com.example.ryomi.myenglish.questiongenerator.QGUtils;
import com.example.ryomi.myenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.myenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NAME_is_DEMONYM extends Theme{
    //placeholders
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    //since there aren't that many Japanese demonyms available,
    //just get the country name and convert it to a demonym by adding "~人"
    private final String countryForeignPH = "countryForeign";
    private final String demonymENPH = "demonymEN";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private String demonymEN;
        private String demonymForeign;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameForeign,
                String countryForeign,
                String demonymEN)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            this.demonymEN = demonymEN;
            this.demonymForeign = convertCountryToDemonym(countryForeign);
        }

        private String convertCountryToDemonym(String country){
            //first check to make sure it's Japanese
            Pattern p = Pattern.compile("[a-zA-Z]");
            Matcher m = p.matcher(country);

            //if it's written in English, return the demonym for English
            //(the demonym in English is instantiated already)
            if(m.find()){
                return demonymEN;
            } else {
                boolean katakanaStarted = false;
                for (int i=0; i<country.length(); i++){
                    char c = country.charAt(i);
                    if (isKatakana(c)){
                        katakanaStarted = true;
                    } else {
                        if (katakanaStarted){
                            country = country.substring(0,i);
                            break;
                        }
                    }
                }

                return country + "人";
            }
        }

        private boolean isKatakana(char c){
            return (c >= 'ァ' && c <= 'ヿ');
        }
    }

    public NAME_is_DEMONYM(EndpointConnectorReturnsXML connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    protected String getSPARQLQuery(){
        return "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
                " ?" + demonymENPH + " ?" + countryForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
                "    ?" + personNamePH + " wdt:P27 ?country . " + //has a country of citizenship
                "    ?country wdt:P1549 ?" + demonymENPH + " . " + //and the country has a demonym
                "    FILTER (LANG(?" + demonymENPH + ") = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " + //just get the English demonym
                "    FILTER (STR(?" + demonymENPH + ") != 'United States') . " + //United States is noted as a demonym (can't edit out?)
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + " . " +
                "                           ?country rdfs:label ?" + countryForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + "} . " + //English translation
                "    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
            personID = QGUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
            String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
            String countryForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, countryForeignPH);
            String demonymEN = SPARQLDocumentParserHelper.findValueByNodeName(head, demonymENPH);

            QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, countryForeign, demonymEN);
            queryResults.add(qr);
        }
    }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.personNameForeign);
        }
    }


    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
        }

    }

    private String NAME_is_DEMONYM_EN_correct(QueryResult qr){
        String sentence = qr.personNameEN + " is " + qr.demonymEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "は" + qr.demonymForeign + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("is");
        pieces.add(qr.demonymEN);
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
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
