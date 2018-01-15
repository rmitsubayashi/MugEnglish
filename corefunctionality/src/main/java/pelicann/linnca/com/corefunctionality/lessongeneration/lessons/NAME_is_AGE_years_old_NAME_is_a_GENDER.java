package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
        private final int age;
        private final boolean isMale;
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
            this.isMale = isMale(gender);
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

        private boolean isMale(String genderID){
            switch (genderID){
                case "Q6581097":
                    return true;
                case "Q6581072":
                    return false;
                default:
                    return true;
            }
        }

        private boolean isAdult(){
            return age >= 18;
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
        super.questionSetsToPopulate = 1;
        super.categoryOfQuestion = WikiDataEntity.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }

    @Override
    protected String getSPARQLQuery(){
        //find person with birthday and is alive
        return "SELECT ?person ?personLabel ?personEN " +
                " ?gender ?birthday " +
                "WHERE " +
                "{" +
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
            String gender = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            gender = WikiDataEntity.getWikiDataIDFromReturnedResult(gender);
            QueryResult qr = new QueryResult(personID, personEN, personJP,gender, birthday);
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
        return qr.personEN + " is " + Integer.toString(qr.age) + " " + yearString + " old.\n" +
                qr.personEN + " is a " + qr.genderEN + ".";
    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は" + Integer.toString(qr.age) + "歳です。\n" +
                qr.personJP + "は" + qr.genderJP + "です。";
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String yearString = qr.singular ? "year" : "years";
        String sentence1 = qr.personEN + " is " + Integer.toString(qr.age) + " " +
                yearString + " old.";
        sentence1 = GrammarRules.uppercaseFirstLetterOfSentence(sentence1);
        String sentence2 = qr.personEN + " is a " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE
                + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence1 + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.genderEN;
    }

    private List<String> fillInBlankMultipleChoiceChoices(QueryResult qr){
        List<String> choices = new ArrayList<>(5);
        choices.add("man");
        choices.add("woman");
        choices.add("boy");
        choices.add("girl");
        if (!choices.contains(qr.genderEN)){
            choices.add(qr.genderEN);
        }
        return choices;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback(QueryResult qr){
        String isAdultString = qr.isAdult() ? "大人" : "子供";
        String isMaleString = qr.isMale ? "男性" : "女性";
        String feedback = isAdultString + "の" + isMaleString + "なので" + qr.genderJP + "です";
        List<String> responses = new ArrayList<>(5);
        responses.addAll(fillInBlankMultipleChoiceChoices(qr));
        responses.remove(qr.genderEN);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        List<String> choices = fillInBlankMultipleChoiceChoices(qr);
        String question = fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        FeedbackPair wrongFeedback = fillInBlankMultipleChoiceFeedback(qr);
        List<FeedbackPair> feedback = new ArrayList<>(1);
        feedback.add(wrongFeedback);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(feedback);

        List<QuestionData> questionDataList = new ArrayList<>(1);
        questionDataList.add(data);

        return questionDataList;
    }


    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(4);
        choices.add("man");
        choices.add("woman");
        choices.add("boy");
        choices.add("girl");
        return choices;
    }

    private List<String> multipleChoiceAnswers(){
        List<String> answers = new ArrayList<>(4);
        answers.add("man");
        answers.add("woman");
        answers.add("boy");
        answers.add("girl");
        return answers;
    }

    private List<String> multipleChoiceQuestions(){
        List<String> questions = new ArrayList<>(4);
        questions.add("大人の男");
        questions.add("大人の女");
        questions.add("男の子");
        questions.add("女の子");
        return questions;
    }

    private List<List<QuestionData>> createMultipleChoiceQuestions(){
        List<List<QuestionData>> allQuestions = new ArrayList<>(4);
        List<String> answers = multipleChoiceAnswers();
        List<String> questions = multipleChoiceQuestions();
        for (int i=0; i<4; i++) {
            String question = questions.get(i);
            String answer = answers.get(i);

            List<String> choices = multipleChoiceChoices();
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
            data.setQuestion(question);
            data.setChoices(choices);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> questionDataList = new ArrayList<>(4);
            questionDataList.add(data);
            allQuestions.add(questionDataList);
        }

        return allQuestions;
    }

    private List<String> multipleChoiceQuestions2(){
        List<String> answers = new ArrayList<>(4);
        answers.add("I am a man. I am...");
        answers.add("I am a woman. I am...");
        answers.add("I am a boy. I am...");
        answers.add("I am a girl. I am...");
        return answers;
    }
    
    //three variations
    private List<List<String>> multipleChoiceChoices2(){
        List<List<String>> choices = new ArrayList<>(3);
        List<String> variation1 = new ArrayList<>(4);
        variation1.add("34歳 男性");
        variation1.add("42歳 女性");
        variation1.add("5歳 男性");
        variation1.add("4歳 女性");
        choices.add(variation1);
        List<String> variation2 = new ArrayList<>(4);
        variation2.add("68歳 男性");
        variation2.add("90歳 女性");
        variation2.add("10歳 男性");
        variation2.add("12歳 女性");
        choices.add(variation2);
        List<String> variation3 = new ArrayList<>(4);
        variation3.add("50歳 男性");
        variation3.add("32歳 女性");
        variation3.add("9歳 男性");
        variation3.add("13歳 女性");
        choices.add(variation3);
        return choices;
    }

    private List<List<QuestionData>> createMultipleChoiceQuestions2(){
        List<List<QuestionData>> allQuestions = new ArrayList<>(4);
        List<String> questions = multipleChoiceQuestions2();
        List<List<String>> allChoices = multipleChoiceChoices2();
        for (int i=0; i<4; i++) {
            List<QuestionData> questionDataList = new ArrayList<>(4);
            String question = questions.get(i);
            //three variations
            for (int j=0; j<3; j++) {
                List<String> choices = allChoices.get(j);
                String answer = choices.get(i);
                QuestionData data = new QuestionData();
                data.setId("");
                data.setLessonId(lessonKey);

                data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
                data.setQuestion(question);
                data.setChoices(choices);
                data.setAnswer(answer);
                data.setAcceptableAnswers(null);

                questionDataList.add(data);
            }
            allQuestions.add(questionDataList);
        }

        return allQuestions;
    }



    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        List<List<QuestionData>> multipleChoice = createMultipleChoiceQuestions();
        List<List<QuestionData>> multipleChoice2 = createMultipleChoiceQuestions2();
        questionSet.addAll(multipleChoice);
        questionSet.addAll(multipleChoice2);
        return questionSet;
    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(0,4);
        Collections.shuffle(multipleChoiceQuestions);
        List<List<QuestionData>> multipleChoiceQuestions2 = preGenericQuestions.subList(4,8);
        Collections.shuffle(multipleChoiceQuestions2);
    }

    @Override
    protected List<List<QuestionData>> getPostGenericQuestions(){
        List<QuestionData> instructions = createInstructionQuestion();
        List<List<QuestionData>> questionSet = new ArrayList<>(1);
        questionSet.add(instructions);
        return questionSet;
    }

    private String instructionQuestionQuestion(){
        return "あなたは何歳ですか。あなたは大人の男ですか。大人の女ですか。男の子ですか。女の子ですか。";
    }

    private String instructionQuestionAnswer(){
        return "I am " + QuestionResponseChecker.ANYTHING + " years old. I am a man.";
    }

    private List<String> instructionQuestionAcceptableAnswers(){
        String acceptableAnswer1 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am a woman.";
        String acceptableAnswer2 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am a boy.";
        String acceptableAnswer3 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am a girl.";
        String acceptableAnswer4 = "I am " + QuestionResponseChecker.ANYTHING + ". I am a man.";
        String acceptableAnswer5 = "I am " + QuestionResponseChecker.ANYTHING + ". I am a woman.";
        String acceptableAnswer6 = "I am " + QuestionResponseChecker.ANYTHING + ". I am a boy.";
        String acceptableAnswer7 = "I am " + QuestionResponseChecker.ANYTHING + ". I am a girl.";
        String acceptableAnswer8 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am man.";
        String acceptableAnswer9 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am woman.";
        String acceptableAnswer10 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am boy.";
        String acceptableAnswer11 = "I am " + QuestionResponseChecker.ANYTHING + " years old. I am girl.";
        String acceptableAnswer12 = "I am " + QuestionResponseChecker.ANYTHING + ". I am man.";
        String acceptableAnswer13 = "I am " + QuestionResponseChecker.ANYTHING + ". I am woman.";
        String acceptableAnswer14 = "I am " + QuestionResponseChecker.ANYTHING + ". I am boy.";
        String acceptableAnswer15 = "I am " + QuestionResponseChecker.ANYTHING + ". I am girl.";
        List<String> acceptableAnswers = new ArrayList<>(15);
        acceptableAnswers.add(acceptableAnswer1);
        acceptableAnswers.add(acceptableAnswer2);
        acceptableAnswers.add(acceptableAnswer3);
        acceptableAnswers.add(acceptableAnswer4);
        acceptableAnswers.add(acceptableAnswer5);
        acceptableAnswers.add(acceptableAnswer6);
        acceptableAnswers.add(acceptableAnswer7);
        acceptableAnswers.add(acceptableAnswer8);
        acceptableAnswers.add(acceptableAnswer9);
        acceptableAnswers.add(acceptableAnswer10);
        acceptableAnswers.add(acceptableAnswer11);
        acceptableAnswers.add(acceptableAnswer12);
        acceptableAnswers.add(acceptableAnswer13);
        acceptableAnswers.add(acceptableAnswer14);
        acceptableAnswers.add(acceptableAnswer15);
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