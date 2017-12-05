package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Chat;
import com.linnca.pelicann.questions.Question_Chat_MultipleChoice;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class Thanks_no_problem extends Lesson {
    public static final String KEY = "Thanks_no_problem";

    public Thanks_no_problem(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.lessonKey = KEY;
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
        List<List<QuestionData>> questionSet = new ArrayList<>(4);
        List<QuestionData> chatMultipleChoiceQuestion = chatMultipleChoiceQuestion();
        questionSet.add(chatMultipleChoiceQuestion);
        List<QuestionData> chatQuestion = chatQuestion();
        questionSet.add(chatQuestion);
        List<QuestionData> spellingQuestion = spellingQuestion();
        questionSet.add(spellingQuestion);
        List<QuestionData> translateQuestion = translateQuestion();
        questionSet.add(translateQuestion);
        return questionSet;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "thanks","ありがとう",
                "Thanks! No problem.","ありがとう！どういたしまして。", KEY));
        words.add(new VocabularyWord("", "no problem",
                "どういたしまして","Thanks! No problem.","ありがとう！どういたしまして。", KEY));
        return words;
    }

    //only one choice
    private List<QuestionData> chatMultipleChoiceQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Chat_MultipleChoice.QUESTION_TYPE);
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "thanks");
        ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(answerItem);
        data.setQuestion(Question_Chat.formatQuestion("無名", chatItems));
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
        data.setQuestionType(Question_Chat.QUESTION_TYPE);
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "thanks");
        ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(answerItem);
        data.setQuestion(Question_Chat.formatQuestion("無名", chatItems));
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
        data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
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
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("ありがとう");
        data.setChoices(null);
        data.setAnswer("thanks");
        data.setAcceptableAnswers(translateAcceptableAnswers());

        dataList.add(data);
        return dataList;
    }
}
