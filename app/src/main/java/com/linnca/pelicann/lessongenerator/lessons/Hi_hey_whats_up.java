package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Chat;
import com.linnca.pelicann.questions.Question_Chat_MultipleChoice;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class Hi_hey_whats_up extends Lesson {
    public static final String KEY = "Hi_hey_whats_up";

    public Hi_hey_whats_up(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questionSet = new ArrayList<>(6);
        List<List<QuestionData>> chatMultipleChoiceQuestions = chatMultipleChoiceQuestions();
        questionSet.addAll(chatMultipleChoiceQuestions);
        List<List<QuestionData>> chatQuestions = chatQuestions();
        questionSet.addAll(chatQuestions);

        return questionSet;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(new VocabularyWord("", "hi","こんにちは",
                "Hi!","こんにちは！", KEY));
        words.add(new VocabularyWord("", "hey","こんにちは",
                "Hey!","こんにちは！", KEY));
        words.add(new VocabularyWord("", "what\'s up","こんにちは",
                "What\'s up!","こんにちは！", KEY));
        return words;
    }

    //every choice is correct
    private List<List<QuestionData>> chatMultipleChoiceQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = choices();
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
            data.setChoices(choices());
            data.setAnswer(answer);
            List<String> alternateAnswers = choices();
            alternateAnswers.remove(answer);
            data.setAcceptableAnswers(alternateAnswers);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<String> choices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("hi");
        choices.add("hey");
        choices.add("what's up");
        return choices;
    }

    private FeedbackPair helloFeedback(){
        List<String> responses = new ArrayList<>(1);
        responses.add("hello");
        String feedback = "馴れ馴れしく挨拶をしてくれているので、よそよそしい挨拶は避けましょう。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    //every choice is correct
    private List<List<QuestionData>> chatQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = choices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_Chat.QUESTION_TYPE);
            ChatQuestionItem chatItem1 = new ChatQuestionItem(false, answer);
            ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
            List<ChatQuestionItem> chatItems = new ArrayList<>(2);
            chatItems.add(chatItem1);
            chatItems.add(answerItem);
            data.setQuestion(Question_Chat.formatQuestion("無名", chatItems));
            data.setChoices(choices());
            data.setAnswer(answer);
            List<String> alternateAnswers = choices();
            alternateAnswers.remove(answer);
            //we haven't learned apostrophes yet
            alternateAnswers.add("whats up");
            //also accept 'hello'
            alternateAnswers.add("hello");
            data.setAcceptableAnswers(alternateAnswers);
            FeedbackPair feedbackPair = helloFeedback();
            List<FeedbackPair> feedbackPairs = new ArrayList<>();
            feedbackPairs.add(feedbackPair);
            data.setFeedback(feedbackPairs);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }
}
