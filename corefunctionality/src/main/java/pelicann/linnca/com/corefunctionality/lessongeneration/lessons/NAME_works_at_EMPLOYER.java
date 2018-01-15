package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import pelicann.linnca.com.corefunctionality.questions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class NAME_works_at_EMPLOYER extends Lesson {
    public static final String KEY = "NAME_works_at_EMPLOYER";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private final Map<String, List<String>> queryResultMap = new HashMap<>();

    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String employerID;
        private final String employerEN;
        private final String employerJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String employerID,
                String employerEN,
                String employerJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.employerID = employerID;
            this.employerEN = employerEN;
            this.employerJP = employerJP;
        }
    }

    public NAME_works_at_EMPLOYER(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION;
    }

    @Override
    protected String getSPARQLQuery(){
        //since there aren't that many Japanese employers available,
        //just get the employer name and convert it to a employer by adding "~人"
        return "SELECT ?person ?personLabel ?personEN " +
                " ?employer ?employerEN ?employerLabel " +
                "WHERE " +
                "{" +
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

            //to help with multiple choice questions
            if (queryResultMap.containsKey(personID)){
                List<String> value = queryResultMap.get(personID);
                value.add(employerID);
            } else {
                List<String> list = new ArrayList<>();
                list.add(employerID);
                queryResultMap.put(personID, list);
            }

            QueryResult qr = new QueryResult(personID, personEN, personJP, employerID, employerEN, employerJP);
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

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord work = new VocabularyWord("","work", "仕事をする",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
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
    private String formatSentenceEN(QueryResult qr){
        //use the definite article before school name ( ~ of ~)
        //for better accuracy.
        //still there are a lot of employers that will need 'the'
        String sentence = qr.personEN + " works at " +
                GrammarRules.definiteArticleBeforeSchoolName(qr.employerEN);
        if (!qr.employerEN.endsWith(".")){
            sentence += ".";
        }
        return sentence;
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.employerJP + "で働いています。";
    }

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<QuestionData> translate = createTranslateQuestion();
        List<QuestionData> multipleChoice = createMultipleChoiceQuestion();
        List<QuestionData> translate2 = createTranslateQuestion2();

        List<List<QuestionData>> questionSet = new ArrayList<>(3);
        questionSet.add(translate);
        questionSet.add(multipleChoice);
        questionSet.add(translate2);
        return questionSet;

    }

    private List<String> translateQuestionAcceptableAnswers(){
        List<String> answers = new ArrayList<>(3);
        answers.add("仕事");
        answers.add("働いている");
        answers.add("働いてる");
        answers.add("はたらく");
        return answers;
    }

    private List<QuestionData> createTranslateQuestion(){
        String question = "work";
        String answer = "働く";
        List<String> acceptableAnswers = translateQuestionAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String multipleChoiceQuestion(){
        return "～で働いています";
    }

    private String multipleChoiceAnswer(){
        return "works at";
    }

    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("works at");
        choices.add("works from");
        choices.add("works");
        return choices;
    }

    private List<QuestionData> createMultipleChoiceQuestion(){
        String question = multipleChoiceQuestion();
        String answer = multipleChoiceAnswer();
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = multipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        questionDataList.add(data);

        return questionDataList;
    }

    private List<String> translateQuestionAcceptableAnswers2(){
        List<String> answers = new ArrayList<>(1);
        answers.add("work at");
        return answers;
    }

    private List<QuestionData> createTranslateQuestion2(){
        String question = "～で働いています";
        String answer = "works at";
        List<String> acceptableAnswers = translateQuestionAcceptableAnswers2();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }


    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = qr.personEN + " works at " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private List<QueryResult> bigEmployers(){
        List<QueryResult> employers = new ArrayList<>(5);
        employers.add(new QueryResult("","","","Q53268","Toyota","トヨタ自動車"));
        employers.add(new QueryResult("","","","Q201653","SoftBank", "ソフトバンクグループ株式会社"));
        employers.add(new QueryResult("","","","Q41187","Sony","ソニー"));
        employers.add(new QueryResult("","","","Q95","Google","Google"));
        employers.add(new QueryResult("","","","Q312","Apple Inc.","アップル"));
        return employers;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return GrammarRules.definiteArticleBeforeSchoolName(qr.employerEN);
    }

    private List<String> fillInBlankMultipleChoiceChoices(QueryResult qr){
        List<String> allEmployers = queryResultMap.get(qr.personID);
        List<QueryResult> possibleEmployers = bigEmployers();
        for (Iterator<QueryResult> iterator = possibleEmployers.iterator(); iterator.hasNext();){
            QueryResult employer = iterator.next();
            if (allEmployers.contains(employer.employerID)){
                iterator.remove();
            }
        }

        if (possibleEmployers.size() > 2){
            Collections.shuffle(possibleEmployers);
            possibleEmployers = possibleEmployers.subList(0,2);
        }

        List<String> choices = new ArrayList<>(2);
        for (QueryResult employer : possibleEmployers){
            choices.add(employer.employerEN);
        }
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices(qr);
        choices.add(answer);
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

    private String fillInBlankQuestion(QueryResult qr){
        String sentence = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " " + GrammarRules.definiteArticleBeforeSchoolName(qr.employerEN);
        if (!qr.employerEN.endsWith(".")) {
            sentence += ".";
        }
        return sentence + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(){
        return "works at";
    }

    private List<String> fillInBlankAcceptableAnswers(){
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add("work at");
        return acceptableAnswers;
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<String> acceptableAnswers = fillInBlankAcceptableAnswers();
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        questionDataList.add(data);

        return questionDataList;
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> instructionsQuestion = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(instructionsQuestion);
        return questionSet;
    }

    private String instructionQuestionQuestion(){
        return "あなたはどこで働いていますか。";
    }

    private String instructionQuestionAnswer(){
        return "I work at " + QuestionResponseChecker.ANYTHING + ".";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        String acceptableAnswer = "I works at " + QuestionResponseChecker.ANYTHING + ".";
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add(acceptableAnswer);
        return acceptableAnswers;

    }

    private FeedbackPair instructionQuestionFeedback(){
        String response = "I works at " + QuestionResponseChecker.ANYTHING + ".";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        String feedback = "自分のことを言っている場合、動詞の最後のsはいりません。\nworksではなくworkになります。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private List<QuestionData> createInstructionQuestion(){
        String question = this.instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        List<String> acceptableAnswers = instructionQuestionAcceptableAnswers();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(instructionQuestionFeedback());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
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
