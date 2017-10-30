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

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Thanks_no_problem extends Lesson {
    public static final String KEY = "Thanks_no_problem";

    public Thanks_no_problem(WikiBaseEndpointConnector connector, LessonListener listener){
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
        List<QuestionData> questions = new ArrayList<>(4);
        List<QuestionData> chatMultipleChoiceQuestion = chatMultipleChoiceQuestion();
        questions.addAll(chatMultipleChoiceQuestion);
        List<QuestionData> chatQuestion = chatQuestion();
        questions.addAll(chatQuestion);
        List<QuestionData> spellingQuestion = spellingQuestion();
        questions.addAll(spellingQuestion);
        List<QuestionData> translateQuestion = translateQuestion();
        questions.addAll(translateQuestion);
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

    //only one choice
    private List<QuestionData> chatMultipleChoiceQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLE_CHOICE);
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "thanks");
        ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(answerItem);
        data.setQuestion(QuestionUtils.formatChatQuestion("無名", chatItems));
        data.setChoices(choices());
        data.setAnswer("no problem");
        data.setAcceptableAnswers(null);

        dataList.add(data);
        return dataList;
    }

    private List<String> choices(){
        List<String> choices = new ArrayList<>(1);
        choices.add("no problem");
        return choices;
    }

    //there are a lot more,
    //but this is just a few
    private List<String> chatAcceptableAnswers(){
        List<String> otherReplies = new ArrayList<>(4);
        otherReplies.add("you're welcome");
        otherReplies.add("no worries");
        otherReplies.add("my pleasure");
        otherReplies.add("any time");
        return otherReplies;
    }

    private FeedbackPair chatFeedBack(){
        List<String> responses = chatAcceptableAnswers();
        String feedback = "他の返答も知っているとはすごいですね！ただ、採点が難しくなるのでレッスンに沿って答えてください。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);

    }

    private List<QuestionData> chatQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.CHAT);
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "thanks");
        ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(answerItem);
        data.setQuestion(QuestionUtils.formatChatQuestion("無名", chatItems));
        data.setChoices(null);
        data.setAnswer("no problem");
        data.setAcceptableAnswers(chatAcceptableAnswers());
        List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
        feedbackPairs.add(chatFeedBack());
        data.setFeedback(feedbackPairs);

        dataList.add(data);
        return dataList;
    }

    private List<String> translateAcceptableAnswers(){
        List<String> otherReplies = new ArrayList<>(1);
        otherReplies.add("thank you");
        return otherReplies;
    }

    private List<QuestionData> spellingQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
        data.setQuestion("ありがとう");
        data.setChoices(null);
        data.setAnswer("thanks");
        data.setAcceptableAnswers(translateAcceptableAnswers());

        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> translateQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion("ありがとう");
        data.setChoices(null);
        data.setAnswer("thanks");
        data.setAcceptableAnswers(translateAcceptableAnswers());

        dataList.add(data);
        return dataList;
    }
}
