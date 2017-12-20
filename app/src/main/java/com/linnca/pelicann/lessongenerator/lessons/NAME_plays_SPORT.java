package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.SportsHelper;
import com.linnca.pelicann.lessongenerator.TermAdjuster;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionResponseChecker;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_Instructions;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NAME_plays_SPORT extends Lesson{
    public static final String KEY = "NAME_plays_SPORT";

    private List<QueryResult> queryResults = new ArrayList<>();
    //to record all sports a person plays
    private final Map<String, List<QueryResult>> queryResultMap = new HashMap<>();

    private class QueryResult {
        private String personID;
        private String personEN;
        private String personJP;
        private String sportID;
        private String sportNameEN;
        private String sportNameJP;
        //we need these for creating questions.
        //we will get them from fireBase
        private String verb = "";
        private String object = "";

        private QueryResult( String personID,
                             String personEN, String personJP,
                             String sportID, String sportNameEN, String sportNameJP){
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.sportID = sportID;
            this.sportNameEN = sportNameEN;
            this.sportNameJP = sportNameJP;
            //temporary. will update by connecting to db
            this.verb = "play";
            //also temporary
            this.object = sportNameEN;
        }
    }

    public NAME_plays_SPORT(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION;

    }

    @Override
    protected String getSPARQLQuery(){
        return
                "SELECT ?person ?personEN ?personLabel " +
                        " ?sport ?sportEN ?sportLabel " +
                        "		WHERE " +
                        "		{ " +
                        "			?person wdt:P641 ?sport . " + //plays sport
                        "		    FILTER NOT EXISTS { ?person wdt:P570 ?dateDeath } . " +//死んでいない（played ではなくてplays）
                        "           ?person rdfs:label ?personEN . " + //English label
                        "           ?sport rdfs:label ?sportEN . " + //English label
                        "           FILTER (LANG(?personEN) = '" +
                                    WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           FILTER (LANG(?sportEN) = '" +
                                    WikiBaseEndpointConnector.ENGLISH + "') . " +
                        "           SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                                    WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                                    WikiBaseEndpointConnector.ENGLISH + "' } " +
                        "           BIND (wd:%s as ?person) " +
                        "		}";
    }

    @Override
    protected synchronized void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, "sport");
            // ~entity/id になってるから削る
            sportID = WikiDataEntity.getWikiDataIDFromReturnedResult(sportID);
            String sportNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportLabel");
            String sportNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportEN");
            sportNameEN = TermAdjuster.adjustSportsEN(sportNameEN);

            QueryResult qr = new QueryResult(personID,
                    personEN, personJP,
                    sportID, sportNameEN, sportNameJP);

            queryResults.add(qr);

            //to help with true/false questions
            if (queryResultMap.containsKey(personID)){
                List<QueryResult> value = queryResultMap.get(personID);
                value.add(qr);
            } else {
                List<QueryResult> list = new ArrayList<>();
                list.add(qr);
                queryResultMap.put(personID, list);
            }

        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            List<QuestionData> fillInBlankMultipleChoice = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoice);

            List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
            questionSet.add(spellingQuestion);

            List<QuestionData> fillInBlank = createFillInTheBlankQuestion(qr);
            questionSet.add(fillInBlank);
            
            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));

        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord play;
        if (qr.object.equals("")){
            play = new VocabularyWord("", qr.verb, qr.sportNameJP + "をする",
                    formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        } else {
            play = new VocabularyWord("", qr.verb, "（スポーツを）する",
                    formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        }
        String exampleSentenceEN = "";
        String exampleSentenceJP = "";
        if (!qr.object.equals("")){
            exampleSentenceEN = formatSentenceEN(qr);
            exampleSentenceJP = formatSentenceJP(qr);
        }
        VocabularyWord sport = new VocabularyWord("", qr.sportNameEN,qr.sportNameJP,
                exampleSentenceEN, exampleSentenceJP, KEY);

        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(play);
        words.add(sport);
        return words;
    }

    //we want to read from the database and then create the questions
    @Override
    protected void accessDBWhenCreatingQuestions(){
        Set<String> sportIDs = new HashSet<>(queryResults.size());
        for (QueryResult qr : queryResults){
            sportIDs.add(qr.sportID);
        }
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onSportQueried(String wikiDataID, String verb, String object) {
                //find all query results with the sport ID and update it
                for (QueryResult qr : queryResults){
                    if (qr.sportID.equals(wikiDataID)){
                        qr.verb = verb;
                        qr.object = object;
                    }
                }
            }

            @Override
            public void onSportsQueried() {
                createQuestionsFromResults();
                saveNewQuestions();
            }
        };
        db.getSports(sportIDs, onDBResultListener);
    }

    private String formatSentenceEN(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
        return qr.personEN + " " + verbObject + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.sportNameJP + "をします。";
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> multipleChoiceQuestions = createMultipleChoiceQuestions();

        List<List<QuestionData>> questionSet = new ArrayList<>(4);
        questionSet.addAll(multipleChoiceQuestions);
        return questionSet;
    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(0,4);
        Collections.shuffle(multipleChoiceQuestions);
    }

    private List<String> multipleChoiceQuestions(){
        List<String> questions = new ArrayList<>(4);
        questions.add(Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
        " karate");
        questions.add(Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " soccer");
        questions.add(Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " baseball");
        questions.add(Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " judo");
        return questions;
    }

    private List<String> multipleChoiceAnswers(){
        List<String> answers = new ArrayList<>(4);
        answers.add("does");
        answers.add("plays");
        answers.add("plays");
        answers.add("does");
        return answers;
    }

    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("does");
        choices.add("plays");
        return choices;
    }

    private List<List<QuestionData>> createMultipleChoiceQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(4);
        List<String> allQuestions = multipleChoiceQuestions();
        List<String> allAnswers = multipleChoiceAnswers();
        List<String> choices = multipleChoiceChoices();
        for (int i=0; i<4; i++ ){
            List<QuestionData> questionDataList = new ArrayList<>(1);
            String question = allQuestions.get(i);
            String answer = allAnswers.get(i);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(question);
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questionDataList.add(data);
            questions.add(questionDataList);
        }
        return questions;
    }

    //just for true/false
    private class SimpleQueryResult {
        final String wikiDataID;
        final String sportEN;
        final String sportJP;
        final String verb;

        SimpleQueryResult(String wikiDataID, String sportEN, String sportJP, String verb) {
            this.wikiDataID = wikiDataID;
            this.sportEN = sportEN;
            this.sportJP = sportJP;
            this.verb = verb;
        }
    }
    private List<SimpleQueryResult> popularSports(){
        List<SimpleQueryResult> list = new LinkedList<>();
        list.add(new SimpleQueryResult("Q2736", "soccer", "サッカー", "play"));
        list.add(new SimpleQueryResult("Q5369", "baseball", "野球", "play"));
        list.add(new SimpleQueryResult("Q847", "tennis", "テニス", "play"));
        list.add(new SimpleQueryResult("Q38108", "figure skating", "フィギュアスケート", "do"));
        list.add(new SimpleQueryResult("Q3930", "table tennis", "卓球", "play"));
        return list;
    }

    private String formatFalseAnswer(QueryResult qr, SimpleQueryResult sqr){
        String verbObject = SportsHelper.getVerbObject(sqr.verb, sqr.sportEN, SportsHelper.PRESENT3RD);
        return qr.personEN + " " + verbObject + ".";
    }

    private FeedbackPair trueFalseFeedback(SimpleQueryResult sqr){
        //we are displaying sport names the user might not have encountered yet,
        // so explain what that sport is afterwards
        String response1 = Question_TrueFalse.getTrueFalseString(true);
        String response2 = Question_TrueFalse.getTrueFalseString(false);
        String feedback = sqr.sportEN + ": " + sqr.sportJP;
        List<String> responses = new ArrayList<>(2);
        responses.add(response1);
        responses.add(response2);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);

    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        //1 true and 1 false question
        List<QuestionData> questionDataList = new ArrayList<>(2);
        String question = this.formatSentenceEN(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);

        //remove all sports the person plays
        List<SimpleQueryResult> falseAnswers = popularSports();
        List<QueryResult> allSports = queryResultMap.get(qr.personID);
        for (QueryResult singleSport : allSports) {
            for (Iterator<SimpleQueryResult> iterator = falseAnswers.iterator(); iterator.hasNext();) {
                SimpleQueryResult sport = iterator.next();
                if (sport.wikiDataID.equals(singleSport.sportID))
                    iterator.remove();
            }
        }

        Collections.shuffle(falseAnswers);
        //we don't want too many false answers
        //or the answers will most likely be false
        SimpleQueryResult falseSport = falseAnswers.get(0);

        question = this.formatFalseAnswer(qr, falseSport);
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(trueFalseFeedback(falseSport));
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(Question_TrueFalse.TRUE_FALSE_QUESTION_FALSE);
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.sportNameEN, SportsHelper.PRESENT3RD);
        if (verbObject.contains("plays ") || verbObject.contains("does ")){
            return qr.sportNameEN;
        } else {
            return verbObject;
        }
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.sportNameEN, SportsHelper.PRESENT3RD);
        String sentence = qr.personEN + " ";
        if (verbObject.contains("plays ")){
            sentence += "plays";
        } else if (verbObject.contains("does ")){
            sentence += "does";
        }
        sentence += Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return sentence;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback(List<SimpleQueryResult> sqrList, String answer){
        //we are displaying sport names the user might not have encountered yet,
        // so explain what that sport is afterwards
        StringBuilder feedback = new StringBuilder("");
        List<String> responses = new ArrayList<>(sqrList.size());
        responses.add(answer);
        for (SimpleQueryResult sqr : sqrList){
            responses.add(sqr.sportEN);
            String sqrFeedback = sqr.sportEN + ":" + sqr.sportJP + "\n";
            feedback.append(sqrFeedback);
        }
        return new FeedbackPair(responses, feedback.toString(), FeedbackPair.EXPLICIT);

    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        //remove all sports the person plays
        List<SimpleQueryResult> falseAnswers = popularSports();
        List<QueryResult> allSports = queryResultMap.get(qr.personID);
        for (QueryResult singleSport : allSports) {
            for (Iterator<SimpleQueryResult> iterator = falseAnswers.iterator(); iterator.hasNext();) {
                SimpleQueryResult sport = iterator.next();
                if (sport.wikiDataID.equals(singleSport.sportID))
                    iterator.remove();
            }
        }
        Collections.shuffle(falseAnswers);

        List<QuestionData> questionDataList = new ArrayList<>(2);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        while (falseAnswers.size() >= 2) {
            List<SimpleQueryResult> falseChoices = new ArrayList<>(2);
            falseChoices.add(falseAnswers.get(0));
            falseChoices.add(falseAnswers.get(1));
            falseAnswers.remove(0);
            falseAnswers.remove(0);
            List<FeedbackPair> allFeedback = new ArrayList<>(1);
            List<String> choices = new ArrayList<>(3);
            choices.add(answer);
            choices.add(falseChoices.get(0).sportEN);
            choices.add(falseChoices.get(1).sportEN);
            allFeedback.add(fillInBlankMultipleChoiceFeedback(falseChoices, answer));
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(fillInBlankMultipleChoiceQuestion(qr));
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setFeedback(allFeedback);
            questionDataList.add(data);
        }

        return questionDataList;
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_Spelling.QUESTION_TYPE);
        data.setQuestion(qr.sportNameJP);
        data.setChoices(null);
        data.setAnswer(qr.sportNameEN);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private String fillInTheBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInTheBlankAnswer(QueryResult qr){
        return SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
    }

    private List<QuestionData> createFillInTheBlankQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        String question = fillInTheBlankQuestion(qr);
        String answer = fillInTheBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> instructionsQuestion = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(instructionsQuestion);
        return questionSet;
    }

    private String instructionQuestionQuestion(){
        return "あなたは何のスポーツをしていますか。";
    }

    private String instructionQuestionAnswer(){
        return "I play " + QuestionResponseChecker.ANYTHING + ".";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        // so I can cover sports like swimming..
        //this will accept almost anything, but it's more important
        // to have the user say something than marking correctness...
        String acceptableAnswer = "I " + QuestionResponseChecker.ANYTHING + ".";
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(acceptableAnswer);
        return acceptableAnswers;

    }

    private FeedbackPair instructionQuestionFeedback1(){
        String response = "I plays " + QuestionResponseChecker.ANYTHING + ".";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        String feedback = "自分のことを言っている場合、動詞の最後のsはいりません。\nplaysではなくplayになります。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private FeedbackPair instructionQuestionFeedback2(){
        String response = "I does " + QuestionResponseChecker.ANYTHING + ".";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        String feedback = "自分のことを言っている場合、動詞の最後のsはいりません。\ndoesは特別なので、doeではなくdoになります。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private List<QuestionData> createInstructionQuestion(){
        String question = this.instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        List<String> acceptableAnswers = instructionQuestionAcceptableAnswers();
        List<FeedbackPair> allFeedback = new ArrayList<>(2);
        allFeedback.add(instructionQuestionFeedback1());
        allFeedback.add(instructionQuestionFeedback2());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_Instructions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(allFeedback);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}