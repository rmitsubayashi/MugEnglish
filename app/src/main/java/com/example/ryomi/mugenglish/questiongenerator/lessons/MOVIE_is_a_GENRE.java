package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Lesson;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class MOVIE_is_a_GENRE extends Lesson {
    public static final String KEY = "MOVIE_is_a_GENRE";
    //placeholders
    private final String movieNamePH = "movieName";
    private final String movieNameForeignPH = "movieNameForeign";
    private final String movieNameENPH = "movieNameEN";
    private final String genreENPH = "genreEN";
    private final String genreForeignPH = "genreForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String movieID;
        private String movieNameEN;
        private String movieNameForeign;
        private String genreEN;
        private String genreForeign;

        private QueryResult(
                String movieID,
                String movieNameEN,
                String movieNameForeign,
                String genreEN,
                String genreForeign)
        {
            this.movieID = movieID;
            this.movieNameEN = movieNameEN;
            this.movieNameForeign = movieNameForeign;
            this.genreEN = genreEN;
            this.genreForeign = genreForeign;
        }
    }

    public MOVIE_is_a_GENRE(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 4;

    }

    @Override
    protected String getSPARQLQuery(){
        //find movie name
        return "SELECT ?" + movieNamePH + " ?" + movieNameForeignPH + " ?" + movieNameENPH +
                " ?" + genreENPH + " ?" + genreForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + movieNamePH + " wdt:P31 wd:Q11424 . " + //is a movie
                "    ?" + movieNamePH + " wdt:P136 ?genre . " + //has an genre
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + movieNamePH + " rdfs:label ?" + movieNameForeignPH + " . " +
                "                           ?genre rdfs:label ?" + genreForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + movieNamePH + " rdfs:label ?" + movieNameENPH + " . " +
                "                           ?genre rdfs:label ?" + genreENPH + " . " +
                "                           } . " + //English translation
                "    BIND (wd:%s as ?" + movieNamePH + ") . " + //binding the ID of entity as ?movie
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
            String genreEN = SPARQLDocumentParserHelper.findValueByNodeName(head, genreENPH);
            String genreForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, genreForeignPH);

            QueryResult qr = new QueryResult(movieID, movieNameEN, movieNameForeign, genreEN, genreForeign);
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

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.movieID));
        }

    }

    private String adjustGenreEN(String genreEN){
        //does not necessarily have to end with 'film'
        //for example 'film adaptation'
        if (!genreEN.contains("film")){
            return genreEN + " film";
        }
        return genreEN;
    }

    private String adjustGenreForeign(String genreForeign){
        if (!QGUtils.containsJapanese(genreForeign))
            return genreForeign;
        if (genreForeign.contains("映画") || genreForeign.contains("作品"))
            return genreForeign;
        else
            return genreForeign + "映画";
    }

    private String MOVIE_is_a_GENRE_EN_correct(QueryResult qr){
        String adjustedGenre = adjustGenreEN(qr.genreEN);
        String indefiniteArticle = GrammarRules.indefiniteArticleBeforeNoun(genreENPH);
        String sentence = qr.movieNameEN + " is " + indefiniteArticle + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        String adjustGenre = adjustGenreForeign(qr.genreForeign);
        return qr.movieNameForeign + "は" + adjustGenre + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.movieNameEN);
        pieces.add("is");
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.genreEN));
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
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.movieNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
