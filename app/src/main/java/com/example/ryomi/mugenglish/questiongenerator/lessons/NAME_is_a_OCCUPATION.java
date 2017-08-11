package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
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

public class NAME_is_a_OCCUPATION extends Lesson {
    public static final String KEY = "NAME_is_a_OCCUPATION";
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

    public NAME_is_a_OCCUPATION(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    @Override
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

    @Override
    protected void processResultsIntoClassWrappers(Document document) {
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
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.personNameForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
        }

    }

    private String NAME_is_OCCUPATION_EN_correct(QueryResult qr){
        String indefiniteArticle = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String sentence = qr.personNameEN + " is " + indefiniteArticle + ".";
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
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN));
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
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    //give a hint (first 1 or 2 characters, depending on length of word)
    private int hintBorder = 7;
    private String fillInBlankQuestion(QueryResult qr){
        int hintCt = qr.occupationEN.length() > hintBorder ? 2 : 1;
        String precedingHint = qr.occupationEN.substring(0,hintCt);
        String followingHint = qr.occupationEN.substring(qr.occupationEN.length()-hintCt);
        String indefiniteArticle = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String article;
        //remove article
        if (indefiniteArticle.substring(0,2).equals("a "))
            article = "a";
        else
            article = "an";

        String sentence = qr.personNameEN + " is " + article + " " +
                precedingHint + QuestionUtils.FILL_IN_BLANK_TEXT + followingHint + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer(QueryResult qr){
        int hintCt = qr.occupationEN.length() > hintBorder ? 2 : 1;;
        return qr.occupationEN.substring(hintCt, qr.occupationEN.length()-hintCt);
    }

    //in case the user types the whole thing in
    private List<String> fillInBlankAlternateAnswers(QueryResult qr){
        List<String> answers = new ArrayList<>(1);
        answers.add(qr.occupationEN);
        return answers;
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<String> acceptableAnswers = fillInBlankAlternateAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

        return data;
    }
}
