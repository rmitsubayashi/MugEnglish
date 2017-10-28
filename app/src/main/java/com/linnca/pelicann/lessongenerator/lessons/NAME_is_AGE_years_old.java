package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

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

    public NAME_is_AGE_years_old(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 4;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person with birthday and is alive
        return "SELECT ?person ?personLabel ?personEN " +
                " ?birthday " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
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
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String birthday = SPARQLDocumentParserHelper.findValueByNodeName(head, "birthday");

            QueryResult qr = new QueryResult(personID, personEN, personJP, birthday);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, null));
        }

    }

    private String fillInBlankQuestion(QueryResult qr){
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        String sentence = qr.personEN + " is " +
                Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER + " " + yearString + " old.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        String sentence2 = "ヒント：" + qr.personJP + "の誕生日は" + qr.birthday + "です";

        return sentence + "\n\n" + sentence2;
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
        data.setTopic(qr.personJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
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
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
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
        data.setTopic(qr.personJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(alternateAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

}