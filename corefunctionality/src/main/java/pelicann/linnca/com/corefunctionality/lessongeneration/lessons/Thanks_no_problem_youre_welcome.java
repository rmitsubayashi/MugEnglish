package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class Thanks_no_problem_youre_welcome extends Lesson {
    public static final String KEY = "Thanks_no_problem_youre_welcome";

    public Thanks_no_problem_youre_welcome(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questionSet = new ArrayList<>(4);
        List<QuestionData> spellingQuestion = spellingQuestion();
        questionSet.add(spellingQuestion);
        List<QuestionData> translateQuestion = translateQuestion();
        questionSet.add(translateQuestion);
        List<QuestionData> chatMultipleChoiceQuestion = chatMultipleChoiceQuestion();
        questionSet.add(chatMultipleChoiceQuestion);
        List<QuestionData> chatQuestion = chatQuestion();
        questionSet.add(chatQuestion);
        return questionSet;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "thanks","ありがとう",
                "Thanks! No problem.","ありがとう！どういたしまして。", KEY));
        words.add(new VocabularyWord("", "no problem",
                "どういたしまして","Thanks! No problem.","ありがとう！どういたしまして。", KEY));
        words.add(new VocabularyWord("", "you're welcome",
                "どういたしまして","Thanks! You're welcome.","ありがとう！どういたしまして。", KEY));
        return words;
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

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion("ありがとう");
        data.setChoices(null);
        data.setAnswer("thanks");
        data.setAcceptableAnswers(translateAcceptableAnswers());

        dataList.add(data);
        return dataList;
    }

    private String chatMultipleChoiceAnswer(){
        return "no problem";
    }

    private List<String> chatMultipleChoiceAcceptableAnswers(){
        List<String> answers = new ArrayList<>(1);
        answers.add("you're welcome");
        return answers;
    }

    private List<String> chatMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("you're welcome");
        choices.add("no problem");
        return choices;
    }

    //only correct choices
    private List<QuestionData> chatMultipleChoiceQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLECHOICE);
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "thanks");
        ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(answerItem);
        data.setQuestion(QuestionSerializer.serializeChatQuestion("無名", chatItems));
        data.setChoices(chatMultipleChoiceChoices());
        data.setAnswer(chatMultipleChoiceAnswer());
        data.setAcceptableAnswers(chatMultipleChoiceAcceptableAnswers());

        dataList.add(data);
        return dataList;
    }

    //there are a lot more,
    //but this is just a few
    private List<String> chatAcceptableAnswers(){
        List<String> otherReplies = new ArrayList<>(5);
        otherReplies.add("you're welcome");
        otherReplies.add("youre welcome");
        otherReplies.add("no worries");
        otherReplies.add("my pleasure");
        otherReplies.add("any time");
        return otherReplies;
    }

    private FeedbackPair chatFeedBack(){
        List<String> responses = chatAcceptableAnswers();
        responses.remove("you're welcome");
        responses.remove("youre welcome");
        String feedback = "他の返答も知っているとはすごいですね！ただ、採点が難しくなるのでレッスンに沿って答えてください。";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);

    }

    private List<QuestionData> chatQuestion(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(QuestionTypeMappings.CHAT);
        ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "thanks");
        ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(chatItem1);
        chatItems.add(answerItem);
        data.setQuestion(QuestionSerializer.serializeChatQuestion("無名", chatItems));
        data.setChoices(null);
        data.setAnswer("no problem");
        data.setAcceptableAnswers(chatAcceptableAnswers());
        List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
        feedbackPairs.add(chatFeedBack());
        data.setFeedback(feedbackPairs);

        dataList.add(data);
        return dataList;
    }
}
