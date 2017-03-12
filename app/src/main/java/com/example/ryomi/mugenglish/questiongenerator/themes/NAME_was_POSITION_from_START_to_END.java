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

public class NAME_was_POSITION_from_START_to_END extends Theme{
    //クエリーするときの名前
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String positionHeldForeignPH = "positionHeldForeign";
    private final String positionHeldENPH = "positionHeldEN";
    private final String startDatePH = "startDate";
    private final String endDatePH = "endDate";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String personID;
        private String positionHeldEN;
        private String positionHeldForeign;
        private String personNameEN;
        private String personNameForeign;
        private String startDate;
        private String endDate;

        private QueryResult(
                String personID,
                String positionHeldEN, String positionHeldForeign,
                String personNameEN, String personNameForeign,
                String startDate, String endDate)
        {
            this.personID = personID;
            this.positionHeldEN = positionHeldEN;
            this.positionHeldForeign = positionHeldForeign;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public NAME_was_POSITION_from_START_to_END(WikiBaseEndpointConnector connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;/*
		super.backupIDsOfTopics.add("Q5284");//Bill Gates
		super.backupIDsOfTopics.add("Q8027");//Elon Musk*/
    }

    @Override
    protected String getSPARQLQuery(){

        //find the education institution, start and end date of one individual
        //ex ビル・ゲーツ | ハーバード・カレッジ | Bill Gates | Harvard College | 1973-01-01T00:00:00Z | 1975-01-01T00:00:00Z
        return 	"SELECT ?" + personNamePH + " ?" + personNameForeignPH +  " ?" + personNameENPH +
                " ?" + positionHeldForeignPH + " ?" + positionHeldENPH +
                " ?" + startDatePH + " ?" + endDatePH + " " +
                "WHERE" +
                "{" +
                "    ?" + personNamePH + " p:P39 ?positionStatement . " + //this person has a position
                "    ?positionStatement ps:P39 ?position . " + //grabbing name of position
                "    ?positionStatement pq:P580 ?" + startDatePH + " . " + //grabbing start date
                "    ?positionStatement pq:P582 ?" + endDatePH + " . " + //grabbing end date
                "	 SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //these labels should be in the foreign language
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " +  //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + ". " + //grabbing foreign label of person
                "                           ?position rdfs:label ?" + positionHeldForeignPH + " . }" +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //English labels
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH + " . " +
                "                           ?position rdfs:label ?" + positionHeldENPH + " . } " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'} " + //everything else is in English

                "    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
                "    FILTER (YEAR(?" + startDatePH + ") != YEAR(?" + endDatePH + ") ) . " + //so we can prevent from 2000 to 2000
                "} ";

    }

    @Override
    protected void processResultsIntoClassWrappers(Document document){
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
            personID = QGUtils.stripWikidataID(personID);

            String positionHeldEN = SPARQLDocumentParserHelper.findValueByNodeName(head, positionHeldENPH);
            String positionHeldForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, positionHeldForeignPH);

            String startDateTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, startDatePH);
            String endDateTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, endDatePH);

            String startDate = this.getYearFromFullISO8601DateTime(startDateTimeValue);
            String endDate = this.getYearFromFullISO8601DateTime(endDateTimeValue);

            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
            String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);

            QueryResult qr = new QueryResult(
                    personID,
                    positionHeldEN, positionHeldForeign,
                    personNameEN, personNameForeign,
                    startDate, endDate);

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
            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            QuestionData fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);

            QuestionData trueFalseQuestionTrue = createTrueFalseQuestion(qr, true);
            QuestionData trueFalseQuestionFalse = createTrueFalseQuestion(qr, false);
            int i = new Random().nextInt();
            if (i%2 == 0) {
                questionSet.add(trueFalseQuestionTrue);
                questionSet.add(trueFalseQuestionFalse);
            } else {
                questionSet.add(trueFalseQuestionFalse);
                questionSet.add(trueFalseQuestionTrue);
            }

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.personID));
        }
    }

    private String getYearFromFullISO8601DateTime(String fullISO8601DateTime){
        //we can assume the first four letters are the date
        // ex: 1990-01-01T00:00:00Z
        return fullISO8601DateTime.substring(0,4);
    }


    private String NAME_was_POSITION_from_START_to_END_EN_correct(QueryResult qr){
        //'the' を公職名の前につけるかどうか
        String position = GrammarRules.articleBeforePosition(qr.positionHeldEN);

        String sentence = qr.personNameEN + " was " + position +
                " from " + qr.startDate + " to " + qr.endDate + ".";
        //人の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }


    //correct sentence in foreign language (need only one?)
    private String formatSentenceForeign(QueryResult qr){
        return qr.personNameForeign + "は" + qr.startDate + "年から" + qr.endDate + "年まで" + qr.positionHeldForeign + "でした。";
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceForeign(qr) + "\n\n";
        //'the' を公職名の前につけるかどうか
        String position = GrammarRules.articleBeforePosition(qr.positionHeldEN);
        String sentence2 = qr.personNameEN + " was " + position +
                " " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE + " " + qr.startDate + " to " + qr.endDate + ".";
        //人の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + sentence2;
    }

    private String fillInBlankAnswer(){
        return "from";
    }

    private List<String> fillInBlankChoices(){
        List<String> prepositions = new ArrayList<>();
        prepositions.add("before");
        prepositions.add("during");
        prepositions.add("at");
        prepositions.add("by");
        prepositions.add("for");

        Collections.shuffle(prepositions);

        List<String> choices = new ArrayList<>();
        choices.add(prepositions.get(0));
        choices.add(prepositions.get(1));

        return choices;
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

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence1 = formatSentenceForeign(qr) + "\n\n";
        //'the' を公職名の前につけるかどうか
        String position = GrammarRules.articleBeforePosition(qr.positionHeldEN);
        String sentence2 = qr.personNameEN + " was " + position +
                " from " + qr.startDate + " " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.endDate + ".";
        //人の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + sentence2;
    }

    private String fillInBlankAnswer2(){
        return "to";
    }

    private List<String> fillInBlankChoices2(){
        List<String> prepositions = new ArrayList<>();
        prepositions.add("after");
        prepositions.add("during");
        prepositions.add("at");
        prepositions.add("by");
        prepositions.add("for");

        Collections.shuffle(prepositions);

        List<String> choices = new ArrayList<>();
        choices.add(prepositions.get(0));
        choices.add(prepositions.get(1));

        return choices;
    }

    private QuestionData createFillInBlankQuestion2(QueryResult qr){
        String question = this.fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2();
        List<String> choices = fillInBlankChoices2();
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

    private String trueFalseQuestion(QueryResult qr, boolean correct){
        String fromTo;
        if (correct)
            fromTo = "from " + qr.startDate + " to " + qr.endDate;
        else
            fromTo = "from " + qr.endDate + " to " + qr.startDate;

        String position = GrammarRules.articleBeforePosition(qr.positionHeldEN);
        String sentence = qr.personNameEN + " was " + position + " " + fromTo + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private QuestionData createTrueFalseQuestion(QueryResult qr, boolean correct){
        String question = trueFalseQuestion(qr, correct);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(correct ? QuestionUtils.TRUE_FALSE_QUESTION_TRUE : QuestionUtils.TRUE_FALSE_QUESTION_FALSE);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }
}
