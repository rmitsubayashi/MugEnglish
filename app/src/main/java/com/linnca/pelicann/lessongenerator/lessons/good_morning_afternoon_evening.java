package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.db.Database;
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
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = new ArrayList<>(9);
        List<QuestionData> chatQuestions = chatMultipleChoiceQuestions();
        questions.addAll(chatQuestions);
        List<QuestionData> multipleChoiceQuestions = multipleChoiceQuestions();
        questions.addAll(multipleChoiceQuestions);
        List<QuestionData> fillInBlankQuestions = fillInBlankQuestions();
        questions.addAll(fillInBlankQuestions);
        for (int i=0; i<9; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(3);
        words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "good morning"),
                "good morning","おはよう","Good morning!","おはよう！", KEY));
        words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "good afternoon"),
                "good afternoon","こんにちは","Good afternoon!","こんにちは！", KEY));
        words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "good evening"),
                "good evening","こんばんは","Good evening!","こんばんは！", KEY));
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(3);
        ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "good morning"));
        ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "good afternoon"));
        ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, "good evening"));
        return ids;
    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<Integer> ids = new ArrayList<>(9);
        //the first three chat questions are introductory
        // and should all be displayed first
        List<Integer> temp = new ArrayList<>(3);
        for (int i=1; i<4; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        ids.addAll(temp);
        temp.clear();
        //the multiple choice and fill in blank
        //questions can be alternating
        for (int i=4; i<7; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        for (Integer i : temp) {
            ids.add(i);
            ids.add(i+3);
        }

        List<List<String>> questionSet = new ArrayList<>();
        for (Integer i : ids) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    private List<QuestionData> chatMultipleChoiceQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = multipleChoiceChoices();
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
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
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

    private List<QuestionData> multipleChoiceQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> enAnswers = multipleChoiceChoices();
        List<String> jpAnswers = multipleChoiceChoicesJP();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = enAnswers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.MULTIPLE_CHOICE);
            data.setQuestion(jpAnswers.get(i));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestions(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> enAnswers = multipleChoiceChoices();
        List<String> jpAnswers = multipleChoiceChoicesJP();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = enAnswers.get(i).replace("good ","");
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
            data.setQuestion(jpAnswers.get(i) + "\n\ngood " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }
}
