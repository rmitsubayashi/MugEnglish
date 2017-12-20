package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.questions.Question_TrueFalse;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Walk_turn_create_PAST extends Lesson {
    public static final String KEY = "Walk_turn_create_PAST";

    public Walk_turn_create_PAST(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questionSet = new ArrayList<>(9);
        List<QuestionData> translate1 = translateQuestion1();
        questionSet.add(translate1);
        List<QuestionData> translate2 = translateQuestion2();
        questionSet.add(translate2);
        List<QuestionData> translate3 = translateQuestion3();
        questionSet.add(translate3);
        List<QuestionData> translate4 = translateQuestion4();
        questionSet.add(translate4);
        List<QuestionData> translate5 = translateQuestion5();
        questionSet.add(translate5);
        List<QuestionData> translate6 = translateQuestion6();
        questionSet.add(translate6);
        List<QuestionData> trueFalse1 = trueFalseQuestion1();
        questionSet.add(trueFalse1);
        List<QuestionData> trueFalse2 = trueFalseQuestion2();
        questionSet.add(trueFalse2);
        List<QuestionData> trueFalse3 = trueFalseQuestion3();
        questionSet.add(trueFalse3);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> translate1 = preGenericQuestions.subList(0,3);
        Collections.shuffle(translate1);
        List<List<QuestionData>> translate2 = preGenericQuestions.subList(3,6);
        Collections.shuffle(translate2);
        List<List<QuestionData>> trueFalse = preGenericQuestions.subList(6,9);
        Collections.shuffle(trueFalse);
    }

    private List<QuestionData> translateQuestion1(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("創りました");
        data.setChoices(null);
        data.setAnswer("created");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> translateQuestion2(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("曲がりました");
        data.setChoices(null);
        data.setAnswer("turned");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> translateQuestion3(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("歩きました");
        data.setChoices(null);
        data.setAnswer("walked");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> translateQuestion4(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("私は芸術品を創りました");
        data.setChoices(null);
        data.setAnswer("I created art");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> translateQuestion5(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("私は右に曲がりました");
        data.setChoices(null);
        data.setAnswer("I turned right");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> translateQuestion6(){
        List<QuestionData> dataList = new ArrayList<>(1);

        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion("私は歩きました");
        data.setChoices(null);
        data.setAnswer("I walked");
        data.setAcceptableAnswers(null);

        dataList.add(data);

        return dataList;
    }

    private List<String> trueFalseJP1(){
        List<String> questions = new ArrayList<>(2);
        questions.add("私は芸術品を創りました。");
        questions.add("私は芸術品を創ります。");
        return questions;
    }

    private List<String> trueFalseEN1(){
        List<String> questions = new ArrayList<>(2);
        questions.add("I created art.");
        questions.add("I create art.");
        return questions;
    }

    private List<QuestionData> trueFalseQuestion1(){
        List<QuestionData> dataList = new ArrayList<>(4);
        List<String> questionJP = trueFalseJP1();
        List<String> questionEN = trueFalseEN1();
        for (int jp=0; jp<=1; jp++) {
            for (int en=0; en<=1; en++) {
                String question = questionJP.get(jp) + "\n" +
                        questionEN.get(en);
                boolean correct = jp == en;
                QuestionData data = new QuestionData();
                data.setId("");
                data.setLessonId(lessonKey);

                data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
                data.setQuestion(question);
                data.setChoices(null);
                data.setAnswer(Question_TrueFalse.getTrueFalseString(correct));
                data.setAcceptableAnswers(null);

                dataList.add(data);
            }
        }

        return dataList;
    }

    private List<String> trueFalseJP2(){
        List<String> questions = new ArrayList<>(2);
        questions.add("私は右に曲がりました。");
        questions.add("私は右に曲がります。");
        return questions;
    }

    private List<String> trueFalseEN2(){
        List<String> questions = new ArrayList<>(2);
        questions.add("I turned right.");
        questions.add("I turn right.");
        return questions;
    }

    private List<QuestionData> trueFalseQuestion2(){
        List<QuestionData> dataList = new ArrayList<>(4);
        List<String> questionJP = trueFalseJP2();
        List<String> questionEN = trueFalseEN2();
        for (int jp=0; jp<=1; jp++) {
            for (int en=0; en<=1; en++) {
                String question = questionJP.get(jp) + "\n" +
                        questionEN.get(en);
                boolean correct = jp == en;
                QuestionData data = new QuestionData();
                data.setId("");
                data.setLessonId(lessonKey);

                data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
                data.setQuestion(question);
                data.setChoices(null);
                data.setAnswer(Question_TrueFalse.getTrueFalseString(correct));
                data.setAcceptableAnswers(null);

                dataList.add(data);
            }
        }

        return dataList;
    }

    private List<String> trueFalseJP3(){
        List<String> questions = new ArrayList<>(2);
        questions.add("私は歩きました。");
        questions.add("私は歩きます。");
        return questions;
    }

    private List<String> trueFalseEN3(){
        List<String> questions = new ArrayList<>(2);
        questions.add("I walked.");
        questions.add("I walk.");
        return questions;
    }

    private List<QuestionData> trueFalseQuestion3(){
        List<QuestionData> dataList = new ArrayList<>(4);
        List<String> questionJP = trueFalseJP3();
        List<String> questionEN = trueFalseEN3();
        for (int jp=0; jp<=1; jp++) {
            for (int en=0; en<=1; en++) {
                String question = questionJP.get(jp) + "\n" +
                        questionEN.get(en);
                boolean correct = jp == en;
                QuestionData data = new QuestionData();
                data.setId("");
                data.setLessonId(lessonKey);

                data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
                data.setQuestion(question);
                data.setChoices(null);
                data.setAnswer(Question_TrueFalse.getTrueFalseString(correct));
                data.setAcceptableAnswers(null);

                dataList.add(data);
            }
        }

        return dataList;
    }

}
