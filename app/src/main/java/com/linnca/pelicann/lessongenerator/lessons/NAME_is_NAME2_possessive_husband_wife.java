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
import com.linnca.pelicann.questions.QuestionFeedbackFormatter;
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

public class NAME_is_NAME2_possessive_husband_wife extends Lesson{
    public static final String KEY = "NAME_is_NAME2_possessive_husband_wife";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String spouseEN;
        private final String spouseJP;
        private final String personTitleEN;
        private final String personTitleJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String spouseEN,
                String spouseJP,
                String personGenderID)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.spouseEN = spouseEN;
            this.spouseJP = spouseJP;
            this.personTitleEN = getPersonTitleEN(personGenderID);
            this.personTitleJP = getPersonTitleJP(personGenderID);
        }

        private String getPersonTitleEN(String id){
            switch (id){
                case "Q6581097":
                    return "husband";
                case "Q6581072":
                    return "wife";
                default:
                    return "none";
            }
        }

        private String getPersonTitleJP(String id){
            switch (id){
                case "Q6581097":
                    return "夫";
                case "Q6581072":
                    return "妻";
                default:
                    return "none";
            }
        }
    }

    public NAME_is_NAME2_possessive_husband_wife(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 3;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?person ?personLabel ?personEN " +
                " ?spouseEN ?spouseLabel " +
                " ?personGender " +
                "WHERE " +
                "{" +
                "    ?person wdt:P26 ?spouse . " + //has a spouse
                "    ?person wdt:P21 ?personGender . " + //get person gender
                "    ?person rdfs:label ?personEN . " +
                "    ?spouse rdfs:label ?spouseEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?spouseEN) = '" +
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
            String spouseEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseEN");
            String spouseJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "spouseLabel");
            String personGenderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "personGender");
            personGenderID = WikiDataEntity.getWikiDataIDFromReturnedResult(personGenderID);
            QueryResult qr = new QueryResult(personID, personEN, personJP,
                    spouseEN, spouseJP,
                    personGenderID);
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

            List<QuestionData> fillInBlankMultipleChoiceQuestion2 = createFillInBlankMultipleChoiceQuestion2(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion2);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord spouse = new VocabularyWord("",qr.personTitleEN, qr.personTitleJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(spouse);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        return qr.personEN + " is " + qr.spouseEN + "\'s " +
                qr.personTitleEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.spouseJP +
                "の" + qr.personTitleJP + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("is");
        pieces.add(qr.spouseEN);
        pieces.add("'s");
        pieces.add(qr.personTitleEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return Question_SentencePuzzle.formatAnswer(puzzlePieces(qr));
    }

    private List<String> acceptablePuzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.spouseEN);
        pieces.add("'s");
        pieces.add(qr.personTitleEN);
        pieces.add("is");
        pieces.add(qr.personEN);
        return pieces;
    }

    private List<String> puzzlePiecesAcceptableAnswers(QueryResult qr){
        List<String> pieces = acceptablePuzzlePieces(qr);
        String answer = Question_SentencePuzzle.formatAnswer(pieces);
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(answer);
        return acceptableAnswers;
    }

    private FeedbackPair sentencePuzzleFeedback(QueryResult qr){
        List<String> responsePieces = acceptablePuzzlePieces(qr);
        String responseAnswer = Question_SentencePuzzle.formatAnswer(responsePieces);
        String response = QuestionFeedbackFormatter.formatSentencePuzzleAnswer(responseAnswer);
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        List<String> correctPieces = puzzlePieces(qr);
        String correctAnswer =Question_SentencePuzzle.formatAnswer(correctPieces);
        String correctFeedback = QuestionFeedbackFormatter.formatSentencePuzzleAnswer(correctAnswer);
        String feedback = "今回は「AのBはCです」ではなくて「CはAのBです」なので、\n" +
                correctFeedback + "\n" +
                "のほうが良い解答です。";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        List<QuestionData> dataList = new ArrayList<>();
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        List<String> acceptableAnswers = puzzlePiecesAcceptableAnswers(qr);
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(sentencePuzzleFeedback(qr));
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(allFeedback);

        dataList.add(data);

        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " is " +
                qr.spouseEN + "'s " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                ".";
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.personTitleEN;
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
        String answer = fillInBlankMultipleChoiceAnswer(qr);
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

        return questionDataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = qr.personEN + " is " +
                qr.spouseEN + "'s " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT +
                ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.personTitleEN;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
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

        return questionDataList;
    }

    private String fillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String sentence = qr.personEN + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " is " +
                qr.spouseEN + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.personTitleEN + ".";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(4);
        choices.add("   : 's");
        choices.add("'s : 's");
        choices.add("'s :   ");
        return choices;
    }

    private String fillInBlankMultipleChoiceAnswer2(){
        return "   : 's";
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }
}