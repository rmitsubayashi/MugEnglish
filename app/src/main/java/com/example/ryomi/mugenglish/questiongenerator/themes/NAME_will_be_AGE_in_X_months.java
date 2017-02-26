package com.example.ryomi.mugenglish.questiongenerator.themes;

import android.util.Log;

import com.example.ryomi.mugenglish.connectors.EndpointConnectorReturnsXML;
import com.example.ryomi.mugenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionData;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.questiongenerator.GrammarRules;
import com.example.ryomi.mugenglish.questiongenerator.QGUtils;
import com.example.ryomi.mugenglish.questiongenerator.QuestionUtils;
import com.example.ryomi.mugenglish.questiongenerator.Theme;

import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.Months;
import org.joda.time.Years;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NAME_will_be_AGE_in_X_months extends Theme {
    //placeholders
    private final String personNamePH = "personName";
    private final String personNameForeignPH = "personNameForeign";
    private final String personNameENPH = "personNameEN";
    private final String birthdayPH = "birthday";

    private List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private String personID;
        private String personNameEN;
        private String personNameForeign;
        private int newAge;
        int monthsUntil;
        String birthdayEN;
        String birthdayForeign;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameForeign,
                String dateString) {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameForeign = personNameForeign;
            calculateBirthday(dateString);
        }

        private void calculateBirthday(String dateString) {
            Log.d("TAG", "Date: " + dateString);
            //format is YYYY-MM-DDT... so ISO8061
            dateString = dateString.substring(0, 10);
            LocalDate birthday = LocalDate.parse(dateString);
            LocalDate now = new LocalDate();
            Years age = Years.yearsBetween(birthday, now);
            newAge = age.getYears() + 1;

            MonthDay birthdayMonthDay = MonthDay.parse(dateString);
            int year = now.getYear();
            LocalDate nextBirthday = birthdayMonthDay.toLocalDate(year);
            if (nextBirthday.isBefore(now)) {
                nextBirthday = nextBirthday.plusYears(1);
            }

            monthsUntil = Months.monthsBetween(now, nextBirthday).getMonths();

            birthdayEN = birthday.toString("MMMMMMMMMM d", Locale.US);
            birthdayForeign = birthday.toString("M月d日", Locale.JAPAN);
        }
    }

    public NAME_will_be_AGE_in_X_months(EndpointConnectorReturnsXML connector, ThemeData data) {
        super(connector, data);
        super.questionSetsLeftToPopulate = 2;

    }

    protected String getSPARQLQuery() {
        //find person name and blood type
        return "SELECT ?" + personNamePH + " ?" + personNameForeignPH + " ?" + personNameENPH +
                " ?" + birthdayPH + "Label " +
                "WHERE " +
                "{" +
                "    ?" + personNamePH + " wdt:P31 wd:Q5 . " + //is human
                "    ?" + personNamePH + " wdt:P569 ?" + birthdayPH + " . " + //has a birthday
                "    FILTER NOT EXISTS { ?" + personNamePH + " wdt:P570 ?dateDeath } . " + //not dead
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //foreign label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "' . " + //fallback language is English
                "                           ?" + personNamePH + " rdfs:label ?" + personNameForeignPH + " } . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" + WikiBaseEndpointConnector.ENGLISH + "' . " +
                "                           ?" + personNamePH + " rdfs:label ?" + personNameENPH +
                "                           } . " + //English translation
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.ENGLISH + "' } . " + //the rest in English
                "    BIND (wd:%s as ?" + personNamePH + ") . " + //binding the ID of entity as ?person
                "} ";

    }

    protected void processResultsIntoClassWrappers() {
        Document document = super.documentOfTopics;
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i = 0; i < resultLength; i++) {
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, personNamePH);
            personID = QGUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameENPH);
            String personNameForeign = SPARQLDocumentParserHelper.findValueByNodeName(head, personNameForeignPH);
            String birthday = SPARQLDocumentParserHelper.findValueByNodeName(head, birthdayPH + "Label");

            QueryResult qr = new QueryResult(personID, personNameEN, personNameForeign, birthday);
            queryResults.add(qr);
        }
    }

    @Override
    protected void saveResultTopics() {
        for (QueryResult qr : queryResults) {
            topics.add(qr.personNameForeign);
        }
    }


    protected void createQuestionsFromResults() {
        for (QueryResult qr : queryResults) {
            List<QuestionData> questionSet = new ArrayList<>();
            QuestionData sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            QuestionData fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);
            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID));
        }

    }

    private String monthsUntilHelper(int monthsUntil) {
        String result;
        if (monthsUntil == 1) {
            result = "one month";
        } else {
            //0 will be 0 months
            result = QGUtils.convertIntToWord(monthsUntil) + " months";
        }

        return result;
    }

    private String NAME_will_be_AGE_in_X_MONTHS_EN_correct(QueryResult qr) {
        String sentence = qr.personNameEN + " will be " + qr.newAge + " in " + monthsUntilHelper(qr.monthsUntil) + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceForeign(QueryResult qr) {
        return qr.personNameForeign + "は" + qr.monthsUntil + "か月後、" + qr.newAge + "歳になります。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr) {
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("will");
        pieces.add("be");
        pieces.add(Integer.toString(qr.newAge));
        pieces.add("in");
        pieces.add(monthsUntilHelper(qr.monthsUntil));
        return pieces;
    }

    private List<String> puzzlePiecesAlternate(QueryResult qr) {
        List<String> pieces = new ArrayList<>();
        pieces.add("in");
        pieces.add(monthsUntilHelper(qr.monthsUntil));
        pieces.add(qr.personNameEN);
        pieces.add("will");
        pieces.add("be");
        pieces.add(Integer.toString(qr.newAge));
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr) {
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private String puzzlePiecesAlternateAnswer(QueryResult qr) {
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePiecesAlternate(qr));
    }

    private QuestionData createSentencePuzzleQuestion(QueryResult qr) {
        String question = this.formatSentenceForeign(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        List<String> alternateAnswers = new ArrayList<>();
        alternateAnswers.add(this.puzzlePiecesAlternateAnswer(qr));
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(alternateAnswers);
        data.setVocabulary(new ArrayList<String>());

        return data;
    }

    private String fillInBlankQuestion(QueryResult qr) {
        String sentence1 = "The birthday of " + qr.personNameEN + " is on " + qr.birthdayEN + ". \n";
        String sentence2 = qr.personNameEN + " is " + (qr.newAge - 1) + ".\n";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        String sentence3 = qr.personNameEN + " will be " +
                qr.newAge + " in " +
                QuestionUtils.FILL_IN_BLANK_NUMBER + " months.";
        return sentence1 + sentence2 + sentence3;
    }

    private String fillInBlankAnswer(QueryResult qr) {
        return Integer.toString(qr.monthsUntil);
    }

    //we aren't testing math so make it correct +-1
    private List<String> fillInBlankAlternateAnswers(QueryResult qr){
        List<String> acceptableAnswers = new ArrayList<>();
        int lowerbound;
        //we shouldn't have negative number
        if (qr.monthsUntil == 0)
            lowerbound = 0;
        else
            lowerbound = qr.monthsUntil - 1;
        //no need to clamp this
        int upperbound = qr.monthsUntil + 1;
        acceptableAnswers.add(Integer.toString(lowerbound));
        acceptableAnswers.add(Integer.toString(upperbound));

        return acceptableAnswers;
    }

    private QuestionData createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<String> acceptableAnswers = fillInBlankAlternateAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setThemeId(super.themeData.getId());
        data.setTopic(qr.personNameForeign);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setVocabulary(null);

        return data;
    }
}
