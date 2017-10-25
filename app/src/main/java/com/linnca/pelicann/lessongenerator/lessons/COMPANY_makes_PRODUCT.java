package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.lessongenerator.TermAdjuster;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class COMPANY_makes_PRODUCT extends Lesson{
    public static final String KEY = "COMPANY_makes_PRODUCT";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String companyID;
        private final String companyNameEN;
        private final String companyNameJP;
        private final String productEN;
        private final String productJP;

        private QueryResult(
                String companyID,
                String companyNameEN,
                String companyNameJP,
                String productEN,
                String productJP)
        {
            this.companyID = companyID;
            this.companyNameEN = companyNameEN;
            this.companyNameJP = companyNameJP;
            this.productEN = productEN;
            this.productJP = productJP;
        }
    }

    public COMPANY_makes_PRODUCT(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 1;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_OTHER;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find company name and blood type
        return "SELECT ?companyName ?companyNameLabel ?companyNameEN " +
                " ?productEN ?productLabel " +
                "WHERE " +
                "{" +
                "    ?companyName wdt:P31 wd:Q4830453 . " + //is a business enterprise
                "    ?companyName wdt:P1056 ?product . " + //makes a product/material
                "    ?companyName rdfs:label ?companyNameEN . " +
                "    ?product rdfs:label ?productEN . " +
                "    FILTER (LANG(?companyNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?productEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?companyName) . " + //binding the ID of entity as ?company
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
            String companyID = SPARQLDocumentParserHelper.findValueByNodeName(head, "companyName");
            companyID = LessonGeneratorUtils.stripWikidataID(companyID);
            String companyNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "companyNameEN");
            String companyNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "companyNameLabel");
            String productEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "productEN");
            String productJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "productLabel");

            QueryResult qr = new QueryResult(companyID, companyNameEN, companyNameJP, productEN, productJP);
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

            List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
            questionSet.add(spellingQuestion);

            List<QuestionData> translateQuestion = createTranslateQuestion(qr);
            questionSet.add(translateQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> translateQuestion2 = createTranslateQuestion2(qr);
            questionSet.add(translateQuestion2);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.companyID, qr.companyNameJP));
        }

    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.companyNameEN);
        pieces.add("makes");
        pieces.add(qr.productEN);
        return pieces;
    }

    private String formatSentenceJP(QueryResult qr){
        //作る/造る distinction impossible to determine?
        return qr.companyNameJP + "は" + qr.productJP + "をつくります。";
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.companyNameEN + " makes " + qr.productEN + ".";
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
        data.setTopic(qr.companyNameJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        String question = qr.productJP;
        String answer = qr.productEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.companyNameJP);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createTranslateQuestion(QueryResult qr){
        String question = qr.productJP;
        String answer = qr.productEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.companyNameJP);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.companyNameEN + " " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " " + qr.productEN + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String fillInBlankAnswer(){
        return "makes";
    }

    private List<String> fillInBlankAlternateAnswers(){
        List<String> answers = new ArrayList<>(1);
        answers.add("make");
        return answers;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<String> acceptableAnswers = fillInBlankAlternateAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.companyNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

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
        String acceptableAnswer = qr.productEN + " make " + qr.companyNameEN + ".";
        acceptableAnswer = GrammarRules.uppercaseFirstLetterOfSentence(acceptableAnswer);
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(acceptableAnswer);
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestion2(QueryResult qr){
        String question = translateQuestion2(qr);
        String answer = translateAnswer2(qr);
        List<String> acceptableAnswers = translateAcceptableAnswers2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.companyNameJP);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<String> translateAcceptableAnswersGeneric(){
        List<String> acceptableAnswers = new ArrayList<>(5);
        acceptableAnswers.add("作る");
        acceptableAnswers.add("造る");
        acceptableAnswers.add("造ります");
        acceptableAnswers.add("作ります");
        acceptableAnswers.add("つくります");
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "make";
        String answer = "つくる";
        List<String> acceptableAnswers = translateAcceptableAnswersGeneric();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        int index = 1;

        List<String> questionIDs = new ArrayList<>();
        List<QuestionData> toSave1 = createTranslateQuestionGeneric();
        int toSave1Size = toSave1.size();
        while (index <= toSave1Size){
            questionIDs.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, index));
            index++;
        }
        List<List<String>> questionSets = new ArrayList<>();
        questionSets.add(questionIDs);
        return questionSets;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> toSaveSet1 = createTranslateQuestionGeneric();


        List<QuestionData> questions = new ArrayList<>(1);
        int set1Size = toSaveSet1.size();
        for (int i=1; i<= set1Size; i++){
            String id = LessonGeneratorUtils.formatGenericQuestionID(KEY, i);
            toSaveSet1.get(i-1).setId(id);
            questions.add(toSaveSet1.get(i-1));
        }

        return questions;

    }
}