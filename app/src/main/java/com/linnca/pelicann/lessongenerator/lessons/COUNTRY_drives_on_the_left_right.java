package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class COUNTRY_drives_on_the_left_right extends Lesson{
    public static final String KEY = "COUNTRY_drives_on_the_left_right";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String countryID;
        private final String countryEN;
        private final String countryJP;
        private final String sideEN;
        private final String sideJP;

        private QueryResult(
                String countryID,
                String countryEN,
                String countryJP,
                String sideEN,
                String sideJP)
        {
            this.countryID = countryID;
            this.countryEN = countryEN;
            this.countryJP = countryJP;
            this.sideEN = sideEN;
            this.sideJP = sideJP;
        }
    }

    public COUNTRY_drives_on_the_left_right(WikiBaseEndpointConnector connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 3;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PLACE;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find country name and blood type
        return "SELECT ?country ?countryLabel ?countryEN " +
                " ?sideEN ?sideLabel " +
                "WHERE " +
                "{" +
                "    ?country wdt:P1622 ?side . " + //has a driving side
                "    ?country rdfs:label ?countryEN . " +
                "    ?side rdfs:label ?sideEN . " +
                "    FILTER (LANG(?countryEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?sideEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?country) . " + //binding the ID of entity as ?country
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
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");
            countryID = LessonGeneratorUtils.stripWikidataID(countryID);
            String countryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryEN");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            String sideEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sideEN");
            String sideJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "sideLabel");

            QueryResult qr = new QueryResult(countryID, countryEN, countryJP, sideEN, sideJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> translateQuestion2 = createTranslateQuestion2(qr);
            questionSet.add(translateQuestion2);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.countryID, qr.countryJP, null));
        }

    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.countryEN);
        pieces.add("drives");
        pieces.add("on");
        pieces.add("the " + qr.sideEN);
        return pieces;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.countryJP + "は" + qr.sideJP + "側通行です。";
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.countryEN + " drives on the " + qr.sideEN + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private String trueFalseQuestion(QueryResult qr, boolean isTrue){
        if (isTrue){
            return formatSentenceEN(qr);
        } else {
            String opposite = qr.sideEN.equals("left") ? "right" : "left";
            String wrongSentence = qr.countryEN + " drives on the " + opposite + ".";
            wrongSentence = GrammarRules.uppercaseFirstLetterOfSentence(wrongSentence);
            return wrongSentence;
        }
    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> dataList = new ArrayList<>();

        String question = trueFalseQuestion(qr, true);
        String answer = Question_TrueFalse.getTrueFalseString(true);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        question = trueFalseQuestion(qr, false);
        answer = Question_TrueFalse.getTrueFalseString(false);
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.countryEN + " drives " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " the " + qr.sideEN + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String fillInBlankAnswer(){
        return "on";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }


    private String translateQuestion2(QueryResult qr){
        return formatSentenceJP(qr);
    }

    private String translateAnswer2(QueryResult qr){
        return formatSentenceEN(qr);
    }

    //accept 'make' instead of 'makes'
    private List<String> translateAcceptableAnswers2(QueryResult qr){
        String acceptableAnswer1 = qr.countryEN + " drive on the " + qr.sideEN + ".";
        acceptableAnswer1 = GrammarRules.uppercaseFirstLetterOfSentence(acceptableAnswer1);
        String acceptableAnswer2 = qr.countryEN + " drives on " + qr.sideEN + ".";
        acceptableAnswer2 = GrammarRules.uppercaseFirstLetterOfSentence(acceptableAnswer2);
        String acceptableAnswer3 = qr.countryEN + " drive on " + qr.sideEN + ".";
        acceptableAnswer3 = GrammarRules.uppercaseFirstLetterOfSentence(acceptableAnswer3);


        List<String> acceptableAnswers = new ArrayList<>(3);
        acceptableAnswers.add(acceptableAnswer1);
        acceptableAnswers.add(acceptableAnswer2);
        acceptableAnswers.add(acceptableAnswer3);
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestion2(QueryResult qr){
        String question = translateQuestion2(qr);
        String answer = translateAnswer2(qr);
        List<String> acceptableAnswers = translateAcceptableAnswers2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "drive";
        String answer = "運転";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createSpellingQuestionGeneric1(){
        String question = "左";
        String answer = "left";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createSpellingQuestionGeneric2(){
        String question = "右";
        String answer = "right";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>(4);
        for (int i=1; i<3; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> toSaveSet1 = createTranslateQuestionGeneric();
        List<QuestionData> toSaveSet2 = createSpellingQuestionGeneric1();
        List<QuestionData> toSaveSet3 = createSpellingQuestionGeneric2();

        List<QuestionData> questions = new ArrayList<>(3);
        questions.addAll(toSaveSet1);
        questions.addAll(toSaveSet2);
        questions.addAll(toSaveSet3);
        int setSize = questions.size();
        for (int i=1; i<= setSize; i++){
            String id = LessonGeneratorUtils.formatGenericQuestionID(KEY, i);
            questions.get(i-1).setId(id);
        }

        return questions;

    }
}