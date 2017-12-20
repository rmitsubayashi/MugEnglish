package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_MultipleChoice;
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
import java.util.List;

public class NAME_drives_a_car_to_CITY extends Lesson {
    public static final String KEY = "NAME_drives_a_car_to_CITY";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String cityEN;
        private final String cityJP;

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
    }

    public NAME_drives_a_car_to_CITY(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personEN " +
                " ?cityEN ?cityLabel " +
                "WHERE " +
                "{" +
                "    ?person wdt:P19 ?city . " + //has a place of origin
                "    ?city wdt:P31/wdt:P279* wd:Q515 . " + //is a city
                "    ?person rdfs:label ?personEN . " +
                "    ?city rdfs:label ?cityEN . " +
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
            QueryResult qr = new QueryResult(personID, personEN, personJP, cityEN, cityJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);
            List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);
            List<QuestionData> multipleChoice = createMultipleChoiceQuestion(qr);
            questionSet.add(multipleChoice);
            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private String formatSentenceEN(QueryResult qr){
        return qr.personEN + " drives a car to " + qr.cityEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.cityJP +
                "まで車を運転します。";
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord drive = new VocabularyWord("","drive", "運転",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord car = new VocabularyWord("","car", "車",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord to = new VocabularyWord("","to", "~まで",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(drive);
        words.add(car);
        words.add(to);
        return words;
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<QuestionData> translateQuestion = createTranslateQuestion();
        List<QuestionData> spellingQuestion = createSpellingQuestion();

        List<List<QuestionData>> questionList = new ArrayList<>(2);
        questionList.add(translateQuestion);
        questionList.add(spellingQuestion);
        return questionList;

    }

    private List<QuestionData> createSpellingQuestion(){
        String question = "運転";
        String answer = "drive";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_Spelling.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createTranslateQuestion(){
        String question = "car";
        String answer = "車";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("drives");
        pieces.add("a car");
        pieces.add("to");
        pieces.add(qr.cityEN);
        return pieces;
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

        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " to " + qr.cityEN + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankInputAnswer(){
        return "drives a car";
    }

    private List<String> fillInBlankInputAcceptableAnswers(){
        List<String> answers = new ArrayList<>(3);
        answers.add("drives car");
        answers.add("drive a car");
        answers.add("drive car");
        return answers;
    }

    private List<QuestionData> createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer();
        List<String> acceptableAnswers = fillInBlankInputAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private String multipleChoiceQuestion(QueryResult qr){
        String sentence = "「to " + qr.cityEN + "」はどこに当てはまるでしょうか。";
        String sentence2 =  "(A) " + qr.personEN + " drives (B) a car (C).";
        return sentence + "\n" + sentence2;
    }

    private String multipleChoiceAnswer(){
        return "(C)";
    }

    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("(A)");
        choices.add("(B)");
        choices.add("(C)");
        return choices;
    }

    private List<QuestionData> createMultipleChoiceQuestion(QueryResult qr){
        String question = this.multipleChoiceQuestion(qr);
        String answer = multipleChoiceAnswer();
        List<String> choices = multipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> trueFalse = createTrueFalseQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(trueFalse);
        return questionSet;
    }

    private String trueFalseQuestion(){
        return "I drive a car";
    }

    private String trueFalseAnswer(){
        return Question_TrueFalse.getTrueFalseString(true);
    }

    private List<String> trueFalseAcceptableAnswer(){
        List<String> answers = new ArrayList<>(1);
         answers.add(Question_TrueFalse.getTrueFalseString(false));
         return answers;
    }

    private List<QuestionData> createTrueFalseQuestion(){
        String question = trueFalseQuestion();
        String answer = trueFalseAnswer();
        List<String> acceptableAnswers = trueFalseAcceptableAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }
}