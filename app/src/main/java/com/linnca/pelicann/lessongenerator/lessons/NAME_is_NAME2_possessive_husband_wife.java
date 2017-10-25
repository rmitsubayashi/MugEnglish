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
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_NAME2_possessive_husband_wife extends Lesson{
    public static final String KEY = "NAME_is_NAME2_possessive_husband_wife";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personNameEN;
        private final String personNameJP;
        private final String spouseNameEN;
        private final String spouseNameJP;
        private final String spouseTitleEN;
        private final String spouseTitleJP;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameJP,
                String spouseNameEN,
                String spouseNameJP,
                String spouseGenderID)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameJP = personNameJP;
            this.spouseNameEN = spouseNameEN;
            this.spouseNameJP = spouseNameJP;
            this.spouseTitleEN = getSpouseTitleEN(spouseGenderID);
            this.spouseTitleJP = getSpouseTitleJP(spouseGenderID);
        }

        private String getSpouseTitleEN(String id){
            switch (id){
                case "Q6581097":
                    return "husband";
                case "Q6581072":
                    return "wife";
                default:
                    return "husband";
            }
        }

        private String getSpouseTitleJP(String id){
            switch (id){
                case "Q6581097":
                    return "夫";
                case "Q6581072":
                    return "妻";
                default:
                    return "夫";
            }
        }
    }

    public NAME_is_NAME2_possessive_husband_wife(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 3;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?personName ?personNameLabel ?personNameEN " +
                " ?spouseNameEN ?spouseNameLabel " +
                " ?spouseGender " +
                "WHERE " +
                "{" +
                "    {?personName wdt:P31 wd:Q5} UNION " + //is human
                "    {?personName wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?personName wdt:P26 ?spouseName . " + //has a spouse
                "    ?spouseName wdt:P21 ?spouseGender . " + //get spouse gender
                "    ?personName rdfs:label ?personNameEN . " +
                "    ?spouseName rdfs:label ?spouseNameEN . " +
                "    FILTER (LANG(?personNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?spouseNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?personName) . " + //binding the ID of entity as ?person
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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "personName");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameEN");
            String personNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameLabel");
            String spouseNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseNameEN");
            String spouseNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseNameLabel");
            String spouseGenderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseGenderEN");
            spouseGenderID = LessonGeneratorUtils.stripWikidataID(spouseGenderID);
            QueryResult qr = new QueryResult(personID, personNameEN, personNameJP,
                    spouseNameEN, spouseNameJP,
                    spouseGenderID);
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

            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personNameJP, null));
        }

    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personNameJP + "の" + qr.spouseTitleJP +
                "は" + qr.spouseNameJP + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("'s");
        pieces.add(qr.spouseTitleEN);
        pieces.add("is");
        pieces.add(qr.spouseNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<String> puzzlePiecesAcceptableAnswers(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.spouseNameEN);
        pieces.add("is");
        pieces.add(qr.personNameEN);
        pieces.add("'s");
        pieces.add(qr.spouseTitleEN);
        String answer = QuestionUtils.formatPuzzlePieceAnswer(pieces);
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(answer);
        return acceptableAnswers;
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> dataList = new ArrayList<>();
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        List<String> acceptableAnswers = puzzlePiecesAcceptableAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        dataList.add(data);

        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = formatSentenceJP(qr);
        String sentence2 = qr.personNameEN + "'s " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " is " + qr.spouseNameEN + ".";
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswerspouseName(QueryResult qr){
        return qr.spouseTitleEN;
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("husband");
        choices.add("wife");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswerspouseName(qr);
        List<String> choices = fillInBlankMultipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.personNameEN + "'s " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                " is " + qr.spouseNameEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.spouseTitleEN;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }


}