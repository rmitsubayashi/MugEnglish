package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.lessongenerator.SportsHelper;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

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
        private String personForeign;
        private String sportID;
        private String sportNameForeign;
        //we need these for creating questions.
        //we will get them from fireBase
        private String verb = "";
        private String object = "";

        private QueryResult( String personID,
                             String personEN, String personForeign,
                             String sportID, String sportNameEN, String sportNameForeign){
            this.personID = personID;
            this.personEN = personEN;
            this.personForeign = personForeign;
            this.sportID = sportID;
            this.sportNameForeign = sportNameForeign;
            //temporary. will update by connecting to db
            this.verb = "play";
            //also temporary
            this.object = sportNameEN;
        }
    }

    public NAME_played_SPORT(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return
                "SELECT ?person ?personEN ?personLabel " +
                        " ?sport ?sportEN ?sportLabel " +
                        "		WHERE " +
                        "		{ " +
                        "           {?person wdt:P31 wd:Q5} UNION " + //is human
                        "           {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                        "			?person wdt:P641 ?sport . " + //played sport
                        "		    ?person wdt:P570 ?dateDeath . " +//is dead
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
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String sportID = SPARQLDocumentParserHelper.findValueByNodeName(head, "sport");
            // ~entity/id になってるから削る
            sportID = LessonGeneratorUtils.stripWikidataID(sportID);
            String sportNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportLabel");
            String sportNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportEN");

            QueryResult qr = new QueryResult(personID,
                    personEN, personForeign,
                    sportID, sportNameEN, sportNameForeign);

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
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            if (!qr.object.equals("")) {
                List<QuestionData> translateQuestion = createTranslationQuestion(qr);
                questionSet.add(translateQuestion);

                List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
                questionSet.add(spellingQuestion);
            }

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personForeign, null));

        }
    }

    //we want to read from the database and then create the questions
    @Override
    protected void accessDBWhenCreatingQuestions(){
        Set<String> sportIDs = new HashSet<>(queryResults.size());
        for (QueryResult qr : queryResults){
            sportIDs.add(qr.sportID);
        }
        OnResultListener onResultListener = new OnResultListener() {
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
        db.getSports(sportIDs, onResultListener);
    }

    private String NAME_played_SPORT_EN_correct(QueryResult qr){
        String verbObject = SportsHelper.getVerbObject(qr.verb, qr.object, SportsHelper.PAST);
        return qr.personEN + " " + verbObject + ".";
    }

    private String formatSentenceForeign(QueryResult qr){
        return qr.personForeign + "は" + qr.sportNameForeign + "をしました。";
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
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        String question = formatSentenceForeign(qr);
        List<String> choices = puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    //just for true/false
    private class SimpleQueryResult {
        String wikiDataID;
        String sportEN;
        String verb;

        SimpleQueryResult(String wikiDataID, String sportEN, String verb) {
            this.wikiDataID = wikiDataID;
            this.sportEN = sportEN;
            this.verb = verb;
        }
    }
    private List<SimpleQueryResult> popularSports(){
        List<SimpleQueryResult> list = new ArrayList<>(5);
        list.add(new SimpleQueryResult("Q2736", "soccer", "play"));
        list.add(new SimpleQueryResult("Q5369", "baseball", "play"));
        list.add(new SimpleQueryResult("Q847", "tennis", "play"));
        return list;
    }

    private String formatFalseAnswer(QueryResult qr, SimpleQueryResult sqr){
        //all the sports are 'play'
        String verbObject = SportsHelper.getVerbObject(sqr.verb, sqr.sportEN, SportsHelper.PAST);
        return qr.personEN + " " + verbObject + ".";
    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(3);
        String question = this.NAME_played_SPORT_EN_correct(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personForeign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);

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
        if (falseAnswers.size()>2) {
            falseAnswers = falseAnswers.subList(0, 2);
        }

        for (SimpleQueryResult falseSport : falseAnswers){
            question = this.formatFalseAnswer(qr, falseSport);
            data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(qr.personForeign);
            data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
            data.setQuestion(question);
            data.setChoices(null);
            data.setAnswer(Question_TrueFalse.TRUE_FALSE_QUESTION_FALSE);
            data.setAcceptableAnswers(null);

            questionDataList.add(data);
        }
        return questionDataList;
    }

    private List<QuestionData> createTranslationQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personForeign);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(qr.object);
        data.setChoices(null);
        data.setAnswer(qr.sportNameForeign);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>(1);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personForeign);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(qr.sportNameForeign);
        data.setChoices(null);
        data.setAnswer(qr.object);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);
        return questionDataList;
    }
}