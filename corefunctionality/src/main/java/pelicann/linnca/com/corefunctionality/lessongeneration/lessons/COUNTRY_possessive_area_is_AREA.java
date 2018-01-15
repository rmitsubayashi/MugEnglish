package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;


import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessongeneration.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.lessongeneration.StringUtils;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;


import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


public class COUNTRY_possessive_area_is_AREA extends Lesson {
    public static final String KEY = "COUNTRY_possessive_area_is_AREA";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String countryID;
        private final String countryEN;
        private final String countryJP;
        //highest area is 1.4 bil (ints can go up to 2~ bil)
        private final int area;

        private QueryResult(
                String countryID,
                String countryEN,
                String countryJP,
                int area
        ) {

            this.countryID = countryID;
            this.countryEN = countryEN;
            this.countryJP = countryJP;
            this.area = area;
        }
    }



    public COUNTRY_possessive_area_is_AREA(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){

        super(connector, db, listener);
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PLACE;
        super.questionSetsToPopulate = 4;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?country ?countryEN ?countryLabel " +
                " ?area " +
                "WHERE " +
                "{" +
                "    ?country wdt:P31 wd:Q6256 . " + //is a country
                "    ?country wdt:P2046 ?area . " + //has area
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

    protected synchronized void processResultsIntoClassWrappers(Document document) {

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
            String areaString = SPARQLDocumentParserHelper.findValueByNodeName(head, "area");
            //since most of the area has decimal, convert it to a double first
            int area = (int)Double.parseDouble(areaString);
            QueryResult qr = new QueryResult(countryID, countryEN, countryJP, area);

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

            List<QuestionData> translateWordQuestion = createTranslateWordQuestion(qr);
            questionSet.add(translateWordQuestion);

            List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.countryID, qr.countryJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord area = new VocabularyWord("","area", "面積",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord country = new VocabularyWord("", qr.countryEN,qr.countryJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(area);
        words.add(country);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = GrammarRules.definiteArticleBeforeCountry(qr.countryEN) + "\'s area is " +
                StringUtils.convertIntToStringWithCommas(qr.area) + " km².";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.countryJP + "の面積は" + Integer.toString(qr.area) + " km²です。";
    }

    private List<QuestionData> spellingQuestionGeneric(){
        String question = "面積";
        String answer = "area";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;

    }

    private List<QuestionData> translateQuestionGeneric(){
        String question = "面積";
        String answer = "area";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;

    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> questionSet =new ArrayList<>(1);
        List<QuestionData> spellingQuestion = spellingQuestionGeneric();
        questionSet.add(spellingQuestion);
        List<QuestionData> translate = translateQuestionGeneric();
        questionSet.add(translate);
        return questionSet;

    }

    private FeedbackPair translateFeedback(QueryResult qr){
        String lowercaseCountry = qr.countryEN.toLowerCase();
        String definiteArticleLowercaseCountry =
                GrammarRules.definiteArticleBeforeCountry(qr.countryEN)
                .toLowerCase();
        List<String> responses = new ArrayList<>(2);
        responses.add(lowercaseCountry);
        if (!lowercaseCountry.equals(definiteArticleLowercaseCountry))
            responses.add(definiteArticleLowercaseCountry);
        String feedback = "国の名前は大文字で始まります。\n" + definiteArticleLowercaseCountry + " → " +
                GrammarRules.definiteArticleBeforeCountry(qr.countryEN);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createTranslateWordQuestion(QueryResult qr){
        String question = qr.countryJP;
        String answer = GrammarRules.definiteArticleBeforeCountry(qr.countryEN);
        List<String> acceptableAnswers = new ArrayList<>(1);
        if (!answer.equals(qr.countryEN)){
            acceptableAnswers.add(qr.countryEN);
        }
        FeedbackPair feedbackPair = translateFeedback(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>();
        feedbackPairs.add(feedbackPair);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(feedbackPairs);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String sentence1 = GrammarRules.definiteArticleBeforeCountry(qr.countryEN) + "'s area is " +
                StringUtils.convertIntToWord(qr.area) + " km².";
        String sentence2 = qr.countryJP + "の面積は" + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER + " km²です。";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence1) + "\n\n" + sentence2;
    }



    private String fillInBlankInputAnswer(QueryResult qr){
        return Integer.toString(qr.area);
    }

    private List<QuestionData> createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);
        String answer = fillInBlankInputAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;
    }

}