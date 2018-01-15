package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSetData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class NAME_is_young_He_is_AGE extends Lesson {
    public static final String KEY = "NAME_is_young_He_is_AGE";

    //since this is before learning numbers,
    //use digits instead of words

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final int age;
        private final String genderEN;
        private final String genderJP;
        private final String youngOldEN;
        private final String youngOldJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String birthdayString,
                boolean isMale)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.age = getAge(birthdayString);
            this.genderEN = isMale ? "he" : "she";
            this.genderJP = isMale ? "彼" : "彼女";
            //technically <40 and >65, but these are filtered out
            // in the query
            this.youngOldEN = age > 50 ? "old" : "young";
            this.youngOldJP = age > 50 ? "歳をとっています" : "若いです";
        }

        private int getAge(String birthdayString){
            birthdayString = birthdayString.substring(0, 10);
            LocalDate birthday = LocalDate.parse(birthdayString);
            LocalDate now = new LocalDate();
            Years age = Years.yearsBetween(birthday, now);
            return age.getYears();
        }
    }

    public NAME_is_young_He_is_AGE(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 5;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_QUESTION;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person with birthday and is alive
        return "SELECT ?person ?personLabel ?personEN " +
                " ?birthday ?gender " +
                "WHERE " +
                "{" +
                "    ?person wdt:P21 ?gender . " + //has gender
                "    ?person wdt:P569 ?birthday . " + //has a birthday
                "    FILTER NOT EXISTS { ?person wdt:P570 ?dateDeath } . " + //but not a death date
                "    FILTER (?age > 65 || ?age < 40) . " + //is either young or old
                "    BIND (year(NOW()) - year(?birthday) as ?age) . " + //calculate age (estimate)
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

            QueryResult qr = new QueryResult(personID, personEN, personJP, birthday, isMale);
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

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord word;
        if (qr.youngOldEN.equals("old")) {
            word = new VocabularyWord("", "old", "歳をとっている",
                    formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        } else {
            word = new VocabularyWord("", "young", "若い",
                    formatSentenceEN(qr), formatSentenceJP(qr), KEY);
        }
        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(word);

        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String sentence1 =  qr.personEN + " is " + qr.youngOldEN + ".";
        String sentence2 = qr.genderEN + " is " + Integer.toString(qr.age) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String formatSentenceJP(QueryResult qr){
        String sentence1 = qr.personJP + "は" + qr.youngOldJP + "。";
        String sentence2 = qr.genderJP + "は" + Integer.toString(qr.age) + "歳です。";
        return sentence1 + "\n" + sentence2;
    }

    private String multipleChoiceQuestion(){
        return "若い";
    }

    private String multipleChoiceAnswer(){
        return "young";
    }

    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("young");
        choices.add("old");
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

    private String multipleChoiceQuestion2(){
        return "歳をとっている";
    }

    private String multipleChoiceAnswer2(){
        return "old";
    }

    private List<QuestionData> createMultipleChoiceQuestion2(){
        String question = multipleChoiceQuestion2();
        String answer = multipleChoiceAnswer2();
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

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<QuestionData> multipleChoice = createMultipleChoiceQuestion();
        List<QuestionData> multipleChoice2 = createMultipleChoiceQuestion2();
        List<List<QuestionData>> questions = new ArrayList<>(2);
        questions.add(multipleChoice);
        questions.add(multipleChoice2);

        return questions;
    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(0,2);
        Collections.shuffle(multipleChoiceQuestions);
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence1 =  qr.personEN + " is " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        String sentence2 = qr.genderEN + " is " + Integer.toString(qr.age) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.youngOldEN;
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("young");
        choices.add("old");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
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
        String sentence1 =  qr.personEN + " is " + qr.youngOldEN + ".";
        String sentence2 = QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " is " + Integer.toString(qr.age) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer2(QueryResult qr){
        return qr.genderEN;
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(2);
        choices.add("he");
        choices.add("she");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2(qr);
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
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

}