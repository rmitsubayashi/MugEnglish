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
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_possessive_first_last_name_is_NAME extends Lesson{
    public static final String KEY = "NAME_possessive_first_last_name_is_NAME";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String firstNameEN;
        private final String firstNameJP;
        private final String lastNameEN;
        private final String lastNameJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String firstNameEN,
                String firstNameJP,
                String lastNameEN,
                String lastNameJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.firstNameEN = firstNameEN;
            this.firstNameJP = firstNameJP;
            this.lastNameEN = lastNameEN;
            this.lastNameJP = lastNameJP;
        }
    }

    public NAME_possessive_first_last_name_is_NAME(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 5;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?person ?personLabel ?personEN " +
                " ?firstNameEN ?firstNameLabel " +
                " ?lastNameEN ?lastNameLabel " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?person wdt:P735 ?firstName . " + //has an first name
                "    ?person wdt:P734 ?lastName . " + //has an last name
                "    ?person rdfs:label ?personEN . " +
                "    ?firstName rdfs:label ?firstNameEN . " +
                "    ?lastName rdfs:label ?lastNameEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?firstNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?lastNameEN) = '" +
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
            String firstNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "firstNameEN");
            String firstNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "firstNameLabel");
            String lastNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "lastNameEN");
            String lastNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "lastNameLabel");

            QueryResult qr = new QueryResult(personID, personEN, personJP,
                    firstNameEN, firstNameJP,
                    lastNameEN, lastNameJP);
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

            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord firstName = new VocabularyWord("","first name", "名",
                formatFirstNameSentenceEN(qr), formatFirstNameSentenceJP(qr), KEY);
        VocabularyWord lastName = new VocabularyWord("","last name", "姓",
                formatLastNameSentenceEN(qr), formatLastNameSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(firstName);
        words.add(lastName);
        return words;
    }

    private String formatFirstNameSentenceJP(QueryResult qr){
        return qr.personJP + "の名は" + qr.firstNameJP + "です。";
    }

    private String formatLastNameSentenceJP(QueryResult qr){
        return qr.personJP + "の姓は" + qr.lastNameJP + "です。";
    }

    private String formatFirstNameSentenceEN(QueryResult qr){
        return qr.personEN + "\'s first name is " + qr.firstNameEN + ".";
    }

    private String formatLastNameSentenceEN(QueryResult qr){
        return qr.personEN + "\'s last name is " + qr.lastNameEN + ".";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePiecesFirstName(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("'s");
        pieces.add("first");
        pieces.add("name");
        pieces.add("is");
        pieces.add(qr.firstNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswerFirstName(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePiecesFirstName(qr));
    }

    private List<String> acceptableAnswersFirstName(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.firstNameEN);
        pieces.add("is");
        pieces.add(qr.personEN);
        pieces.add("'s");
        pieces.add("first");
        pieces.add("name");
        String answer = Question_SentencePuzzle.formatAnswer(pieces);
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(answer);
        return acceptableAnswers;
    }

    private List<String> puzzlePiecesLastName(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("'s");
        pieces.add("last");
        pieces.add("name");
        pieces.add("is");
        pieces.add(qr.lastNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswerLastName(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePiecesLastName(qr));
    }

    private List<String> acceptableAnswersLastName(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.lastNameEN);
        pieces.add("is");
        pieces.add(qr.personEN);
        pieces.add("'s");
        pieces.add("last");
        pieces.add("name");
        String answer = Question_SentencePuzzle.formatAnswer(pieces);
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(answer);
        return acceptableAnswers;
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> dataList = new ArrayList<>();
        String question = this.formatFirstNameSentenceJP(qr);
        List<String> choices = this.puzzlePiecesFirstName(qr);
        List<String> acceptableAnswers = acceptableAnswersFirstName(qr);
        String answer = puzzlePiecesAnswerFirstName(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        dataList.add(data);

        question = this.formatLastNameSentenceJP(qr);
        choices = this.puzzlePiecesLastName(qr);
        answer = puzzlePiecesAnswerLastName(qr);
        acceptableAnswers = acceptableAnswersLastName(qr);
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        dataList.add(data);
        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestionFirstName(QueryResult qr){
        String sentence = formatFirstNameSentenceJP(qr);
        String sentence2 = qr.personEN + "'s " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " is " + qr.firstNameEN + ".";
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswerFirstName(){
        return "first name";
    }

    private String fillInBlankMultipleChoiceQuestionLastName(QueryResult qr){
        String sentence = formatFirstNameSentenceJP(qr);
        String sentence2 = qr.personEN + "'s " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " is " + qr.lastNameEN + ".";
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswerLastName(){
        return "last name";
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("last name");
        choices.add("first name");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();
        String question = this.fillInBlankMultipleChoiceQuestionFirstName(qr);
        String answer = fillInBlankMultipleChoiceAnswerFirstName();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        question = this.fillInBlankMultipleChoiceQuestionLastName(qr);
        answer = fillInBlankMultipleChoiceAnswerLastName();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankQuestionLastName(QueryResult qr){
        String sentence = qr.personEN + "'s " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " is " + qr.lastNameEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswerLastName(){
        return "last name";
    }

    private String fillInBlankQuestionFirstName(QueryResult qr){
        String sentence = qr.personEN + "'s " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " is " + qr.firstNameEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswerFirstName(){
        return "first name";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();
        String question = this.fillInBlankQuestionFirstName(qr);
        String answer = fillInBlankAnswerFirstName();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        question = this.fillInBlankQuestionLastName(qr);
        answer = fillInBlankAnswerLastName();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }


}