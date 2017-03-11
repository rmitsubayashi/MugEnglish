package com.example.ryomi.mugenglish.questiongenerator.themes;

import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionDataWrapper;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MOVIE_won_AWARD_in_YEAR extends Theme {
    //クエリーするときの名前
    private final String movieNamePH = "movieName";
    private final String movieNameForeignPH = "movieNameForeign";
    private final String movieNameENPH = "movieNameEN";
    private final String awardForeignPH = "awardForeign";
    private final String awardENPH = "awardEN";
    private final String pointInTimePH = "pointInTime";
    private final String endDatePH = "endDate";

    private List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private String movieID;
        private String awardEN;
        private String awardForeign;
        private String movieNameEN;
        private String movieNameForeign;
        private String pointInTime;

        private QueryResult(
                String movieID,
                String awardEN, String awardForeign,
                String movieNameEN, String movieNameForeign,
                String pointInTime)
        {
            this.movieID = movieID;
            this.awardEN = awardEN;
            this.awardForeign = awardForeign;
            this.movieNameEN = movieNameEN;
            this.movieNameForeign = movieNameForeign;
            this.pointInTime = pointInTime;
        }
    }

    public MOVIE_won_AWARD_in_YEAR(WikiBaseEndpointConnector connector, ThemeData data){
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;/*
		super.backupIDsOfTopics.add("Q5284");//Bill Gates
		super.backupIDsOfTopics.add("Q8027");//Elon Musk*/
    }

    @Override
    protected String getSPARQLQuery(){

        //find the education institution, start and end date of one individual
        //ex ビル・ゲーツ | ハーバード・カレッジ | Bill Gates | Harvard College | 1973-01-01T00:00:00Z | 1975-01-01T00:00:00Z
        return 	"SELECT ?" + movieNamePH + " ?" + movieNameForeignPH +  " ?" + movieNameENPH +
                " ?" + awardForeignPH + " ?" + awardENPH +
                " ?" + pointInTimePH + " " +
                "WHERE" +
                "{" +
                "    ?" + movieNamePH + " wdt:P31 wd:Q11424 . " + //is a movie
                "    ?" + movieNamePH + " p:P166 ?awardStatement . " + //this movie has an award
                "    ?awardStatement ps:P166 ?award . " + //grabbing name of award
                "    ?awardStatement pq:P585 ?" + pointInTimePH + " . " + //grabbing date
                "	 SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //these labels should be in the foreign language
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " +  //fallback language is English
                "                           ?" + movieNamePH + " rdfs:label ?" + movieNameForeignPH + ". " + //grabbing foreign label of movie
                "                           ?award rdfs:label ?" + awardForeignPH + " . }" +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //English labels
                "                           ?" + movieNamePH + " rdfs:label ?" + movieNameENPH + " . " +
                "                           ?award rdfs:label ?" + awardENPH + " . } " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "'} " + //everything else is in English

                "    BIND (wd:%s as ?" + movieNamePH + ") . " +
                "} ";

    }

    @Override
    protected void processResultsIntoClassWrappers(Document document){
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String movieID = SPARQLDocumentParserHelper.findValueByNodeName(head, movieNamePH);
            movieID = QGUtils.stripWikidataID(movieID);

            String awardEN = SPARQLDocumentParserHelper.findValueByNodeName(head, awardENPH);
            String awardForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, awardForeignPH);

            String pointInTimeTimeValue = SPARQLDocumentParserHelper.findValueByNodeName(head, pointInTimePH);

            String pointInTime = this.getYearFromFullISO8601DateTime(pointInTimeTimeValue);

            String movieNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, movieNameENPH);
            String movieNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, movieNameForeignPH);

            QueryResult qr = new QueryResult(
                    movieID,
                    awardEN, awardForeign,
                    movieNameEN, movieNameForeign,
                    pointInTime);

            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void saveResultTopics(){
        for (QueryResult qr : queryResults){
            topics.add(qr.movieNameForeign);
        }
    }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<QuestionData> questionSet = new ArrayList<>();

            QuestionData trueFalseQuestionTrue = createTrueFalseQuestion(qr,true);
            QuestionData trueFalseQuestionFalse = createTrueFalseQuestion(qr,false);
            int i = new Random().nextInt();
            if (i%2 == 0) {
                questionSet.add(trueFalseQuestionTrue);
                questionSet.add(trueFalseQuestionFalse);
            } else {
                questionSet.add(trueFalseQuestionFalse);
                questionSet.add(trueFalseQuestionTrue);
            }

            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet,qr.movieID));
        }
    }

    private String getYearFromFullISO8601DateTime(String fullISO8601DateTime){
        //we can assume the first four letters are the date
        // ex: 1990-01-01T00:00:00Z
        return fullISO8601DateTime.substring(0,4);
    }


    private String MOVIE_won_AWARD_in_YEAR_EN_correct(QueryResult qr){
        //'the' をつけるかどうか
        String award = GrammarRules.definiteArticleBeforeAward(qr.awardEN);

        String sentence = qr.movieNameEN + " won " + award +
                " in " + qr.pointInTime + ".";
        //映画の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    //correct sentence in foreign language (need only one?)
    private String formatSentenceForeign(QueryResult qr){
        return qr.movieNameForeign + "は" + qr.pointInTime + "年に" + qr.awardForeign + "を受賞しました。";
    }

    private String trueFalseQuestion(QueryResult qr, boolean correct){
        Integer year = Integer.parseInt(qr.pointInTime);
        //wrong answer will be 50 ~ 100 years before the award date.
        //this should be able to decide that this is the wrong answer
        Random random = new Random();
        int fiftyToHundred = random.nextInt() % 50;
        fiftyToHundred += 50;
        if (!correct)
            year -= fiftyToHundred;
        String yearString = year.toString();

        String award = GrammarRules.definiteArticleBeforeAward(qr.awardEN);
        String sentence = qr.movieNameEN + " won " + award +
                " in " + yearString + ".";
        //映画の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private QuestionData createTrueFalseQuestion(QueryResult qr, boolean correct){
        String question = trueFalseQuestion(qr, correct);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.movieNameForeign);
        data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(correct ? QuestionUtils.TRUE_FALSE_QUESTION_TRUE : QuestionUtils.TRUE_FALSE_QUESTION_FALSE);
        data.setAcceptableAnswers(null);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private String fillInBlankQuestion(QueryResult qr){
        //'the' をつけるかどうか
        String award = GrammarRules.definiteArticleBeforeAward(qr.awardEN);

        String sentence = qr.movieNameEN + " " + QuestionUtils.FILL_IN_BLANK_MULTIPLE_CHOICE+ " " + award +
                " in " + qr.pointInTime + ".";
        //映画の名前は絶対大文字で始まるから、わざわざやらなくていいはず？
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private List<String> fillInBlankChoices(){
        List<String> win = new ArrayList<>();
        win.add("win");
        win.add("wan");
        win.add("wun");
        win.add("wen");
        Collections.shuffle(win);

        List<String> choices = new ArrayList<>();
        choices.add(win.get(0));
        choices.add(win.get(1));

        return choices;
    }

    private String fillInBlankAnswer(){
        return "won";
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<String> choices = fillInBlankChoices();
        choices.add(answer);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.movieNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setVocabulary(null);

        return data;
    }
}
