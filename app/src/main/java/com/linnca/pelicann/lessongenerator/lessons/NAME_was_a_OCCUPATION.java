package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.TermAdjuster;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_ChooseCorrectSpelling;
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

public class NAME_was_a_OCCUPATION extends Lesson{
    public static final String KEY = "NAME_was_a_OCCUPATION";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String occupationEN;
        private final String occupationJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String occupationEN,
                String occupationJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.occupationEN = occupationEN;
            this.occupationJP = occupationJP;
        }
    }

    public NAME_was_a_OCCUPATION(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personLabel ?personEN " +
                " ?occupationEN ?occupationLabel " +
                "WHERE " +
                "{" +
                "    ?person wdt:P106 ?occupation . " + //has an occupation
                "    ?person wdt:P570 ?dead . " + //is dead
                "    ?person rdfs:label ?personEN . " +
                "    ?occupation rdfs:label ?occupationEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?occupationEN) = '" +
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
            String occupationEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationEN");
            occupationEN = TermAdjuster.adjustOccupationEN(occupationEN);
            String occupationJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "occupationLabel");

            QueryResult qr = new QueryResult(personID, personEN, personJP, occupationEN, occupationJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> sentencePuzzle = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzle);

            List<QuestionData> chooseCorrectSpelling = createChooseCorrectSpellingQuestion(qr);
            questionSet.add(chooseCorrectSpelling);

            List<QuestionData> fillInBlank = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlank);

            List<QuestionData> fillInBlankMultipleChoice = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoice);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord occupation = new VocabularyWord("",qr.occupationEN, qr.occupationJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(occupation);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String indefiniteArticle = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String sentence = qr.personEN + " was " + indefiniteArticle + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.occupationJP + "でした。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("was");
        pieces.add(GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN));
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createChooseCorrectSpellingQuestion(QueryResult qr){
        String question = qr.occupationJP;
        String answer = qr.occupationEN;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_ChooseCorrectSpelling.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    //give a hint (first 1 or 2 characters, depending on length of word)
    private final int hintBorder = 7;
    private String fillInBlankQuestion(QueryResult qr){
        int hintCt = qr.occupationEN.length() > hintBorder ? 2 : 1;
        String precedingHint = qr.occupationEN.substring(0,hintCt);
        String followingHint = qr.occupationEN.substring(qr.occupationEN.length()-hintCt);
        String indefiniteArticle = GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN);
        String article;
        //remove article
        if (indefiniteArticle.substring(0,2).equals("a "))
            article = "a";
        else
            article = "an";

        String sentence = qr.personEN + " was " + article + " " +
                precedingHint + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + followingHint + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer(QueryResult qr){
        int hintCt = qr.occupationEN.length() > hintBorder ? 2 : 1;
        return qr.occupationEN.substring(hintCt, qr.occupationEN.length()-hintCt);
    }

    //in case the user types the whole thing in
    private List<String> fillInBlankAlternateAnswers(QueryResult qr){
        List<String> answers = new ArrayList<>(1);
        answers.add(qr.occupationEN);
        return answers;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<String> acceptableAnswers = fillInBlankAlternateAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + GrammarRules.indefiniteArticleBeforeNoun(qr.occupationEN) + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("is");
        choices.add("was");
        choices.add("ised");
        choices.add("issed");
        return choices;
    }

    private String fillInBlankMultipleChoiceAnswer(){
        return "was";
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}