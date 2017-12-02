package com.linnca.pelicann.lessongenerator.lessons;


import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



public class NAME_possessive_mother_father_is_NAME2 extends Lesson {
    public static final String KEY = "NAME_possessive_mother_father_is_NAME2";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String parentNameEN;
        private final String parentNameJP;
        private final String parentTypeEN;
        private final String parentTypeJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String parentNameEN,
                String parentNameJP,
                boolean isFather
        ) {

            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.parentNameEN = parentNameEN;
            this.parentNameJP = parentNameJP;
            this.parentTypeEN = isFather ? "father" : "mother";
            this.parentTypeJP = isFather ? "父" : "母";
        }
    }



    public NAME_possessive_mother_father_is_NAME2(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.questionSetsToPopulate = 3;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personEN ?personLabel " +
                " ?parentEN ?parentLabel ?instance " +
                " WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    {?person wdt:P22 ?parent . " +
                "    BIND ('father' as ?instance) ." +
                "    ?parent rdfs:label ?parentEN " + //this NEEDS to be in the union
                "    } UNION " + //is a father
                "    {?person wdt:P25 ?parent . " +
                "    BIND ('mother' as ?instance) . " +
                "    ?parent rdfs:label ?parentEN " + //this NEEDS to be in the union
                "    } . " + //or mother
                "    ?person rdfs:label ?personEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?parentEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //JP label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as person

                "} ";

    }

    @Override

    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );

        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++) {
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");

            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String parentNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "parentEN");
            String parentNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "parentLabel");
            String fatherOrMotherString = SPARQLDocumentParserHelper.findValueByNodeName(head, "instance");
            boolean isFather = fatherOrMotherString.equals("father");
            QueryResult qr = new QueryResult(personID, personEN, personJP, parentNameEN, parentNameJP, isFather);

            queryResults.add(qr);

        }

    }

    @Override

    protected int getQueryResultCt(){
        return queryResults.size();
    }

    protected void createQuestionsFromResults(){

        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord parentType = new VocabularyWord("",qr.parentTypeEN, qr.parentTypeJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(parentType);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        return qr.personEN + "\'s " + qr.parentTypeEN + " is " +
                qr.parentNameEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.parentNameJP + "の" + qr.parentTypeJP + "は" +
                qr.parentNameJP + "です。";
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        return  Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.parentTypeEN + " is " + qr.parentNameEN + ".";
    }

    private List<String> fillInBlankMultipleChoiceChoices(QueryResult qr){
        List<String> choices = new ArrayList<>(4);
        choices.add(qr.personEN + "'s");
        choices.add(qr.personEN);
        choices.add(qr.personEN + "s'");
        choices.add(qr.personEN + "s");
        return choices;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.personEN + "'s";
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        List<String> choices = fillInBlankMultipleChoiceChoices(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.personJP);
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

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("'s");
        pieces.add(qr.parentTypeEN);
        pieces.add("is");
        pieces.add(qr.parentNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePieces(qr));
    }

    private List<String> puzzlePiecesAcceptableAnswers(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.parentNameEN);
        pieces.add("is");
        pieces.add(qr.personEN);
        pieces.add("'s");
        pieces.add(qr.parentTypeEN);
        String answer = Question_SentencePuzzle.formatAnswer(pieces);
        List<String> answers = new ArrayList<>(1);
        answers.add(answer);
        return answers;
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        List<String> acceptableAnswers = puzzlePiecesAcceptableAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "father";
        String answer = "父";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric2(){
        String question = "mother";
        String answer = "母";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


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