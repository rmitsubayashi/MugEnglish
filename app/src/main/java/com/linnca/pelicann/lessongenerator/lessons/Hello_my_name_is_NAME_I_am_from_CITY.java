package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.userinterests.WikiDataEntity;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Hello_my_name_is_NAME_I_am_from_CITY extends Lesson{
    public static final String KEY = "Hello_my_name_is_NAME_I_am_from_CITY";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private final Map<String, QueryResult> queryResultMap = new HashMap<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String cityEN;
        private final String cityJP;
        private final List<String> cityJPAlt = new ArrayList<>();

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String cityEN,
                String cityJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.cityEN = cityEN;
            this.cityJP = cityJP;
        }

        void addAlt(String alt){
            cityJPAlt.add(alt);
        }
    }

    public Hello_my_name_is_NAME_I_am_from_CITY(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 1;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT DISTINCT ?person ?personLabel ?personEN " +
                " ?cityEN ?cityLabel ?cityAltJP " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?person wdt:P19 ?city . " + //has a place of birth
                "    ?city wdt:P31/wdt:P279* wd:Q515 . " + //is a city
                "    OPTIONAL { ?city skos:altLabel ?cityAltJP . } " + //any alternative names for city
                "    ?person rdfs:label ?personEN . " +
                "    ?city rdfs:label ?cityEN . " +
                "    FILTER (LANG(?cityAltJP) = '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "') . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?cityEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?person
                "} ";

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

            String cityEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityEN");
            String cityJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityLabel");
            String cityAlt = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityAltJP");
            QueryResult qr = new QueryResult(personID, personEN, personJP, cityEN, cityJP);
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
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
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

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, null));
        }

    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add("hello");
        pieces.add("my name");
        pieces.add("is");
        pieces.add(qr.personEN);
        pieces.add("I");
        pieces.add("am");
        pieces.add("from");
        pieces.add(qr.cityEN);
        return pieces;
    }

    private String formatSentenceJP(QueryResult qr){
        return "こんにちは、私の名前は" + qr.personJP + "です。私は" + qr.cityJP + "から来ました。";
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence1 = "Hello my name is " + qr.personEN + ".";
        String sentence2 = "I am from " + qr.cityEN + ".";
        return sentence1 +"\n"+ sentence2;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
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
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_Spelling.QUESTION_TYPE);
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
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = "I am " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
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
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    
}