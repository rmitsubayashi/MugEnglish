package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import pelicann.linnca.com.corefunctionality.questions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class NAME_is_AGE_years_old extends Lesson {
    public static final String KEY = "NAME_is_AGE_years_old";

    //since this is before learning numbers,
    //use digits instead of words

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final int age;
        private boolean singular;
        private final String birthday;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String birthdayString)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.age = getAge(birthdayString);
            this.birthday = getBirthday(birthdayString);
        }

        private int getAge(String birthdayString){
            birthdayString = birthdayString.substring(0, 10);
            LocalDate birthday = LocalDate.parse(birthdayString);
            LocalDate now = new LocalDate();
            Years age = Years.yearsBetween(birthday, now);
            int ageInt = age.getYears();
            if (ageInt == 1){
                singular = true;
            }
            return ageInt;
        }

        private String getBirthday(String birthdayString){
            birthdayString = birthdayString.substring(0, 10);
            LocalDate birthday = LocalDate.parse(birthdayString);
            DateTimeFormatter birthdayFormat = DateTimeFormat.forPattern("yyyy年M月d日");
            return birthdayFormat.print(birthday);
        }
    }

    public NAME_is_AGE_years_old(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 1;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person with birthday and is alive
        return "SELECT ?person ?personLabel ?personEN " +
                " ?birthday " +
                "WHERE " +
                "{" +
                "    ?person wdt:P569 ?birthday . " + //has a birthday
                "    FILTER NOT EXISTS { ?person wdt:P570 ?dateDeath } . " + //but not a death date
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
            String birthday = SPARQLDocumentParserHelper.findValueByNodeName(head, "birthday");

            QueryResult qr = new QueryResult(personID, personEN, personJP, birthday);
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

            List<QuestionData> fillInBlank = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlank);

            List<QuestionData> fillInBlank2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlank2);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord is = new VocabularyWord( "", "is","~は",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        VocabularyWord yearsOld = new VocabularyWord("","years old", "～歳",
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(is);
        words.add(yearsOld);

        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        return qr.personEN + " is " + qr.age + " " + yearString + " old.";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + qr.age + "歳です。";
    }

    //this introduces the whole phrase
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personEN);
        pieces.add("is");
        pieces.add(Integer.toString(qr.age));
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        pieces.add(yearString);
        pieces.add("old");
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
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        String sentence = qr.personEN + " is " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER + " " + yearString + " old.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        String sentence2 = "ヒント：" + qr.personJP + "の誕生日は" + qr.birthday + "です";
        DateTimeFormatter birthdayFormat = DateTimeFormat.forPattern("yyyy年M月d日");
        String today = birthdayFormat.print(DateTime.now());
        String sentence3 = "(" + today + "現在)";

        return sentence + "\n\n" + sentence2 + "\n" + sentence3;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return Integer.toString(qr.age);
    }

    //allow a leeway
    private List<String> fillInBlankAlternateAnswer(QueryResult qr){
        List<String> leeway = new ArrayList<>(2);
        leeway.add(Integer.toString(qr.age + 1));
        if (qr.age != 0)
            leeway.add(Integer.toString(qr.age-1));
        return leeway;
    }

    private FeedbackPair fillInBlankFeedback(QueryResult qr){
        List<String> responses = fillInBlankAlternateAnswer(qr);
        String feedback = "正確には" + Integer.toString(qr.age) + "歳";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<String> acceptableAnswers = fillInBlankAlternateAnswer(qr);
        FeedbackPair feedbackPair = fillInBlankFeedback(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>();
        feedbackPairs.add(feedbackPair);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        data.setFeedback(feedbackPairs);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence = qr.personJP + "は" + Integer.toString(qr.age) + "歳です。";
        String sentence2 = qr.personEN + " is " + Integer.toString(qr.age) + " " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer2(QueryResult qr){
        String yearString = qr.singular ? "year" : "years";
        return yearString + " old";
    }

    //allow either plural/singular
    private List<String> fillInBlankAlternateAnswer2(QueryResult qr){
        List<String> alternateAnswers = new ArrayList<>(1);
        String yearString = qr.singular ? "years" : "year";
        String answer = yearString + " old";
        alternateAnswers.add(answer);
        return alternateAnswers;
    }

    private List<QuestionData> createFillInBlankQuestion2(QueryResult qr){
        String question = this.fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2(qr);
        List<String> alternateAnswers = fillInBlankAlternateAnswer2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(alternateAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> instructions = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(instructions);
        return questionSet;
    }

    private String instructionQuestionQuestion(){
        return "あなたは何歳ですか。";
    }

    private String instructionQuestionAnswer(){
        return "I am " + QuestionResponseChecker.ANYTHING + " years old.";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        String acceptableAnswer1 = "I am " + QuestionResponseChecker.ANYTHING + ".";
        String acceptableAnswer2 = QuestionResponseChecker.ANYTHING + " years old.";
        List<String> acceptableAnswers = new ArrayList<>(2);
        acceptableAnswers.add(acceptableAnswer1);
        acceptableAnswers.add(acceptableAnswer2);
        return acceptableAnswers;

    }

    private List<QuestionData> createInstructionQuestion(){
        String question = this.instructionQuestionQuestion();
        String answer = instructionQuestionAnswer();
        List<String> acceptableAnswers = instructionQuestionAcceptableAnswers();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

}