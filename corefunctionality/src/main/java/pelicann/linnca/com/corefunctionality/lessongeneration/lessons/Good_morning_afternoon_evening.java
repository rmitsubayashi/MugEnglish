package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;


import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
* This lesson only uses the three terms
* so no dynamic content
* */
public class Good_morning_afternoon_evening extends Lesson {
    public static final String KEY = "Good_morning_afternoon_evening";

    public Good_morning_afternoon_evening(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.lessonKey = KEY;
        super.questionOrder = LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET;
    }
    @Override
    protected synchronized int getQueryResultCt(){return 0;}
    @Override
    protected String getSPARQLQuery(){
        return "";
    }
    @Override
    protected synchronized void createQuestionsFromResults(){}
    @Override
    protected void processResultsIntoClassWrappers(Document document){}

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> questionSet = new ArrayList<>(9);
        List<List<QuestionData>> chatMultipleChoice = chatMultipleChoiceQuestions();
        questionSet.addAll(chatMultipleChoice);
        List<List<QuestionData>> multipleChoice = multipleChoiceQuestions();
        questionSet.addAll(multipleChoice);
        List<List<QuestionData>> fillInBlank = fillInBlankQuestions();
        questionSet.addAll(fillInBlank);
        List<List<QuestionData>> multipleChoice2 = multipleChoiceQuestions2();
        questionSet.addAll(multipleChoice2);
        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> chatMultipleChoiceQuestions = preGenericQuestions.subList(0,3);
        Collections.shuffle(chatMultipleChoiceQuestions);
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(3,6);
        Collections.shuffle(multipleChoiceQuestions);
        List<List<QuestionData>> fillInBlankQuestions = preGenericQuestions.subList(6,9);
        Collections.shuffle(fillInBlankQuestions);
        List<List<QuestionData>> multipleChoiceQuestions2 = preGenericQuestions.subList(9,12);
        Collections.shuffle(multipleChoiceQuestions2);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(new VocabularyWord("", "good morning","おはよう",
                "Good morning!","おはよう！", KEY));
        words.add(new VocabularyWord("", "good afternoon","こんにちは",
                "Good afternoon!","こんにちは！", KEY));
        words.add(new VocabularyWord("", "good evening","こんばんは",
                "Good evening!","こんばんは！", KEY));
        return words;
    }

    private List<List<QuestionData>> chatMultipleChoiceQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = multipleChoiceChoices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLECHOICE);
            ChatQuestionItem chatItem1 = new ChatQuestionItem(false, answer);
            ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
            List<ChatQuestionItem> chatItems = new ArrayList<>(2);
            chatItems.add(chatItem1);
            chatItems.add(answerItem);
            data.setQuestion(QuestionSerializer.serializeChatQuestion("無名", chatItems));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("good morning");
        choices.add("good afternoon");
        choices.add("good evening");
        return choices;
    }

    private List<String> multipleChoiceChoicesJP(){
        List<String> choices = new ArrayList<>(3);
        choices.add("おはよう");
        choices.add("こんにちは");
        choices.add("こんばんは");
        return choices;
    }

    private List<List<QuestionData>> multipleChoiceQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> enAnswers = multipleChoiceChoices();
        List<String> jpAnswers = multipleChoiceChoicesJP();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = enAnswers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
            data.setQuestion(jpAnswers.get(i));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<List<QuestionData>> fillInBlankQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>();
        List<String> enAnswers = multipleChoiceChoices();
        List<String> jpAnswers = multipleChoiceChoicesJP();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = enAnswers.get(i).replace("good ","");
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
            data.setQuestion(jpAnswers.get(i) + "\n\ngood " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<List<String>> getTimesMultipleChoiceQuestion2(){
        List<String> morningTimes = new ArrayList<>(3);
        morningTimes.add("8:00AM");
        morningTimes.add("9:00AM");
        morningTimes.add("10:00AM");
        List<String> afternoonTimes = new ArrayList<>(3);
        afternoonTimes.add("1:00PM");
        afternoonTimes.add("2:00PM");
        afternoonTimes.add("3:00PM");
        List<String> eveningTimes = new ArrayList<>(3);
        eveningTimes.add("6:00PM");
        eveningTimes.add("7:00PM");
        eveningTimes.add("8:00PM");
        List<List<String>> allTimes = new ArrayList<>(3);
        allTimes.add(morningTimes);
        allTimes.add(afternoonTimes);
        allTimes.add(eveningTimes);
        return allTimes;

    }

    private List<List<QuestionData>> multipleChoiceQuestions2(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> enAnswers = multipleChoiceChoices();
        List<List<String>> allTimes = getTimesMultipleChoiceQuestion2();
        for (int i=0; i<3; i++) {
            List<QuestionData> dataList = new ArrayList<>();
            List<String> times = allTimes.get(i);
            for (String time : times) {
                QuestionData data = new QuestionData();
                String answer = enAnswers.get(i);
                data.setId("");
                data.setLessonId(lessonKey);

                data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
                data.setQuestion(time);
                data.setChoices(multipleChoiceChoices());
                data.setAnswer(answer);
                data.setAcceptableAnswers(null);

                dataList.add(data);
            }
            questions.add(dataList);
        }

        return questions;
    }
}
