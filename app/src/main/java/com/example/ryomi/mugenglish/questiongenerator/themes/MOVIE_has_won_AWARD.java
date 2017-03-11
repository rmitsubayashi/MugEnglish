package com.example.ryomi.mugenglish.questiongenerator.themes;

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
import java.util.List;

public class MOVIE_has_won_AWARD extends Theme{
    //placeholders
    private final String movieNamePH = "movieName";
    private final String movieNameForeignPH = "movieNameForeign";
    private final String movieNameENPH = "movieNameEN";
    private final String awardENPH = "awardEN";
    private final String awardForeignPH = "awardForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String movieID;
        private String movieNameEN;
        private String movieNameForeign;
        private String awardEN;
        private String awardForeign;

        private QueryResult(
                String movieID,
                String movieNameEN,
                String movieNameForeign,
                String awardEN,
                String awardForeign)
        {
            this.movieID = movieID;
            this.movieNameEN = movieNameEN;
            this.movieNameForeign = movieNameForeign;
            this.awardEN = awardEN;
            this.awardForeign = awardForeign;
        }
    }

    public MOVIE_has_won_AWARD(WikiBaseEndpointConnector connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 3;
    }

    @Override
    protected String getSPARQLQuery(){
        //find movie with award
        return "SELECT ?" + movieNamePH + " ?" + movieNameForeignPH + " ?" + movieNameENPH +
                " ?" + awardENPH + " ?" + awardForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + movieNamePH + " wdt:P31 wd:Q11424; " + //is a movie
                "                         wdt:P166 ?award . " + //has an award
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + movieNamePH + " rdfs:label ?" + movieNameForeignPH + " . " +
                "                           ?award rdfs:label ?" + awardForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + movieNamePH + " rdfs:label ?" + movieNameENPH + " . " +
                "                           ?award rdfs:label ?" + awardENPH + " . " +
                "                           } . " + //English translation
                "    BIND (wd:%s as ?" + movieNamePH + ") . " +
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
            String movieID = SPARQLDocumentParserHelper.findValueByNodeName(head, movieNamePH);
            movieID = QGUtils.stripWikidataID(movieID);
            String movieNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, movieNameENPH);
            String movieNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, movieNameForeignPH);
            String awardEN = SPARQLDocumentParserHelper.findValueByNodeName(head, awardENPH);
            String awardForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, awardForeignPH);

            QueryResult qr = new QueryResult(movieID, movieNameEN, movieNameForeign, awardEN, awardForeign);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.movieNameForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.movieID));
        }

    }

    private String MOVIE_has_won_AWARD_EN_correct(QueryResult qr){
        String awardName = qr.awardEN;
        String startsWithThe = qr.awardEN.substring(0,4);
        if (!startsWithThe.equals("The ") && !startsWithThe.equals("the ") )
            awardName = "the " + awardName;

        String sentence = qr.movieNameEN + " has won " + awardName + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String award_wrote_movie_Foreign_correct(QueryResult qr) {
        return qr.movieNameForeign + "は" + qr.awardForeign + "を受賞したことがあります。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        String awardName = qr.awardEN;
        String startsWithThe = qr.awardEN.substring(0,4);
        if (!startsWithThe.equals("The ") && !startsWithThe.equals("the ") )
            awardName = "the " + awardName;

        List<String> pieces = new ArrayList<>();
        pieces.add(qr.movieNameEN);
        pieces.add("has");
        pieces.add("won");
        pieces.add(awardName);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr){
        String question = this.award_wrote_movie_Foreign_correct(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.movieNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String awardName = qr.awardEN;
        String startsWithThe = qr.awardEN.substring(0,4);
        if (!startsWithThe.equals("The ") && !startsWithThe.equals("the ") )
            awardName = "the " + awardName;
        String sentence1 = award_wrote_movie_Foreign_correct(qr) + "\n";
        String sentence2 = qr.movieNameEN + " " + QuestionUtils.FILL_IN_BLANK_TEXT +
                " " + awardName + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return "has won";
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.movieNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }
}
