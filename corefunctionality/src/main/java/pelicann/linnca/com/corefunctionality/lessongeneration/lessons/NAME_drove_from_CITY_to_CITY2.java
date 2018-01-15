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
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class NAME_drove_from_CITY_to_CITY2 extends Lesson{
    public static final String KEY = "NAME_drove_from_CITY_to_CITY2";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String cityEN;
        private final String cityJP;
        private final String city2EN;
        private final String city2JP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String cityEN,
                String cityJP,
                String capitalEN,
                String capitalJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.cityEN = cityEN;
            this.cityJP = cityJP;
            this.city2EN = capitalEN;
            this.city2JP = capitalJP;
        }
    }

    public NAME_drove_from_CITY_to_CITY2(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 3;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personEN " +
                " ?cityEN ?cityLabel ?capitalEN ?capitalLabel " +
                "WHERE " +
                "{" +
                "    ?person wdt:P19 ?city . " + //has a place of birth
                "    ?city wdt:P31/wdt:P279* wd:Q515 . " + //is a city
                "    ?city wdt:P17 ?country . " + //city is in country
                "    ?country wdt:P36 ?capital . " + //has a capital
                "    FILTER (?capital != ?city) . " + //which is different from the place of birth
                "    ?person rdfs:label ?personEN . " +
                "    ?city rdfs:label ?cityEN . " +
                "    ?capital rdfs:label ?capitalEN . " +
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?cityEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?capitalEN) = '" +
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

            String cityEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityEN");
            String cityJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "cityLabel");
            String capitalEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "capitalEN");
            String capitalJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "capitalLabel");
            QueryResult qr = new QueryResult(personID, personEN, personJP, cityEN, cityJP, capitalEN, capitalJP);
            queryResults.add(qr);

        }
    }

    @Override
    protected synchronized int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected synchronized void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> spellingQuestion = createSpellingQuestion(qr);
            questionSet.add(spellingQuestion);

            List<QuestionData> fillInBlankMultipleChoice = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoice);

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, null));
        }

    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.cityJP + "から" +
                qr.city2JP + "まで運転しました。";
    }

    private String formatSentenceEN(QueryResult qr){
        return qr.personEN + " drove from " + qr.cityEN +
                " to " + qr.city2EN + ".";
    }

    private List<QuestionData> createSpellingQuestion(QueryResult qr){
        String question = qr.cityJP;
        String answer = qr.cityEN;
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

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " drove " +
                QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " " +
                qr.cityEN + " " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.city2EN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer(){
        return "from : to";
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("from : to");
        choices.add("to : from");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("drove");
        pieces.add("from");
        pieces.add(qr.cityEN);
        pieces.add("to");
        pieces.add(qr.city2EN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces(qr));
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = qr.personEN + "は" + qr.cityEN + "から運転してきました。";
        String sentence2 = qr.personEN + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " " + qr.cityEN + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(){
        return "drove from";
    }

    private FeedbackPair fillInBlankFeedback(){
        String response = "drived from";
        List<String> responses = new ArrayList<>();
        responses.add(response);
        String feedback = "driveの過去形はdroveになります";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(fillInBlankFeedback());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence1 = qr.personJP + "は" + qr.city2JP + "まで運転しました。";
        String sentence2 = qr.personEN + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " " + qr.city2EN + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer2(){
        return "drove to";
    }

    private FeedbackPair fillInBlankFeedback2(){
        String response = "drived to";
        List<String> responses = new ArrayList<>();
        responses.add(response);
        String feedback = "driveの過去形はdroveになります";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion2(QueryResult qr){
        String question = fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(fillInBlankFeedback2());
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }


}