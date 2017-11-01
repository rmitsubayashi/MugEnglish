package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hi_hey_whats_up extends Lesson {
    public static final String KEY = "Hi_hey_whats_up";

    public Hi_hey_whats_up(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
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
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = new ArrayList<>(6);
        List<QuestionData> chatMultipleChoiceQuestions = chatMultipleChoiceQuestions();
        questions.addAll(chatMultipleChoiceQuestions);
        List<QuestionData> chatQuestions = chatQuestions();
        questions.addAll(chatQuestions);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>();
        for (int i=1; i<=6; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "hi"),
                "hi","こんにちは","Hi!","こんにちは！", KEY));
        words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "hey"),
                "hey","こんにちは","Hey!","こんにちは！", KEY));
        words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "what\'s up"),
                "what\'s up","こんにちは","What\'s up!","こんにちは！", KEY));
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(3);
        ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "hi"));
        ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "hey"));
        ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "what\'s up"));
        return ids;
    }

    //every choice is correct
    private List<QuestionData> chatMultipleChoiceQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = choices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLE_CHOICE);
            ChatQuestionItem chatItem1 = new ChatQuestionItem(false, answer);
            ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
            List<ChatQuestionItem> chatItems = new ArrayList<>(2);
            chatItems.add(chatItem1);
            chatItems.add(answerItem);
            data.setQuestion(QuestionUtils.formatChatQuestion("無名", chatItems));
            data.setChoices(choices());
            data.setAnswer(answer);
            List<String> alternateAnswers = choices();
            alternateAnswers.remove(answer);
            data.setAcceptableAnswers(alternateAnswers);

            questions.add(data);
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
        String feedback = "馴れ馴れしく挨拶をしてくれているので、helloとよそよそしい挨拶はできるだけ避けましょう。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    //every choice is correct
    private List<QuestionData> chatQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = choices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.CHAT);
            ChatQuestionItem chatItem1 = new ChatQuestionItem(false, answer);
            ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
            List<ChatQuestionItem> chatItems = new ArrayList<>(2);
            chatItems.add(chatItem1);
            chatItems.add(answerItem);
            data.setQuestion(QuestionUtils.formatChatQuestion("無名", chatItems));
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

            questions.add(data);
        }

        return questions;
    }
}
