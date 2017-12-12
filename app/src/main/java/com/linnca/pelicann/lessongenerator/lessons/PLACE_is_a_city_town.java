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
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



public class PLACE_is_a_city_town extends Lesson {
    public static final String KEY = "PLACE_is_a_city_town";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String placeID;
        private final String placeEN;
        private final String placeJP;
        private final String townCityEN;
        private final String townCityJP;

        private QueryResult(
                String placeID,
                String placeEN,
                String placeJP,
                boolean isTown
        ) {

            this.placeID = placeID;
            this.placeEN = placeEN;
            this.placeJP = placeJP;
            this.townCityEN = isTown ? "town" : "city";
            this.townCityJP = isTown ? "町" : "都市";
        }
    }



    public PLACE_is_a_city_town(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){

        super(connector, db, listener);
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PLACE;
        super.questionSetsToPopulate = 3;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?place ?placeEN ?placeLabel ?instance " +
                " WHERE " +
                "{" +
                "    {?place wdt:P31/wdt:P279* wd:Q3957 . " +
                "    BIND ('town' as ?instance)} UNION " + //is a town
                "    {?place wdt:P31/wdt:P279* wd:Q515 . " +
                "    BIND ('city' as ?instance)} . " + //or city
                "    ?place rdfs:label ?placeEN . " +
                "    FILTER (LANG(?placeEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //JP label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?place) . " + //binding the ID of entity as place

                "} ";



    }

    @Override

    protected synchronized void processResultsIntoClassWrappers(Document document) {

        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );

        int resultLength = allResults.getLength();

        for (int i=0; i<resultLength; i++) {
            Node head = allResults.item(i);
            String placeID = SPARQLDocumentParserHelper.findValueByNodeName(head, "place");

            placeID = WikiDataEntity.getWikiDataIDFromReturnedResult(placeID);
            String placeEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "placeEN");
            String placeJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "placeLabel");
            String cityOrTownString = SPARQLDocumentParserHelper.findValueByNodeName(head, "instance");
            boolean isTown = cityOrTownString.equals("town");
            QueryResult qr = new QueryResult(placeID, placeEN, placeJP, isTown);
            queryResults.add(qr);
        }
    }

    @Override

    protected synchronized int getQueryResultCt(){
        return queryResults.size();
    }

    protected synchronized void createQuestionsFromResults(){

        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.placeID, qr.placeJP, vocabularyWords));
        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord townCity = new VocabularyWord("", qr.townCityEN, qr.townCityJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(townCity);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        return qr.placeEN + " is a " + qr.townCityEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.placeJP + "は" + qr.townCityJP + "です。";
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = qr.placeEN + " is a " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("town");
        choices.add("city");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        List<String> choices = fillInBlankMultipleChoiceChoices();
        String answer = fillInBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;

    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = qr.placeJP + "は" + qr.townCityJP + "です";
        String sentence2 = qr.placeEN + " is a " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.townCityEN;
    }

    private List<QuestionData> createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);

        String answer = fillInBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;

    }

    private List<String> translateAcceptableAnswersGeneric(){
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add("街");
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "town";
        String answer = "町";
        List<String> acceptableAnswers = translateAcceptableAnswersGeneric();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<String> translateAcceptableAnswersGeneric2(){
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add("市");
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestionGeneric2(){
        String question = "city";
        String answer = "都市";
        List<String> acceptableAnswers = translateAcceptableAnswersGeneric2();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> questionSet = new ArrayList<>(2);
        List<QuestionData> translateQuestion = createTranslateQuestionGeneric();
        questionSet.add(translateQuestion);
        List<QuestionData> translateQuestion2 = createTranslateQuestionGeneric2();
        questionSet.add(translateQuestion2);

        return questionSet;
    }

}