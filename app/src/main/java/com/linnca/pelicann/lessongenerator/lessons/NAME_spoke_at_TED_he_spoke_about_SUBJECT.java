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
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_spoke_at_TED_he_spoke_about_SUBJECT extends Lesson{
    public static final String KEY = "NAME_spoke_at_TED_he_spoke_about_SUBJECT";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String subjectEN;
        private final String subjectJP;
        private final String genderEN;
        private final String genderJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String subjectEN,
                String subjectJP,
                boolean isMale)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.subjectEN = subjectEN;
            this.subjectJP = subjectJP;
            this.genderEN = isMale ? "he" : "she";
            this.genderJP = isMale ? "彼" : "彼女";
        }
    }

    public NAME_spoke_at_TED_he_spoke_about_SUBJECT(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personLabel ?personEN " +
                " ?subjectLabel ?subject ?gender " +
                "WHERE " +
                "{" +
                "    ?person wdt:P21 ?gender . " + //has an gender
                "    ?person wdt:P2611 ?tedID . " + //has a TED ID
                "    ?talk wdt:P31 wd:Q23011722 . " + //TED talk
                "    ?talk wdt:P50 ?person . " + //by the person
                "    ?talk wdt:P921 ?subject . " + //has a main subject
                "    ?person rdfs:label ?personEN . " +
                "    ?subject rdfs:label ?subjectEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?subjectEN) = '" +
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
            String subjectEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "subjectEN");
            String subjectJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "subjectJP");
            String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            genderID = WikiDataEntity.getWikiDataIDFromReturnedResult(genderID);
            boolean isMale;
            switch (genderID){
                case "Q6581097":
                    isMale = true;
                    break;
                case "Q6581072":
                    isMale = false;
                    break;
                default:
                    isMale = true;
            }
            QueryResult qr = new QueryResult(personID, personEN, personJP, subjectEN, subjectJP, isMale);
            queryResults.add(qr);
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

            List<QuestionData> fillInBlankMultipleChoice2 = createFillInBlankMultipleChoiceQuestion2(qr);
            questionSet.add(fillInBlankMultipleChoice2);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> fillInBlankMultipleChoice3 = createFillInBlankMultipleChoiceQuestion3(qr);
            questionSet.add(fillInBlankMultipleChoice3);

            List<QuestionData> translate = createTranslateQuestion(qr);
            questionSet.add(translate);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, new ArrayList<VocabularyWord>()));
        }

    }

    private String NAME_is_language_EN_correct(QueryResult qr){
        String sentence = qr.personEN + " spoke at TED.";
        String sentence2 = qr.genderEN + " spoke about " + qr.subjectEN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private String formatSentenceJP(QueryResult qr){
        String sentence1 =  qr.personJP + "はTEDで話しました。";
        String sentence2 = qr.genderJP + "は" + qr.subjectJP + "について話しました。";
        return sentence1 + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " at TED.";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer(){
        return "spoke";
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("spoke");
        choices.add("speaked");
        choices.add("speak");
        return choices;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback(){
        String response = "speaked";
        List<String> responses = new ArrayList<>();
        responses.add(response);
        String feedback = "speakの過去形はspokeになります";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
        feedbackPairs.add(fillInBlankMultipleChoiceFeedback());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(feedbackPairs);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " spoke " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " TED.";
        String sentence3 = qr.genderEN + " spoke " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.subjectEN + ".";
        sentence3 = GrammarRules.uppercaseFirstLetterOfSentence(sentence3);
        return sentence1 + "\n\n" + sentence2 + "\n" + sentence3;
    }

    private String fillInBlankMultipleChoiceAnswer2(){
        return "at : about";
    }

    private List<String> fillInBlankMultipleChoiceAcceptableAnswers2(){
        List<String> answers = new ArrayList<>(1);
        answers.add("on : about");
        return answers;
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(4);
        choices.add("on : about");
        choices.add("at : about");
        choices.add("at : for");
        choices.add("on : for");
        return choices;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback2(){
        String response = "on : about";
        String response2 = "at : about";
        List<String> responses = new ArrayList<>();
        responses.add(response);
        responses.add(response2);
        String feedback = "onだとテレビ番組としてのTEDで話した、\n" +
                "atだと場所としてのTEDで話した、\nというニュアンスになります。どちらも正解です。";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
        List<String> acceptableAnswers = fillInBlankMultipleChoiceAcceptableAnswers2();
        List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
        feedbackPairs.add(fillInBlankMultipleChoiceFeedback2());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(feedbackPairs);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
    
    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + " TED.";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(){
        return "spoke at";
    }

    private FeedbackPair fillInBlankFeedback(){
        List<String> responses = new ArrayList<>();
        responses.add("speaked at");
        responses.add("speaked on");
        String feedback = "speakの過去形は特別でspokeになります";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
        feedbackPairs.add(fillInBlankFeedback());
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

    private String fillInBlankMultipleChoiceQuestion3(QueryResult qr){
        return qr.personEN + " spoke " +
                Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " " +
                qr.subjectEN + " " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " TED.";
    }

    private String fillInBlankMultipleChoiceAnswer3(){
        return "about : on";
    }

    private List<String> fillInBlankMultipleChoiceAcceptableAnswers3(){
        List<String> answers = new ArrayList<>(1);
        answers.add("about : at");
        return answers;
    }

    private List<String> fillInBlankMultipleChoiceChoices3(){
        List<String> choices = new ArrayList<>(4);
        choices.add("   on : about");
        choices.add("   at : about");
        choices.add("about : on");
        choices.add("about : at");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion3(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion3(qr);
        String answer = fillInBlankMultipleChoiceAnswer3();
        List<String> choices = fillInBlankMultipleChoiceChoices3();
        List<String> acceptableAnswers = fillInBlankMultipleChoiceAcceptableAnswers3();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createTranslateQuestion(QueryResult qr){
        String question = qr.subjectEN;
        String answer = qr.subjectJP;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}