package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;


import pelicann.linnca.com.corefunctionality.questions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class Goodbye_bye extends Lesson {
    public static final String KEY = "Goodbye_bye";

    public Goodbye_bye(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> chatMultipleChoice = chatMultipleChoiceQuestions();
        questionSet.addAll(chatMultipleChoice);
        List<List<QuestionData>> spelling = spellingQuestions();
        questionSet.addAll(spelling);
        List<List<QuestionData>> chat = chatQuestions();
        questionSet.addAll(chat);

        return questionSet;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "bye","さようなら",
                "Bye!","さようなら！", KEY));
        words.add(new VocabularyWord("", "goodbye","さようなら",
                "Goodbye!","さようなら！", KEY));
        return words;
    }

    //every choice is correct
    private List<List<QuestionData>> chatMultipleChoiceQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(2);
        List<String> answers = choices();
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
        List<String> choices = new ArrayList<>(2);
        choices.add("goodbye");
        choices.add("bye");
        return choices;
    }

    private List<List<QuestionData>> spellingQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(2);
        List<String> answers = choices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.SPELLING);
            data.setQuestion("さようなら");
            data.setChoices(null);
            data.setAnswer(answer);
            //you technically can spell 'bye' from 'goodbye'
            List<String> alternateAnswers = choices();
            alternateAnswers.remove(answer);
            data.setAcceptableAnswers(alternateAnswers);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<List<QuestionData>> chatQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(2);
        List<String> answers = choices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.CHAT);
            ChatQuestionItem chatItem1 = new ChatQuestionItem(false, "Hello");
            ChatQuestionItem chatItem2 = new ChatQuestionItem(true, "Hello");
            ChatQuestionItem chatItem3 = new ChatQuestionItem(false, answer);
            ChatQuestionItem answerItem = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
            List<ChatQuestionItem> chatItems = new ArrayList<>(4);
            chatItems.add(chatItem1);
            chatItems.add(chatItem2);
            chatItems.add(chatItem3);
            chatItems.add(answerItem);
            data.setQuestion(QuestionSerializer.serializeChatQuestion("無名", chatItems));
            data.setChoices(null);
            data.setAnswer(answer);
            //allow the user to say both 'goodbye' and 'bye'
            List<String> alternateAnswers = choices();
            alternateAnswers.remove(answer);
            data.setAcceptableAnswers(alternateAnswers);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }
}
