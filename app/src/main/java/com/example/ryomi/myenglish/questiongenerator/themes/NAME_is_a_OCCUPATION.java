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

public class NAME_is_a_OCCUPATION extends Theme{
    //placeholders
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String occupationENPH = "occupationEN";
    private final String occupationForeignPH = "occupationForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private String occupationEN;
        private String occupationForeign;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameForeign,
                String occupationEN,
                String occupationForeign)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            this.occupationEN = occupationEN;
            this.occupationForeign = occupationForeign;
        }
    }

    public NAME_is_a_OCCUPATION(EndpointConnectorReturnsXML connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
                " ?" + occupationENPH + " ?" + occupationForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
                "    ?" + personNamePH + " wdt:P106 ?occupation . " + //has an occupation
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + " . " +
                "                           ?occupation rdfs:label ?" + occupationForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
                "                           ?occupation rdfs:label ?" + occupationENPH + " . " +
                "                           } . " + //English translation
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
            String occupationEN = SPARQLDocumentParserHelper.findValueByNodeName(head, occupationENPH);
            String occupationForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, occupationForeignPH);

            QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, occupationEN, occupationForeign);
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

    private String NAME_is_OCCUPATION_EN_correct(QueryResult qr){
        String indefiniteArticle = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String sentence = qr.personNameEN + " is " + indefiniteArticle + " " + qr.occupationEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "は" + qr.occupationForeign + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("is");
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN) + " " + qr.occupationEN);
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
