package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessongeneration.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.lessongeneration.TermAdjuster;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class TEAM_is_a_SPORT_team extends Lesson{
    public static final String KEY = "TEAM_is_a_SPORT_team";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String teamID;
        private final String teamEN;
        private final String teamJP;
        private final String sportEN;
        private final String sportJP;

        private QueryResult(
                String teamID,
                String teamEN,
                String teamJP,
                String sportEN,
                String sportJP)
        {
            this.teamID = teamID;
            this.teamEN = teamEN;
            this.teamJP = teamJP;
            this.sportEN = sportEN;
            this.sportJP = sportJP;
        }
    }

    public TEAM_is_a_SPORT_team(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 4;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_OTHER;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?team ?teamLabel ?teamEN " +
                " ?sportEN ?sportLabel " +
                " WHERE " +
                "{" +
                "    ?team wdt:P31/wdt:P279* wd:Q847017 . " + //is a sports club
                "    ?team wdt:P641 ?sport . " + //has a sport
                "    ?team rdfs:label ?teamEN . " +
                "    ?sport rdfs:label ?sportEN . " +
                "    FILTER (LANG(?teamEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?sportEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?team) . " + //binding the ID of entity as ?team
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
            String teamID = SPARQLDocumentParserHelper.findValueByNodeName(head, "team");
            teamID = WikiDataEntity.getWikiDataIDFromReturnedResult(teamID);
            String teamEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "teamEN");
            String teamJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "teamLabel");
            String sportEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportEN");
            sportEN = TermAdjuster.adjustSportsEN(sportEN);
            String sportJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "sportLabel");
            QueryResult qr = new QueryResult(teamID, teamEN, teamJP, sportEN, sportJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> trueFalse = createTrueFalseQuestion(qr);
            questionSet.add(trueFalse);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.teamID, qr.teamJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord team = new VocabularyWord("","team", "チーム",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord sport = new VocabularyWord("", qr.sportEN, qr.sportJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord teamName = new VocabularyWord("", qr.teamEN, qr.teamJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(team);
        words.add(sport);
        words.add(teamName);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        return qr.teamEN + " is " + GrammarRules.indefiniteArticleBeforeNoun(qr.sportEN) +
                " team.";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.teamJP + "は" + qr.sportJP + "チームです。";
    }

    //just for true/false
    private class SimpleQueryResult {
        final String wikiDataID;
        final String sportEN;
        final String sportJP;

        SimpleQueryResult(String wikiDataID, String sportEN, String sportJP) {
            this.wikiDataID = wikiDataID;
            this.sportEN = sportEN;
            this.sportJP = sportJP;
        }
    }
    private List<SimpleQueryResult> popularTeamSports(){
        List<SimpleQueryResult> list = new ArrayList<>(4);
        list.add(new SimpleQueryResult("Q2736", "soccer", "サッカー"));
        list.add(new SimpleQueryResult("Q5369", "baseball", "野球"));
        list.add(new SimpleQueryResult("Q1734", "volleyball", "バレーボール"));
        list.add(new SimpleQueryResult("Q5372", "basketball", "バスケットボール"));
        return list;
    }

    private String formatFalseAnswer(QueryResult qr, SimpleQueryResult sqr){
        return qr.teamEN + " is " +
                GrammarRules.indefiniteArticleBeforeNoun(sqr.sportEN) + " team.";
    }

    private FeedbackPair trueFalseFeedback(SimpleQueryResult sqr){
        //we are displaying sport names the user might not have encountered yet,
        // so explain what that sport is afterwards
        String response1 = QuestionSerializer.serializeTrueFalseAnswer(true);
        String response2 = QuestionSerializer.serializeTrueFalseAnswer(false);
        String feedback = sqr.sportEN + ": " + sqr.sportJP;
        List<String> responses = new ArrayList<>(2);
        responses.add(response1);
        responses.add(response2);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);

    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        //1 true and 1 false question
        List<QuestionData> questionDataList = new ArrayList<>(2);
        String question = this.formatSentenceEN(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(QuestionSerializer.serializeTrueFalseAnswer(true));
        data.setAcceptableAnswers(null);

        questionDataList.add(data);


        List<SimpleQueryResult> falseAnswers = popularTeamSports();
        Collections.shuffle(falseAnswers);
        //we don't want too many false answers
        //or the answers will most likely be false
        SimpleQueryResult falseSport = falseAnswers.get(0);

        question = this.formatFalseAnswer(qr, falseSport);
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(trueFalseFeedback(falseSport));
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        
        data.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(QuestionSerializer.serializeTrueFalseAnswer(false));
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        //just article
        String article = GrammarRules.indefiniteArticleBeforeNoun(qr.sportEN).replace(qr.sportEN, "");
        String sentence2 = qr.teamEN + " is " + article +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.sportEN + " team";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}