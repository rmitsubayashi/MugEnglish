package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Chat;
import com.linnca.pelicann.questions.Question_Chat_MultipleChoice;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class Goodbye_bye extends Lesson {
    public static final String KEY = "Goodbye_bye";

    public Goodbye_bye(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = new ArrayList<>(4);
        List<QuestionData> chatQuestions = chatMultipleChoiceQuestions();
        questions.addAll(chatQuestions);
        List<QuestionData> spellingQuestions = spellingQuestions();
        questions.addAll(spellingQuestions);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>();
        for (int i=1; i<=4; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "bye"),
                "bye","さようなら","Bye!","さようなら！", KEY));
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "goodbye"),
                "goodbye","さようなら","Goodbye!","さようなら！", KEY));
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(2);
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "bye"));
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "goodbye"));
        return ids;
    }

    //every choice is correct
    private List<QuestionData> chatMultipleChoiceQuestions(){
        List<QuestionData> questions = new ArrayList<>(2);
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

            questions.add(data);
        }

        return questions;
    }

    private List<String> choices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("goodbye");
        choices.add("bye");
        return choices;
    }

    //every choice is correct
    private List<QuestionData> spellingQuestions(){
        List<QuestionData> questions = new ArrayList<>(2);
        List<String> answers = choices();
        for (String answer : answers) {
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_Spelling.QUESTION_TYPE);
            data.setQuestion("さようなら");
            data.setChoices(null);
            data.setAnswer(answer);
            //you technically can spell 'bye' from 'goodbye'
            List<String> alternateAnswers = choices();
            alternateAnswers.remove(answer);
            data.setAcceptableAnswers(alternateAnswers);

            questions.add(data);
        }

        return questions;
    }
}
