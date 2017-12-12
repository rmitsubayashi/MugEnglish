package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionResponseChecker;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_ChooseCorrectSpelling;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_Instructions;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NAME_speaks_LANGUAGE extends Lesson{
    public static final String KEY = "NAME_speaks_LANGUAGE";

    private final List<QueryResult> queryResults = new ArrayList<>();
    //there may be people with multiple spoken languages
    private final Map<String, List<String>> queryResultMap = new HashMap<>();

    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String languageEN;
        private final String languageJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String languageEN,
                String languageJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.languageEN = languageEN;
            this.languageJP = languageJP;
        }
    }

    public NAME_speaks_LANGUAGE(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 5;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personLabel ?personEN " +
                " ?language ?languageEN ?languageLabel " +
                "WHERE " +
                "{" +
                "    ?person wdt:P1412 ?language . " + //has a spoken/written language
                "    ?person rdfs:label ?personEN . " +
                "    ?language rdfs:label ?languageEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?languageEN) = '" +
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
            String languageID = SPARQLDocumentParserHelper.findValueByNodeName(head, "language");
            languageID = WikiDataEntity.getWikiDataIDFromReturnedResult(languageID);
            String languageEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "languageEN");
            String languageJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "languageLabel");

            QueryResult qr = new QueryResult(personID, personEN, personJP, languageEN, languageJP);
            queryResults.add(qr);

            if (queryResultMap.containsKey(personID)){
                List<String> value = queryResultMap.get(personID);
                value.add(languageID);
            } else {
                List<String> list = new ArrayList<>();
                list.add(languageID);
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
            List<QuestionData> fillInBlankMultipleChoice = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoice);

            List<QuestionData> chooseCorrectSpellingQuestion = createChooseCorrectSpellingQuestion(qr);
            questionSet.add(chooseCorrectSpellingQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord speak = new VocabularyWord("", "speak", "話す",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord language = new VocabularyWord("", qr.languageEN, qr.languageJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(speak);
        words.add(language);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.personEN + " speaks " + qr.languageEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.languageJP + "を話します。";
    }

    private class LanguageHelper {
        private String wikiDataID;
        private String nameEN;
        private String nameJP;

        public LanguageHelper(String wikiDataID, String nameEN, String nameJP) {
            this.wikiDataID = wikiDataID;
            this.nameEN = nameEN;
            this.nameJP = nameJP;

        }
    }
    private List<LanguageHelper> fillInBlankOptions(QueryResult qr){
        List<LanguageHelper> optionList = new LinkedList<>();
        optionList.add(new LanguageHelper("Q1860","English","英語"));
        optionList.add(new LanguageHelper("Q1321","Spanish","スペイン語"));
        optionList.add(new LanguageHelper("Q5146","Portuguese","ポルトガル語"));
        optionList.add(new LanguageHelper("Q7737","Russian","ロシア語"));
        optionList.add(new LanguageHelper("Q150","French","フランス語"));
        //remove if it is in the list so we don't choose it at first.
        //insert later
        List<String> languagesSpoken = queryResultMap.get(qr.personID);
        for (Iterator<LanguageHelper> iterator = optionList.iterator(); iterator.hasNext();){
            LanguageHelper option = iterator.next();
            if (languagesSpoken.contains(option.wikiDataID)){
                iterator.remove();
            }
        }
        Collections.shuffle(optionList);
        return optionList;
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        return qr.personEN + " speaks " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.languageEN;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback(List<LanguageHelper> falseCountries, String answer) {
        List<String> responses = new ArrayList<>(3);
        responses.add(answer);
        StringBuilder feedback = new StringBuilder("");
        for (LanguageHelper languageHelper : falseCountries){
            responses.add(languageHelper.nameEN);
            feedback.append(languageHelper.nameEN);
            feedback.append(" : ");
            feedback.append(languageHelper.nameJP);
            feedback.append("\n");
        }
        return new FeedbackPair(responses, feedback.toString(), FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<LanguageHelper> options = fillInBlankOptions(qr);
        while (options.size() > 2) {
            List<LanguageHelper> choices = new ArrayList<>(2);
            List<String> choiceStrings = new ArrayList<>(3);
            choiceStrings.add(answer);
            choices.add(options.get(0));
            choiceStrings.add(options.get(0).nameEN);
            options.remove(0);
            choices.add(options.get(0));
            choiceStrings.add(options.get(0).nameEN);
            options.remove(0);
            List<FeedbackPair> allFeedback = new ArrayList<>(1);
            allFeedback.add(fillInBlankMultipleChoiceFeedback(choices, answer));
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(qr.personJP);
            data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(question);
            data.setChoices(choiceStrings);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setFeedback(allFeedback);
            questionDataList.add(data);
        }
        return questionDataList;
    }

    private List<QuestionData> createChooseCorrectSpellingQuestion(QueryResult qr){
        String question = qr.languageJP;
        String answer = qr.languageEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_ChooseCorrectSpelling.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " speaks " +
                 Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.languageEN;
    }

    private FeedbackPair fillInBlankFeedback(QueryResult qr){
        String lowercaseCountry = qr.languageEN.toLowerCase();
        List<String> responses = new ArrayList<>();
        responses.add(lowercaseCountry);
        String feedback = "国の言語の名前は大文字で始まります。\n" + lowercaseCountry + " → " + qr.languageEN;
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
        feedbackPairs.add(fillInBlankFeedback(qr));
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(feedbackPairs);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> instructionsQuestion = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(instructionsQuestion);
        return questionSet;
    }

    //lets the user freely introduce themselves
    private String instructionQuestionQuestion(){
        return "あなたはどの言語を話せますか。";
    }

    private String instructionQuestionAnswer(){
        return "I speak " + QuestionResponseChecker.ANYTHING + ".";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        String acceptableAnswer1 = "I speaks " + QuestionResponseChecker.ANYTHING + ".";
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(acceptableAnswer1);
        return acceptableAnswers;

    }

    private List<QuestionData> createInstructionQuestion(){
        String question = instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        List<String> acceptableAnswers = instructionQuestionAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Instructions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}