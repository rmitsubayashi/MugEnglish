package com.example.ryomi.mugenglish.questiongenerator.themes;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
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

public class BOOK_was_published_by_PUBLISHER extends Theme{
    //placeholders
    private final String bookNamePH = "bookName";
    private final String bookNameForeignPH = "bookNameForeign";
    private final String bookNameENPH = "bookNameEN";
    private final String publisherENPH = "publisherEN";
    private final String publisherForeignPH = "publisherForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String bookID;
        private String bookNameEN;
        private String bookNameForeign;
        private String publisherEN;
        private String publisherForeign;

        private QueryResult(
                String bookID,
                String bookNameEN,
                String bookNameForeign,
                String publisherEN,
                String publisherForeign)
        {
            this.bookID = bookID;
            this.bookNameEN = bookNameEN;
            this.bookNameForeign = bookNameForeign;
            this.publisherEN = publisherEN;
            this.publisherForeign = publisherForeign;
        }
    }

    public BOOK_was_published_by_PUBLISHER(WikiBaseEndpointConnector connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    @Override
    protected String getSPARQLQuery(){
        //find book with publisher
        return "SELECT ?" + bookNamePH + " ?" + bookNameForeignPH + " ?" + bookNameENPH +
                " ?" + publisherENPH + " ?" + publisherForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + bookNamePH + " wdt:P31 wd:Q571 . " + //is a book
                "    ?" + bookNamePH + " wdt:P123 ?publisher . " + //has a publisher
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + bookNamePH + " rdfs:label ?" + bookNameForeignPH + " . " +
                "                           ?publisher rdfs:label ?" + publisherForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + bookNamePH + " rdfs:label ?" + bookNameENPH + " . " +
                "                           ?publisher rdfs:label ?" + publisherENPH + " . " +
                "                           } . " + //English translation
                "    BIND (wd:%s as ?" + bookNamePH + ") . " +
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
            String bookID = SPARQLDocumentParserHelper.findValueByNodeName(head, bookNamePH);
            bookID = QGUtils.stripWikidataID(bookID);
            String bookNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, bookNameENPH);
            String bookNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, bookNameForeignPH);
            String publisherEN = SPARQLDocumentParserHelper.findValueByNodeName(head, publisherENPH);
            String publisherForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, publisherForeignPH);

            QueryResult qr = new QueryResult(bookID, bookNameEN, bookNameForeign, publisherEN, publisherForeign);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.bookNameForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            QuestionData sentencePuzzleQuestion2 = createSentencePuzzleQuestion2(qr);
            questionSet.add(sentencePuzzleQuestion2);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.bookID));
        }

    }

    private String BOOK_was_published_by_PUBLISHER_EN_correct(QueryResult qr){
        String sentence = qr.bookNameEN + " was published by " + qr.publisherEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String BOOK_was_published_by_PUBLISHER_Foreign_correct(QueryResult qr){
        return qr.bookNameForeign + "は" + qr.publisherForeign + "によって出版されました。";
    }

    private String PUBLISHER_published_BOOK_EN_correct(QueryResult qr){
        String sentence = qr.publisherEN + " published " + qr.bookNameEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String PUBLISHER_published_BOOK_Foreign_correct(QueryResult qr) {
        return qr.publisherForeign + "が" + qr.bookNameForeign + "を出版しました。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.publisherEN);
        pieces.add("published");
        pieces.add(qr.bookNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr){
        String question = this.PUBLISHER_published_BOOK_Foreign_correct(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.bookNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces2(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.bookNameEN);
        pieces.add("was published");
        pieces.add("by");
        pieces.add(qr.publisherEN);
        return pieces;
    }

    private String puzzlePiecesAnswer2(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces2(qr));
    }

    private QuestionData createSentencePuzzleQuestion2(QueryResult qr){
        String question = this.BOOK_was_published_by_PUBLISHER_Foreign_correct(qr);
        List<String> choices = this.puzzlePieces2(qr);
        String answer = puzzlePiecesAnswer2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.bookNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
