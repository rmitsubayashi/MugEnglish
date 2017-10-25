package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
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

/*
* This lesson only uses the three terms
* so no dynamic content
* */
public class Numbers_4_6 extends Lesson {
    public static final String KEY = "Numbers_4_6";

    public Numbers_4_6(WikiBaseEndpointConnector connector, LessonListener listener){
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
        List<QuestionData> questions = new ArrayList<>();
        List<QuestionData> multipleChoiceQuestions = multipleChoiceQuestions();
        questions.addAll(multipleChoiceQuestions);
        List<QuestionData> translateQuestions = translateQuestions();
        questions.addAll(translateQuestions);
        List<QuestionData> fillInBlankQuestions = fillInBlankQuestions();
        questions.addAll(fillInBlankQuestions);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<Integer> ids = new ArrayList<>(9);
        List<Integer> temp = new ArrayList<>(3);
        for (int i=0; i<3; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        for (Integer i : temp){
            ids.add(i+1);
        }
        Collections.shuffle(temp);
        for (Integer i : temp){
            ids.add(i+4);
        }
        Collections.shuffle(temp);
        for (Integer i : temp){
            ids.add(i+7);
        }

        List<List<String>> questionSet = new ArrayList<>();
        for (Integer i : ids) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    private List<String> japanese(){
        List<String> choices = new ArrayList<>(3);
        choices.add("四");
        choices.add("五");
        choices.add("六");
        return choices;
    }

    private List<String> numbers(){
        List<String> choices = new ArrayList<>(3);
        choices.add("4");
        choices.add("5");
        choices.add("6");
        return choices;
    }

    private List<String> english(){
        List<String> choices = new ArrayList<>(3);
        choices.add("four");
        choices.add("five");
        choices.add("six");
        return choices;
    }

    private List<QuestionData> translateQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = english();
        List<String> numbers = japanese();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setVocabulary(new ArrayList<String>());
            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = numbers();
        List<String> numbers = english();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
            data.setQuestion(numbers.get(i) + " = " + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setVocabulary(new ArrayList<String>());
            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> multipleChoiceQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.MULTIPLE_CHOICE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setChoices(answers);
            data.setAcceptableAnswers(null);
            data.setVocabulary(new ArrayList<String>());
            questions.add(data);
        }

        return questions;
    }
}
