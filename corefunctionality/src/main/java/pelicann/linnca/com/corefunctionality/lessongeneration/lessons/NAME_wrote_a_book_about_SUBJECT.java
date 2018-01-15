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
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class NAME_wrote_a_book_about_SUBJECT extends Lesson{
    public static final String KEY = "NAME_wrote_a_book_about_SUBJECT";

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String subjectEN;
        private final String subjectJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String subjectEN,
                String subjectJP)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.subjectEN = subjectEN;
            this.subjectJP = subjectJP;
        }
    }

    public NAME_wrote_a_book_about_SUBJECT(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personEN " +
                " ?subjectLabel ?subjectEN " +
                "WHERE " +
                "{" +
                "    ?book wdt:P31 wd:Q571 ." + //is a book
                "    ?book wdt:P50 ?person . " + //written by the person
                "    ?book wdt:P921 ?subject . " + //has a subject
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
            String subjectJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "subjectLabel");
            QueryResult qr = new QueryResult(personID, personEN, personJP, subjectEN, subjectJP);
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

            List<QuestionData> fillInBlankMultipleChoice = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoice);

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> fillInBlankMultipleChoice2 = createFillInBlankMultipleChoiceQuestion2(qr);
            questionSet.add(fillInBlankMultipleChoice2);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);
            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord subject = new VocabularyWord("",qr.subjectEN, qr.subjectJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(subject);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        return qr.personEN + " wrote a book about " + qr.subjectEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.subjectJP + "についての本を書きました。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("wrote");
        pieces.add("a book");
        pieces.add("about");
        pieces.add(qr.subjectEN);
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

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence1 = qr.personJP + "は本を書きました。";
        String sentence2 = qr.personEN + " " +
                QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " a book.";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer(){
        return "wrote";
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(4);
        choices.add("wrote");
        choices.add("write");
        choices.add("writed");
        choices.add("wrute");
        return choices;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback(){
        String response = "writed";
        List<String> responses = new ArrayList<>();
        responses.add(response);
        String feedback = "writeの過去形はwroteになります";
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

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(feedbackPairs);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + " a book about " +
                qr.subjectEN + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(){
        return "wrote";
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String sentence1 = formatSentenceJP(qr);
        String sentence2 = qr.personEN + " wrote a book " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.subjectEN + ".";
        return sentence1 + "\n\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer2(){
        return "about";
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(4);
        choices.add("about");
        choices.add("for");
        choices.add("from");
        choices.add("to");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }
}