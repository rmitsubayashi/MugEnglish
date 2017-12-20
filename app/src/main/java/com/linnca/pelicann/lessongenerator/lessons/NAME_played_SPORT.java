package com.linnca.pelicann.lessongenerator.lessons;

import android.util.Log;

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
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_TranslateWord;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NAME_played_SPORT extends Lesson{
    public static final String KEY = "NAME_played_SPORT";

    private List<QueryResult> queryResults = new ArrayList<>();
    //to record all sports a person played
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

    public NAME_played_SPORT(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 4;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        //get people at least 50 years old.
        // since organized sports are relatively new and not many sports players
        // are retired
        return
                "SELECT ?person ?personEN ?personLabel " +
                        " ?sport ?sportEN ?sportLabel " +
                        "		WHERE " +
                        "		{ " +
                        "			?person wdt:P641 ?sport . " + //played sport
                        "		    ?person wdt:P569 ?dateBirth . " +//has birth date
                        "           ?person rdfs:label ?personEN . " + //English label
                        "           ?sport rdfs:label ?sportEN . " + //English label
                        "           FILTER (year(NOW()) - year(?dateBirth) > 50) . " + //at least 50 years old
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

            List<QuestionData> sentencePuzzle = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzle);

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            List<QuestionData> translateQuestion = createTranslationQuestion(qr);
            questionSet.add(translateQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));

        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){

        String exampleSentenceEN = "";
        String exampleSentenceJP = "";
        if (!qr.object.equals("")){
            exampleSentenceEN = formatSentenceEN(qr);
            exampleSentenceJP = formatSentenceJP(qr);
        }
        VocabularyWord sport = new VocabularyWord("", qr.sportNameEN, qr.sportNameJP,
                exampleSentenceEN, exampleSentenceJP, KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
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
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PAST);
        return qr.personEN + " " + verbObject + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.sportNameJP + "をしました。";
    }

    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        String verb = SportsHelper.inflectVerb(qr.verb, SportsHelper.PAST);
        pieces.add(verb);
        if (!qr.object.equals(""))
            pieces.add(qr.object);

        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        String question = formatSentenceJP(qr);
        List<String> choices = puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private String formatFalseAnswer(QueryResult qr){
        //present instead of past
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PRESENT3RD);
        return qr.personEN + " " + verbObject + ".";
    }

    private FeedbackPair trueFalseFeedback(QueryResult qr){
        String response = Question_TrueFalse.getTrueFalseString(true);
        String feedback = qr.personJP + "は引退しているので playedを使いましょう。";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(2);
        String question = this.formatSentenceEN(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(Question_TrueFalse.getTrueFalseString(true));
        data.setAcceptableAnswers(null);
        questionDataList.add(data);


        question = this.formatFalseAnswer(qr);
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(trueFalseFeedback(qr));
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(Question_TrueFalse.getTrueFalseString(false));
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);
        questionDataList.add(data);

        return questionDataList;
    }

    private String translateQuestion(QueryResult qr){
        return qr.sportNameJP + "をしました";
    }

    private String translateAnswer(QueryResult qr){
        return SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PAST);
    }

    private List<String> translateAcceptableAnswers(QueryResult qr){
        List<String> answers = new ArrayList<>(1);
        String answer = "I " + SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PAST);
        answers.add(answer);
        return answers;
    }

    private List<QuestionData> createTranslationQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(translateQuestion(qr));
        data.setChoices(null);
        data.setAnswer(translateAnswer(qr));
        data.setAcceptableAnswers(translateAcceptableAnswers(qr));
        questionDataList.add(data);
        return questionDataList;
    }
}