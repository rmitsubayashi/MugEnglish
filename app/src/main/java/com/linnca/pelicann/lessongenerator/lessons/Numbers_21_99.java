package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_TranslateWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Numbers_21_99 extends Lesson {
    public static final String KEY = "Numbers_21_99";

    public Numbers_21_99(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<Integer> ids = new ArrayList<>(10);
        List<Integer> temp = new ArrayList<>(5);
        for (int i=1; i<=79; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        for (int i=0; i<5; i++){
            ids.add(i+temp.get(i));
            ids.add(i+temp.get(i)+79);
        }

        List<List<String>> questionSet = new ArrayList<>();
        for (Integer i : ids) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    private List<QuestionData> translateQuestions(){
        List<QuestionData> questions = new ArrayList<>(79);
        for (int i=21; i<=99; i++) {
            QuestionData data = new QuestionData();
            String answer = LessonGeneratorUtils.convertIntToWord(i);
            //also accept twenty two (not just twenty-two)
            String acceptableAnswer = answer.replace("-", " ");
            List<String> acceptableAnswers = new ArrayList<>(1);
            acceptableAnswers.add(acceptableAnswer);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> spellingQuestions(){
        List<QuestionData> questions = new ArrayList<>(79);
        for (int i=21; i<=99; i++) {
            QuestionData data = new QuestionData();
            String answer = LessonGeneratorUtils.convertIntToWord(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_Spelling.QUESTION_TYPE);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }
}
