package com.linnca.pelicann.lessongenerator.lessons;


import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.StringUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



public class COUNTRY_possessive_population_is_POPULATION extends Lesson {
    public static final String KEY = "COUNTRY_possessive_population_is_POPULATION";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String countryID;
        private final String countryEN;
        private final String countryJP;
        //highest population is 1.4 bil (ints can go up to 2~ bil)
        private final int population;
        private final String yearString;

        private QueryResult(
                String countryID,
                String countryEN,
                String countryJP,
                int population,
                String yearString
        ) {

            this.countryID = countryID;
            this.countryEN = countryEN;
            this.countryJP = countryJP;
            this.population = population;
            this.yearString = yearString;
        }
    }



    public COUNTRY_possessive_population_is_POPULATION(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){

        super(connector, db, listener);
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PLACE;
        super.questionSetsToPopulate = 3;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?country ?countryEN ?countryLabel " +
                " ?populationCt ?year " +
                "WHERE " +
                "{" +
                "    ?country wdt:P31 wd:Q6256 . " + //is a country
                "    ?country p:P1082 ?population . " + //has population record
                "    ?population ps:P1082 ?populationCt . " +
                "    OPTIONAL {?population pq:P585 ?year} . " +
                "    ?country rdfs:label ?countryEN . " +
                "    FILTER (LANG(?countryEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //JP label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?country) . " + //binding the ID of entity as ?country

                "} ";



    }



    @Override
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );

        int resultLength = allResults.getLength();

        //there may be more than one emergency phone number for a country
        //ie 110 & 119
        for (int i=0; i<resultLength; i++) {
            Node head = allResults.item(i);
            String countryID = SPARQLDocumentParserHelper.findValueByNodeName(head, "country");

            countryID = WikiDataEntity.getWikiDataIDFromReturnedResult(countryID);
            String countryEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryEN");
            String countryJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "countryLabel");
            String populationString = SPARQLDocumentParserHelper.findValueByNodeName(head, "populationCt");
            int population = Integer.parseInt(populationString);
            //optional
            String year = SPARQLDocumentParserHelper.findValueByNodeName(head, "year");
            year = StringUtils.getYearFromFullISO8601DateTime(year);
            QueryResult qr = new QueryResult(countryID, countryEN, countryJP, population, year);

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

            List<QuestionData> translateWordQuestion = createTranslateWordQuestion(qr);
            questionSet.add(translateWordQuestion);

            List<QuestionData> fillInBlankInput1Question = createFillInBlankInputQuestion1(qr);
            questionSet.add(fillInBlankInput1Question);

            List<QuestionData> fillInBlankInput2Question = createFillInBlankInputQuestion2(qr);
            questionSet.add(fillInBlankInput2Question);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.countryID, qr.countryJP, vocabularyWords));
        }
    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord population = new VocabularyWord("","population", "人口",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord country = new VocabularyWord("", qr.countryEN,qr.countryJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(population);
        words.add(country);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = GrammarRules.definiteArticleBeforeCountry(qr.countryEN) + "\'s population is " +
                StringUtils.convertIntToStringWithCommas(qr.population) + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.countryJP + "の人口は" + Integer.toString(qr.population) + "です。";
    }

    private FeedbackPair fillInBlankInputFeedback(QueryResult qr){
        String lowercaseCountry = qr.countryEN.toLowerCase();
        List<String> responses = new ArrayList<>();
        responses.add(lowercaseCountry);
        String feedback = "国の名前は大文字で始まります。\n" + lowercaseCountry + " → " + qr.countryEN;
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createTranslateWordQuestion(QueryResult qr){
        String question = qr.countryJP;
        String answer = qr.countryEN;
        FeedbackPair feedbackPair = fillInBlankInputFeedback(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>();
        feedbackPairs.add(feedbackPair);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryJP);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(feedbackPairs);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankInputQuestion1(QueryResult qr){
        String sentence1 = qr.countryJP + "の人口は" + Integer.toString(qr.population) + "です。";
        String sentence2 = GrammarRules.definiteArticleBeforeCountry(qr.countryEN) + "'s " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " is " + StringUtils.convertIntToStringWithCommas(qr.population) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        String sentence3 = "";
        if (!qr.yearString.equals("")){
            sentence3 = "\n(" + qr.yearString + ")";
        }
        return sentence1 + "\n\n" + sentence2 + sentence3;
    }

    private String fillInBlankInputAnswer1(){
        return "population";
    }

    private List<QuestionData> createFillInBlankInputQuestion1(QueryResult qr){
        String question = this.fillInBlankInputQuestion1(qr);

        String answer = fillInBlankInputAnswer1();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryJP);
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

    private String fillInBlankInputQuestion2(QueryResult qr){
        String sentence1 = GrammarRules.definiteArticleBeforeCountry(qr.countryEN) + "'s population is " +
                StringUtils.convertIntToWord(qr.population) + ".";
        sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
        String sentence2 = qr.countryJP + "の人口は" + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER + "です。";
        String sentence3 = "";
        if (!qr.yearString.equals("")){
            sentence3 = "\n(" + qr.yearString + ")";
        }
        return sentence1 + "\n\n" + sentence2 + sentence3;
    }



    private String fillInBlankInputAnswer2(QueryResult qr){
        return Integer.toString(qr.population);
    }

    private List<QuestionData> createFillInBlankInputQuestion2(QueryResult qr){
        String question = this.fillInBlankInputQuestion2(qr);
        String answer = fillInBlankInputAnswer2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.countryJP);
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

    private List<QuestionData> spellingSuggestiveQuestionGeneric(){
        String question = "人口";

        String answer = "population";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<String> questionIDs = new ArrayList<>();
        questionIDs.add(formatGenericQuestionID(KEY, 1));
        List<List<String>> questionSets = new ArrayList<>();
        questionSets.add(questionIDs);
        return questionSets;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = spellingSuggestiveQuestionGeneric();
        String id1 = formatGenericQuestionID(KEY, 1);
        questions.get(0).setId(id1);
        return questions;

    }

}