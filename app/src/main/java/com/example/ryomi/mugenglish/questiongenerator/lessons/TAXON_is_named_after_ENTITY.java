package com.example.ryomi.mugenglish.questiongenerator.lessons;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
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
import java.util.Collections;
import java.util.List;

public class TAXON_is_named_after_ENTITY extends Lesson {
    public static final String KEY = "TAXON_is_named_after_ENTITY";
    //placeholders
    private final String entityPH = "entityPH";
    private final String entityForeignPH = "entityForeignPH";
    private final String entityENPH = "entityENPH";
    private final String taxonPH = "taxonPH";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String entityID;
        private String entityEN;
        private String entityForeign;
        private String taxon;

        private QueryResult(
                String entityID,
                String entityEN,
                String entityForeign,
                String taxon)
        {
            this.entityID = entityID;
            this.entityEN = entityEN;
            this.entityForeign = entityForeign;
            this.taxon = taxon;
        }
    }

    public TAXON_is_named_after_ENTITY(WikiBaseEndpointConnector connector, LessonData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    @Override
    protected String getSPARQLQuery(){
        //find book with author
        return "SELECT ?" + entityPH + " ?" + entityForeignPH + " ?" + entityENPH +
                " ?" + taxonPH + " " +
                "WHERE " +
                "{" +
                "    ?taxon wdt:P31 wd:Q16521; " + //is a taxon
                "           wdt:P225 ?" + taxonPH + "; " + //official taxon name
                "           wdt:P138 ?" + entityPH + " . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + entityPH + " rdfs:label ?" + entityForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + entityPH + " rdfs:label ?" + entityENPH + " } . " + //English translation

                "    BIND (wd:%s as ?" + entityPH + ") . " +
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
            String entityID = SPARQLDocumentParserHelper.findValueByNodeName(head, entityPH);
            entityID = QGUtils.stripWikidataID(entityID);
            String entityEN = SPARQLDocumentParserHelper.findValueByNodeName(head, entityENPH);
            String entityForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, entityForeignPH);
            String taxon = SPARQLDocumentParserHelper.findValueByNodeName(head, taxonPH);

            QueryResult qr = new QueryResult(entityID, entityEN, entityForeign, taxon);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.entityForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            QuestionData fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.entityID));
        }
    }

    private String TAXON_is_named_after_ENTITY_EN_correct(QueryResult qr){
        String sentence = qr.taxon + " is named after " + qr.entityEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String TAXON_is_named_after_ENTITY_Foreign_correct(QueryResult qr) {
        return qr.taxon + "は" + qr.entityForeign + "にちなんて命名されました。";
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.taxon + " is named " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE + " " + qr.entityEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private List<String> fillInBlankChoices(){
        List<String> prepositions = new ArrayList<>();
        prepositions.add("before");
        prepositions.add("during");
        prepositions.add("at");
        prepositions.add("from");
        prepositions.add("by");
        prepositions.add("for");
        Collections.shuffle(prepositions);

        List<String> choices = new ArrayList<>();
        choices.add(prepositions.get(0));
        choices.add(prepositions.get(1));

        return choices;
    }

    private String fillInBlankAnswer(){
        return "after";
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<String> choices = fillInBlankChoices();
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.entityForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = TAXON_is_named_after_ENTITY_Foreign_correct(qr) + "\n";
        String sentence2 = qr.taxon + " " + QuestionUtils.FILL_IN_BLANK_TEXT + " " + qr.entityEN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return "is named after";
    }

    private List<String> fillInBlankInputAlternateAnswers(){
        List<String> alternateAnswer = new ArrayList<>(1);
        alternateAnswer.add("was named after");
        return alternateAnswer;
    }

    private QuestionData createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        List<String> acceptableAnswers = fillInBlankInputAlternateAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonData.getId());
        data.setTopic(qr.entityForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

        return data;
    }

}
