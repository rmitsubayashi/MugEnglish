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
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NAME_participated_in_WAR extends Theme {
    //placeholders
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String warENPH = "warEN";
    private final String warForeignPH = "warForeign";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private String warEN;
        private String warForeign;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameForeign,
                String warEN,
                String warForeign)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            this.warEN = warEN;
            this.warForeign = warForeign;
        }
    }

    public NAME_participated_in_WAR(WikiBaseEndpointConnector connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 3;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
                " ?" + warENPH + " ?" + warForeignPH + " " +
                "WHERE " +
                "{" +
                "    ?" + personNamePH + " wdt:P31 wd:Q5; " + //is a human (locations are also included in 'participated in war')
                "                          wdt:P607 ?war . " + //participated in war
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + " . " +
                "                           ?war rdfs:label ?" + warForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'," +
                "       '" + WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "' . " + //fallback language Japanese
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
                "                           ?war rdfs:label ?" + warENPH + " . " +
                "                           } . " +
                "    BIND (wd:%s as ?" + personNamePH + ") . " +
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
            String warEN = SPARQLDocumentParserHelper.findValueByNodeName(head, warENPH);
            String warForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, warForeignPH);

            QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, warEN, warForeign);
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

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
        }

    }

    private String NAME_participated_in_WAR_EN_correct(QueryResult qr){
        String warNameWithDefiniteArticle = GrammarRules.definiteArticleBeforeWar(qr.warEN);
        String sentence = qr.personNameEN + " participated in " + warNameWithDefiniteArticle + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "は" + qr.warForeign + "に参加しました。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        String warNameWithDefiniteArticle = GrammarRules.definiteArticleBeforeWar(qr.warEN);
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("participated");
        pieces.add("in");
        pieces.add(warNameWithDefiniteArticle);
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

    private String fillInBlankQuestion(QueryResult qr){
        String warNameWithDefiniteArticle = GrammarRules.definiteArticleBeforeWar(qr.warEN);
        String sentence = qr.personNameEN + " " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " in " + warNameWithDefiniteArticle + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private List<String> fillInBlankChoices(){
        List<String> inflections = new ArrayList<>();
        inflections.add("participate");
        inflections.add("participateed");
        inflections.add("participatd");
        Collections.shuffle(inflections);

        List<String> choices = new ArrayList<>();
        choices.add(inflections.get(0));
        choices.add(inflections.get(1));

        return choices;
    }

    private String fillInBlankAnswer(){
        return "participated";
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<String> choices = fillInBlankChoices();
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String warNameWithDefiniteArticle = GrammarRules.definiteArticleBeforeWar(qr.warEN);
        String sentence = qr.personNameEN + " " + QuestionUtils.FILL_IN_BLANK_TEXT +
                " in " + warNameWithDefiniteArticle + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankInputAnswer(){
        return "participated";
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }
}
