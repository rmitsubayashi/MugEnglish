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
import com.linnca.pelicann.questions.Question_Instructions;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_a_GENDER extends Lesson {
    public static final String KEY = "NAME_is_a_GENDER";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String genderEN;
        private final String genderJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String genderEN,
                String genderJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.genderEN = genderEN;
            this.genderJP = genderJP;
        }
    }

    public NAME_is_a_GENDER(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 4;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION;
    }

    @Override
    protected String getSPARQLQuery(){
        //find person name and blood type
        return "SELECT ?person ?personLabel ?personEN " +
                " ?gender " +
                "WHERE " +
                "{" +
                "    ?person wdt:P21 ?gender . " + //has an gender
                "    ?person rdfs:label ?personEN . " +
                "    FILTER (LANG(?personEN) = '" +
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
            String gender = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            gender = WikiDataEntity.getWikiDataIDFromReturnedResult(gender);
            String genderEN;
            String genderJP;
            switch (gender){
                case "Q6581097":
                    genderEN = "man";
                    genderJP = "男";
                    break;
                case "Q6581072":
                    genderEN = "woman";
                    genderJP = "女";
                    break;
                default:
                    genderEN = "man/woman";
                    genderJP = "男/女";
            }

            QueryResult qr = new QueryResult(personID, personEN, personJP, genderEN, genderJP);
            queryResults.add(qr);
        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> multipleChoice = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(multipleChoice);

            List<QuestionData> fillInBlank = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlank);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord gender = new VocabularyWord("",qr.genderEN, qr.genderJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        VocabularyWord is = new VocabularyWord("", "is","~は",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(is);
        words.add(gender);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence = qr.personEN + " is a " + qr.genderEN + ".";
        //no need since all names are capitalized?
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        return sentence;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.genderJP + "です。";
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = qr.personEN + " is a " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.genderEN;
    }

    private List<String> fillInBlankMultipleChoiceChoices(QueryResult qr){
        List<String> choices = new ArrayList<>(3);
        choices.add("man");
        choices.add("woman");
        //for ambiguous people
        if (!choices.contains(qr.genderEN)){
            choices.add(qr.genderEN);
        }
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices(qr);
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

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " is a " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        return sentence + "\n\n" + sentence2;
    }

    private List<String> fillInBlankAlternateAnswers(QueryResult qr){

        if (qr.genderEN.equals("man/woman")){
            List<String> answers = new ArrayList<>(1);
            answers.add("man / woman");
            return answers;
        }
        return null;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = qr.genderEN;
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

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> spellingSuggestive = createSpellingSuggestiveQuestion();
        List<QuestionData> instructions = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(2);
        questionSet.add(spellingSuggestive);
        questionSet.add(instructions);
        return questionSet;
    }

    private List<QuestionData> createSpellingSuggestiveQuestion(){
        String question = "私は～";
        String answer = "I am";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    //use the knowledge from previous question
    private String instructionQuestionQuestion(){
        return "あなたは男ですか。女ですか。";
    }

    private String instructionQuestionAnswer(){
        return "I am a man.";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        String acceptableAnswer1 = "I am a woman.";
        String acceptableAnswer2 = "I am man.";
        String acceptableAnswer3 = "I am woman.";
        String acceptableAnswer4 = "man";
        String acceptableAnswer5 = "woman";
        List<String> acceptableAnswers = new ArrayList<>(5);
        acceptableAnswers.add(acceptableAnswer1);
        acceptableAnswers.add(acceptableAnswer2);
        acceptableAnswers.add(acceptableAnswer3);
        acceptableAnswers.add(acceptableAnswer4);
        acceptableAnswers.add(acceptableAnswer5);
        return acceptableAnswers;

    }

    private FeedbackPair instructionFeedback(){
        List<String> responses = new ArrayList<>(2);
        responses.add("man");
        responses.add("woman");
        String feedback = "次は I am を使ってみましょう";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private List<QuestionData> createInstructionQuestion(){
        String question = this.instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        List<String> acceptableAnswers = instructionQuestionAcceptableAnswers();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(instructionFeedback());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_Instructions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);
        data.setFeedback(allFeedback);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}