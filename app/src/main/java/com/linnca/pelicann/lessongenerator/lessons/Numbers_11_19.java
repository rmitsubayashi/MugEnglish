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


public class Numbers_11_19 extends Lesson {
    public static final String KEY = "Numbers_11_19";

    public Numbers_11_19(WikiBaseEndpointConnector connector, LessonListener listener){
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
        List<QuestionData> spellingQuestions = spellingQuestions();
        questions.addAll(spellingQuestions);
        List<QuestionData> translateQuestions = translateQuestions();
        questions.addAll(translateQuestions);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<Integer> ids = new ArrayList<>(18);
        List<Integer> temp = new ArrayList<>(9);
        for (int i=0; i<9; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        for (Integer i : temp){
            ids.add(i+1);
            ids.add(i+10);
        }

        List<List<String>> questionSet = new ArrayList<>();
        for (Integer i : ids) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    private List<String> numbers(){
        List<String> choices = new ArrayList<>(9);
        choices.add("11");
        choices.add("12");
        choices.add("13");
        choices.add("14");
        choices.add("15");
        choices.add("16");
        choices.add("17");
        choices.add("18");
        choices.add("19");
        return choices;
    }

    private List<String> english(){
        List<String> choices = new ArrayList<>(9);
        choices.add("eleven");
        choices.add("twelve");
        choices.add("thirteen");
        choices.add("fourteen");
        choices.add("fifteen");
        choices.add("sixteen");
        choices.add("seventeen");
        choices.add("eighteen");
        choices.add("nineteen");
        return choices;
    }

    private List<QuestionData> translateQuestions(){
        List<QuestionData> questions = new ArrayList<>(9);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<9; i++) {
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

    private List<QuestionData> spellingQuestions(){
        List<QuestionData> questions = new ArrayList<>(9);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<9; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);
            data.setVocabulary(new ArrayList<String>());
            questions.add(data);
        }

        return questions;
    }
}
