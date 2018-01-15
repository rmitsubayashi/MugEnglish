package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
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
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class NAME_worked_for_the_government_He_was_a_politician extends Lesson {
    public static final String KEY = "NAME_worked_for_the government_He_was_a_politician";

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
                boolean isMale
        )
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.genderEN = isMale ? "he" : "she";
            this.genderJP = isMale ? "彼" : "彼女";
        }
    }

    public NAME_worked_for_the_government_He_was_a_politician(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 3;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personLabel ?personEN " +
                " ?gender " +
                "WHERE " +
                "{" +
                "    ?person wdt:P21 ?gender . " + //has gender
                "    ?person wdt:P106 wd:Q82955 . " + //is a politician
                "    ?person wdt:P570 ?dead . " + //is dead
                "    ?person rdfs:label ?personEN . " + //English label
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

            QueryResult qr = new QueryResult(personID, personEN, personJP, isMale);
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

            List<QuestionData> fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);


            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, null));
        }

    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は政府で働いていました。" + qr.genderJP + "は政治家でした。";
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "政府";
        String answer = "government";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createSpellingQuestionGeneric(){
        String question = "政治家";
        String answer = "politician";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.CHOOSECORRECTSPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createSpellingQuestionGeneric2(){
        String question = "政治家";
        String answer = "politician";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SPELLING);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<QuestionData> translateQuestion = createTranslateQuestionGeneric();
        List<QuestionData> spellingQuestion = createSpellingQuestionGeneric();
        List<QuestionData> spellingQuestion2 = createSpellingQuestionGeneric2();
        List<List<QuestionData>> questions = new ArrayList<>(3);
        questions.add(translateQuestion);
        questions.add(spellingQuestion);
        questions.add(spellingQuestion2);

        return questions;

    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
            " for the government.";
        String sentence3 = qr.genderEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " a politician.";
        sentence3 = GrammarRules.uppercaseFirstLetterOfSentence(sentence3);
        return sentence1 + "\n\n" + sentence2 + "\n" + sentence3;
    }


    private String fillInBlankMultipleChoiceAnswer(){
        return "worked : was";
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("work   : was");
        choices.add("worked :  is");
        choices.add("worked : was");
        choices.add("wor    :  is");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer();
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String sentence = qr.personEN + " worked " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        String sentence2 = qr.genderEN + " was a politician.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer2(){
        return "for the government";
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(4);
        choices.add("at the government");
        choices.add("for the government");
        choices.add("from the government");
        choices.add("on the government");

        return choices;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback2(){
        String response = "at the government";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        String feedback = "atは特定な場所を指すときに使います。政府（government）は具体的な政府機関ではないので、atは使いません。";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(fillInBlankMultipleChoiceFeedback2());
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = qr.personJP + "は政府で働いていました。";
        String sentence2 = qr.personEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " for the government.";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(){
        return "worked";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence1 = qr.personJP + "は政治家でした。";
        String sentence2 = qr.personEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " a politician.";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer2(){
        return "was";
    }

    private List<QuestionData> createFillInBlankQuestion2(QueryResult qr){
        String question = this.fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2();
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);

        return questionDataList;
    }
}
