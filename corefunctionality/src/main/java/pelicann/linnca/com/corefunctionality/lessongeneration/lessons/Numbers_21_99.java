package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.lessongeneration.StringUtils;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;


public class Numbers_21_99 extends Lesson {
    public static final String KEY = "Numbers_21_99";

    public Numbers_21_99(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questionSet = new ArrayList<>();
        List<QuestionData> spellingQuestion1 = spellingQuestion1();
        questionSet.add(spellingQuestion1);
        List<QuestionData> spellingQuestion2 = spellingQuestion2();
        questionSet.add(spellingQuestion2);
        List<QuestionData> spellingQuestion3 = spellingQuestion3();
        questionSet.add(spellingQuestion3);
        List<QuestionData> translateQuestion1 = translateQuestion1();
        questionSet.add(translateQuestion1);
        List<QuestionData> translateQuestion2 = translateQuestion2();
        questionSet.add(translateQuestion2);
        List<QuestionData> translateQuestion3 = translateQuestion3();
        questionSet.add(translateQuestion3);

        return questionSet;
    }

    private List<QuestionData> translateQuestion1(){
        List<QuestionData> questions = new ArrayList<>(27);
        for (int i=21; i<47; i++) {
            if (i % 10 == 0){
                //we are not covering tens in this lesson
                continue;
            }
            QuestionData data = new QuestionData();
            String answer = StringUtils.convertIntToWord(i);
            //also accept twenty two (not just twenty-two)
            String acceptableAnswer = answer.replace("-", " ");
            List<String> acceptableAnswers = new ArrayList<>(1);
            acceptableAnswers.add(acceptableAnswer);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> translateQuestion2(){
        List<QuestionData> questions = new ArrayList<>(27);
        for (int i=47; i<73; i++) {
            if (i % 10 == 0){
                //we are not covering tens in this lesson
                continue;
            }
            QuestionData data = new QuestionData();
            String answer = StringUtils.convertIntToWord(i);
            //also accept twenty two (not just twenty-two)
            String acceptableAnswer = answer.replace("-", " ");
            List<String> acceptableAnswers = new ArrayList<>(1);
            acceptableAnswers.add(acceptableAnswer);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> translateQuestion3(){
        List<QuestionData> questions = new ArrayList<>(27);
        for (int i=73; i<=99; i++) {
            if (i % 10 == 0){
                //we are not covering tens in this lesson
                continue;
            }
            QuestionData data = new QuestionData();
            String answer = StringUtils.convertIntToWord(i);
            //also accept twenty two (not just twenty-two)
            String acceptableAnswer = answer.replace("-", " ");
            List<String> acceptableAnswers = new ArrayList<>(1);
            acceptableAnswers.add(acceptableAnswer);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> spellingQuestion1(){
        List<QuestionData> questions = new ArrayList<>(27);
        for (int i=21; i<43; i++) {
            QuestionData data = new QuestionData();
            String answer = StringUtils.convertIntToWord(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.SPELLING);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> spellingQuestion2(){
        List<QuestionData> questions = new ArrayList<>(27);
        for (int i=47; i<73; i++) {
            QuestionData data = new QuestionData();
            String answer = StringUtils.convertIntToWord(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.SPELLING);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> spellingQuestion3(){
        List<QuestionData> questions = new ArrayList<>(27);
        for (int i=73; i<=99; i++) {
            QuestionData data = new QuestionData();
            String answer = StringUtils.convertIntToWord(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(QuestionTypeMappings.SPELLING);
            data.setQuestion(Integer.toString(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }
}
