package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSetData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.vocabulary.VocabularyWord;

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

public class NAME_is_AGE_years_old_NAME_is_a_GENDER extends Lesson {
    public static final String KEY = "NAME_is_AGE_years_old_NAME_is_a_GENDER";

    //since this is before learning numbers,
    //use digits instead of words

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String genderEN;
        private final String genderJP;
        private final boolean isMale;
        private final int age;
        private boolean singular;
        private final String birthday;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                String gender,
                String birthdayString)
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.age = getAge(birthdayString);
            this.birthday = getBirthday(birthdayString);
            this.genderEN = getGenderEN(gender);
            this.genderJP = getGenderJP(gender);
            this.isMale = getMale(gender);
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

        private boolean getMale(String genderID){
            switch (genderID){
                case "Q6581097":
                    return true;
                case "Q6581072":
                    return false;
                default:
                    return true;
            }
        }

        private String getGenderEN(String genderID){
            switch (genderID){
                case "Q6581097":
                    if (age >= 18) {
                        return "man";
                    } else {
                        return  "boy";
                    }
                case "Q6581072":
                    if (age >= 18) {
                        return "woman";
                    } else {
                        return "girl";
                    }
                default:
                    if (age >= 18) {
                        return "man/woman";
                    } else {
                        return "boy/girl";
                    }
            }
        }

        private String getGenderJP(String genderID){
            switch (genderID){
                case "Q6581097":
                    if (age >= 18) {
                        return "大人の男";
                    } else {
                        return "男の子";
                    }
                case "Q6581072":
                    if (age >= 18) {
                        return "大人の女";
                    } else {
                        return "女の子";
                    }
                default:
                    if (age >= 18) {
                        return "大人の男/大人の女";
                    } else {
                        return "男の子/女の子";
                    }
            }
        }
    }

    public NAME_is_AGE_years_old_NAME_is_a_GENDER(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        //find person with birthday and is alive
        return "SELECT ?person ?personLabel ?personEN " +
                " ?gender ?birthday " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?person wdt:P569 ?birthday . " + //has a birthday
                "    ?person wdt:P21 ?gender . " + //has an gender
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
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String birthday = SPARQLDocumentParserHelper.findValueByNodeName(head, "birthday");
            String gender = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            gender = WikiDataEntity.getWikiDataIDFromReturnedResult(gender);
            QueryResult qr = new QueryResult(personID, personEN, personJP,gender, birthday);
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

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            List<VocabularyWord> vocabularyWords = getVocabularyWords(qr);

            super.newQuestions.add(new QuestionSetData(questionSet, qr.personID, qr.personJP, vocabularyWords));
        }

    }

    private List<VocabularyWord> getVocabularyWords(QueryResult qr){
        VocabularyWord gender = new VocabularyWord("", qr.genderEN, qr.genderJP,
                formatSentenceEN(qr), formatSentenceJP(qr), KEY);

        List<VocabularyWord> words = new ArrayList<>(1);
        words.add(gender);
        return words;
    }

    private String formatSentenceEN(QueryResult qr){
        String yearString = qr.singular ? "year" : "years";
        return qr.personEN + " is " + Integer.toString(qr.age) + yearString + " old.\n" +
                qr.personEN + " is a " + qr.genderEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + Integer.toString(qr.age) + "歳です。\n" +
                qr.personJP + "は" + qr.genderJP + "です。";
    }

    private String fillInBlankQuestion(QueryResult qr){
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        String sentence = qr.personEN + " is " +
                Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER + " " + yearString + " old.";
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
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
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
        data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(alternateAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String getTrueFalseLabel(boolean isAdult, boolean isMale){
        if (isAdult){
            if (isMale){
                return "man";
            } else {
                return "woman";
            }
        } else {
            if (isMale){
                return "boy";
            } else {
                return "girl";
            }
        }
    }

    private FeedbackPair getTrueFalseFeedback(String response, QueryResult qr, boolean isAdult, boolean isMale){
        String isAdultString = isAdult ? "大人" : "子供";
        String isMaleString = isMale ? "男性" : "女性";
        String feedback = isAdultString + "の" + isMaleString + "なので" + qr.genderJP + "です";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }


    private String trueFalseQuestion(QueryResult qr, boolean isAdult, boolean isMale){
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        String sentence = qr.personEN + " is " + Integer.toString(qr.age) + " " + yearString + " old.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        String sentence2 = qr.personEN + " is a " +
                getTrueFalseLabel(isAdult, isMale) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private List<String> trueFalseAlternateAnswers(QueryResult qr){
        if (qr.genderEN.equals("man/woman") || qr.genderJP.equals("boy/girl")){
            List<String> answers = new ArrayList<>(2);
            answers.add(Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE);
            answers.add(Question_TrueFalse.TRUE_FALSE_QUESTION_FALSE);
            return answers;
        }
        return null;
    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> dataList = new ArrayList<>();
        for (int i=0; i<4; i++) {
            boolean isMale = i > 2;
            boolean isAdult = i % 2 == 0;
            String question = this.trueFalseQuestion(qr, isAdult, isMale);
            boolean isTrue = true;
            if (isMale != qr.isMale)
                isTrue = false;
            if (isAdult != qr.age >= 18){
                isTrue = false;
            }
            String answer = Question_TrueFalse.getTrueFalseString(isTrue);
            FeedbackPair feedbackPair = getTrueFalseFeedback(
                    Question_TrueFalse.getTrueFalseString(!isTrue), qr,
                    isAdult, isMale
            );
            List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
            feedbackPairs.add(feedbackPair);
            List<String> acceptableAnswers = trueFalseAlternateAnswers(qr);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(qr.personJP);
            data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
            data.setQuestion(question);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);
            data.setFeedback(feedbackPairs);

            dataList.add(data);
        }


        return dataList;
    }

}