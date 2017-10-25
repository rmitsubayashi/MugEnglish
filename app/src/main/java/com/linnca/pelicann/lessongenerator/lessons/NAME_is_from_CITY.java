package com.linnca.pelicann.lessongenerator.lessons;

import android.util.Log;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NAME_is_from_CITY extends Lesson{
    public static final String KEY = "NAME_is_from_CITY";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private final Map<String, QueryResult> queryResultMap = new HashMap<>();
    private class QueryResult {
        private final String personID;
        private final String personNameEN;
        private final String personNameJP;
        private final String cityEN;
        private final String cityJP;
        private final List<String> cityJPAlt = new ArrayList<>();

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameJP,
                String cityEN,
                String cityJP)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameJP = personNameJP;
            this.cityEN = cityEN;
            this.cityJP = cityJP;
        }

        void addAlt(String alt){
            cityJPAlt.add(alt);
        }
    }

    public NAME_is_from_CITY(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 1;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT DISTINCT ?personName ?personNameLabel ?personNameEN " +
                " ?cityEN ?cityLabel ?cityAltJP " +
                "WHERE " +
                "{" +
                "    {?personName wdt:P31 wd:Q5} UNION " + //is human
                "    {?personName wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?personName wdt:P19 ?city . " + //has a place of birth
                "    ?city wdt:P31/wdt:P279* wd:Q515 . " + //is a city
                "    OPTIONAL { ?city skos:altLabel ?cityAltJP . } " + //any alternative names for city
                "    ?personName rdfs:label ?personNameEN . " +
                "    ?city rdfs:label ?cityEN . " +
                "    FILTER (LANG(?cityAltJP) = '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "') . " +
                "    FILTER (LANG(?personNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?cityEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?personName) . " + //binding the ID of entity as ?person
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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "personName");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameEN");
            String personNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameLabel");

            String cityEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityEN");
            String cityJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityLabel");
            String cityAlt = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityAltJP");
            QueryResult qr = new QueryResult(personID, personNameEN, personNameJP, cityEN, cityJP);
            //we are assuming people weren't born in two places
            if (queryResultMap.containsKey(personID)){
                QueryResult value = queryResultMap.get(personID);
                if (cityAlt != null)
                    value.addAlt(cityAlt);
            } else {
                //only add this to the results once for each person
                queryResults.add(qr);
                if (cityAlt != null)
                    qr.addAlt(cityAlt);
                queryResultMap.put(personID, qr);
            }
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> translateQuestion = createTranslateQuestion(qr);
            questionSet.add(translateQuestion);

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
            questionSet.add(spellingQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personNameJP, null));
        }

    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("is from");
        pieces.add(qr.cityEN);
        return pieces;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personNameJP + "は" + qr.cityJP + "から来ました。";
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.personNameEN + " is from " + qr.cityEN + ".";
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
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        String question = qr.cityJP;
        String answer = qr.cityEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<String> translateAcceptableAnswers(QueryResult qr){
        List<String> acceptableAnswers = qr.cityJPAlt;
        Set<String> acceptableAnswerSet = new HashSet<>(acceptableAnswers);
        acceptableAnswers.add(qr.cityJP);
        for (String answer : acceptableAnswers){
            char lastLetter = answer.charAt(answer.length()-1);
            //we also accept city names like '四日市' -> '四日',
            //but it's better than not accepting them
            if (lastLetter == '町' || lastLetter == '村' || lastLetter == '市'){
                String altAnswer = answer.substring(0, answer.length()-1);
                acceptableAnswerSet.add(altAnswer);
            }
            //we can't do the other way around because we don't know whether
            //it's a 市, 町, or 村
        }

        return new ArrayList<>(acceptableAnswerSet);
    }

    private List<QuestionData> createTranslateQuestion(QueryResult qr){
        String question = qr.cityEN;
        String answer = qr.cityJP;
        List<String> acceptableAnswers = translateAcceptableAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.personNameEN + " is " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " " + qr.cityEN + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String fillInBlankAnswer(){
        return "from";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    
}