package com.linnca.pelicann.lessongenerator.lessons;

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
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

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

    public TEAM_is_a_SPORT_team(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsToPopulate = 4;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_OTHER;
        super.lessonKey = KEY;

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
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String teamID = SPARQLDocumentParserHelper.findValueByNodeName(head, "team");
            teamID = LessonGeneratorUtils.stripWikidataID(teamID);
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
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.teamID, qr.teamJP, vocabularyWords));
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

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.teamEN);
        pieces.add("is");
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.sportEN));
        pieces.add("team");
        return pieces;
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
        data.setTopic(qr.teamJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        //just article
        String article = GrammarRules.indefiniteArticleBeforeNoun(qr.sportEN).replace(qr.sportEN, "");
        String sentence2 = qr.teamEN + " is " + article +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
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
        data.setTopic(qr.teamJP);
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