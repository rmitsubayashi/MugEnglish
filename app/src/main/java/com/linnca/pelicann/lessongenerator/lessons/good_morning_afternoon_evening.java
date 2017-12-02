package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Chat;
import com.linnca.pelicann.questions.Question_Chat_MultipleChoice;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_MultipleChoice;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
* This lesson only uses the three terms
* so no dynamic content
* */
public class good_morning_afternoon_evening extends Lesson {
    public static final String KEY = "good_morning_afternoon_evening";

    public good_morning_afternoon_evening(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.lessonKey = KEY;
    }
    @Override
    protected int getQueryResultCt(){return 0;}
    @Override
    protected String getSPARQLQuery(){
        return "";
    }
    @Override
    protected void createQuestionsFromResults(){}
    @Override
    protected void processResultsIntoClassWrappers(Document document){}

    @Override
    protected List<List<QuestionData>> getPreGenericQuestions(){
        List<List<QuestionData>> questionSet = new ArrayList<>(9);
        List<List<QuestionData>> chatMultipleChoiceQuestions = chatMultipleChoiceQuestions();
        questionSet.addAll(chatMultipleChoiceQuestions);
        List<List<QuestionData>> multipleChoiceQuestions = multipleChoiceQuestions();
        questionSet.addAll(multipleChoiceQuestions);
        List<List<QuestionData>> fillInBlankQuestions = fillInBlankQuestions();
        questionSet.addAll(fillInBlankQuestions);

        return questionSet;

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
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_Chat_MultipleChoice.QUESTION_TYPE);
            ChatQuestionItem chatItem1 = new ChatQuestionItem(false, answer);
            ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
            List<ChatQuestionItem> chatItems = new ArrayList<>(2);
            chatItems.add(chatItem1);
            chatItems.add(answerItem);
            data.setQuestion(Question_Chat.formatQuestion("無名", chatItems));
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
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
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
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(jpAnswers.get(i) + "\n\ngood " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }
}
