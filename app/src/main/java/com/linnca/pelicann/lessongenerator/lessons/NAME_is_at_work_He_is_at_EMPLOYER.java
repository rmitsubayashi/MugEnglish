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
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_at_work_He_is_at_EMPLOYER extends Lesson {
    public static final String KEY = "NAME_is_at_work_He_is_at_EMPLOYER";

    private final List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String employerID;
        private final String employerEN;
        private final String employerJP;
        private final String genderEN;
        private final String genderJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String employerID,
                String employerEN,
                String employerJP,
                boolean isMale
                )
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.employerID = employerID;
            this.employerEN = employerEN;
            this.employerJP = employerJP;
            this.genderEN = isMale ? "he" : "she";
            this.genderJP = isMale ? "彼" : "彼女";
        }
    }

    public NAME_is_at_work_He_is_at_EMPLOYER(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;

    }

    @Override
    protected String getSPARQLQuery(){
        //since there aren't that many Japanese employers available,
        //just get the employer name and convert it to a employer by adding "~人"
        return "SELECT ?person ?personLabel ?personEN " +
                " ?employer ?employerEN ?employerLabel " +
                " ?gender " +
                "WHERE " +
                "{" +
                "    ?person wdt:P21 ?gender . " + //has gender
                "    ?person wdt:P108 ?employer . " + //has an employer
                "    ?person rdfs:label ?personEN . " + //English label
                "    ?employer rdfs:label ?employerEN . " + //English label
                "    FILTER (LANG(?employerEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                WikiBaseEndpointConnector.ENGLISH + "' } " +
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
            String employerID = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerName");
            employerID = WikiDataEntity.getWikiDataIDFromReturnedResult(employerID);
            String employerJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerLabel");
            String employerEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "employerEN");
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

            QueryResult qr = new QueryResult(personID, personEN, personJP, employerID, employerEN, employerJP, isMale);
            queryResults.add(qr);
        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> sentencePuzzle = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzle);

            List<QuestionData> fillInBlankMultipleChoice2 = createFillInBlankMultipleChoiceQuestion2(qr);
            questionSet.add(fillInBlankMultipleChoice2);

            List<QuestionData> trueFalse = createTrueFalseQuestion(qr);
            questionSet.add(trueFalse);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord work = new VocabularyWord("","work", "仕事",
                NAME_is_at_work_EN(qr), qr.personJP + "は働いています。", KEY);
        VocabularyWord employer = new VocabularyWord("",qr.employerEN, qr.employerJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(work);
        words.add(employer);
        return words;
    }

    /* Note that some of these employers may need the article 'the' before it.
     * We can't guarantee that all of them will be accurate...
     * Just make sure to let the user be aware that there may be some mistakes
     * */
    private String NAME_is_at_work_EN(QueryResult qr){
        String sentence = qr.personEN + " is at work.";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceEN(QueryResult qr) {
        String sentence1 = NAME_is_at_work_EN(qr);
        String sentence2 = qr.genderEN + " is at " + qr.employerEN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は働いています。"+qr.genderJP+"は"+qr.employerJP+"にいます。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("is");
        pieces.add("at");
        pieces.add("work");
        pieces.add(qr.genderEN);
        pieces.add("is");
        pieces.add("at");
        pieces.add(qr.employerEN);
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

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = NAME_is_at_work_EN(qr);
        String sentence2 = Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " is at " + qr.employerEN + ".";
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return GrammarRules.uppercaseFirstLetterOfSentence(qr.genderEN);
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("He");
        choices.add("She");
        return choices;
    }

    private FeedbackPair fillInBlankFeedback(QueryResult qr){
        String response;
        String gender;
        if (qr.genderEN.equals("he")){
            response = "She";
            gender = "男性";
        } else {
            response = "He";
            gender = "女性";
        }
        String feedback = qr.personJP + "は" + gender + "なので" + qr.genderEN +
                "です。";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(fillInBlankFeedback(qr));
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String sentence = qr.personEN + " is " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        String sentence2 = qr.genderEN + " is at " + GrammarRules.definiteArticleBeforeSchoolName(qr.employerEN);
        if (!qr.employerEN.endsWith(".")){
            sentence += ".";
        }
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer2(){
        return "at work";
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(3);
        choices.add("at work");
        choices.add("from work");
        choices.add("work");

        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }

    private String trueFalseQuestion(QueryResult qr, boolean isTrue){
        if (isTrue){
            return formatSentenceEN(qr);
        } else {
            String sentence1 = qr.genderEN + " is at work.";
            String sentence2 = qr.personEN + " is at " + GrammarRules.definiteArticleBeforeSchoolName(qr.employerEN);
            if (!qr.employerEN.endsWith(".")){
                sentence2 += ".";
            }
            sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
            return sentence1 + "\n" + sentence2;
        }
    }

    //one true and one false question
    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> questionDataList = new ArrayList<>();

        String question = trueFalseQuestion(qr, true);
        String answer = Question_TrueFalse.getTrueFalseString(true);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        questionDataList.add(data);

        question = trueFalseQuestion(qr, false);
        answer = Question_TrueFalse.getTrueFalseString(false);
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        questionDataList.add(data);

        return questionDataList;
    }
}
