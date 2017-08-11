package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
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

public class AUTHOR_wrote_BOOK extends Lesson {
    public static final String KEY = "AUTHOR_wrote_BOOK";
    //placeholders
    private final String bookNamePH = "bookName";
    private final String bookNameForeignPH = "bookNameForeign";
    private final String bookNameENPH = "bookNameEN";
    private final String authorENPH = "authorEN";
    private final String authorForeignPH = "authorForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String bookID;
        private String bookNameEN;
        private String bookNameForeign;
        private String authorEN;
        private String authorForeign;

        private QueryResult(
                String bookID,
                String bookNameEN,
                String bookNameForeign,
                String authorEN,
                String authorForeign)
        {
            this.bookID = bookID;
            this.bookNameEN = bookNameEN;
            this.bookNameForeign = bookNameForeign;
            this.authorEN = authorEN;
            this.authorForeign = authorForeign;
        }
    }

    public AUTHOR_wrote_BOOK(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    @Override
    protected String getSPARQLQuery(){
        //find book with author
        return "SELECT ?" + bookNamePH + " ?" + bookNameForeignPH + " ?" + bookNameENPH +
                " ?" + authorENPH + " ?" + authorForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + bookNamePH + " wdt:P31 wd:Q571 . " + //is a book
                "    ?" + bookNamePH + " wdt:P50 ?author . " + //has an author
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + bookNamePH + " rdfs:label ?" + bookNameForeignPH + " . " +
                "                           ?author rdfs:label ?" + authorForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + bookNamePH + " rdfs:label ?" + bookNameENPH + " . " +
                "                           ?author rdfs:label ?" + authorENPH + " . " +
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
            String authorEN = SPARQLDocumentParserHelper.findValueByNodeName(head, authorENPH);
            String authorForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, authorForeignPH);

            QueryResult qr = new QueryResult(bookID, bookNameEN, bookNameForeign, authorEN, authorForeign);
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

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.bookID));
        }

    }

    private String AUTHOR_wrote_BOOK_EN_correct(QueryResult qr){
        String sentence = qr.authorEN + " wrote " + qr.bookNameEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String AUTHOR_wrote_BOOK_Foreign_correct(QueryResult qr) {
        return qr.authorForeign + "が" + qr.bookNameForeign + "を書きました。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.authorEN);
        pieces.add("wrote");
        pieces.add(qr.bookNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr){
        String question = this.AUTHOR_wrote_BOOK_Foreign_correct(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.bookNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = AUTHOR_wrote_BOOK_Foreign_correct(qr) + "\n";
        String sentence2 = qr.authorEN + " " + QuestionUtils.FILL_IN_BLANK_TEXT +
                " " + qr.bookNameEN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return "wrote";
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.bookNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }
}
