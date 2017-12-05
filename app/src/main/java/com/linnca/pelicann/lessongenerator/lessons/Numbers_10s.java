package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Numbers_10s extends Lesson {
    public static final String KEY = "Numbers_10s";

    public Numbers_10s(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questions = new ArrayList<>();
        List<List<QuestionData>> spellingQuestions = spellingQuestions();
        questions.addAll(spellingQuestions);
        List<QuestionData> fillInBlankQuestion1 = fillInBlankQuestion1();
        questions.add(fillInBlankQuestion1);
        List<QuestionData> fillInBlankQuestion2 = fillInBlankQuestion2();
        questions.add(fillInBlankQuestion2);

        return questions;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(9);
        List<String> wordList = english();
        List<String> translationList = numbers();
        for (int i=0; i<9; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord("", word, translation,
                    "", "", KEY));
        }
        return words;
    }

    private List<String> english(){
        List<String> choices = new ArrayList<>(9);
        choices.add("ten");
        choices.add("twenty");
        choices.add("thirty");
        choices.add("forty");
        choices.add("fifty");
        choices.add("sixty");
        choices.add("seventy");
        choices.add("eighty");
        choices.add("ninety");
        return choices;
    }

    private List<String> numbers(){
        List<String> choices = new ArrayList<>(9);
        choices.add("10");
        choices.add("20");
        choices.add("30");
        choices.add("40");
        choices.add("50");
        choices.add("60");
        choices.add("70");
        choices.add("80");
        choices.add("90");
        return choices;
    }

    private List<List<QuestionData>> spellingQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(9);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<9; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_Spelling.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    //missing some but whatever
    private List<String> fillInBlankQuestionQuestions(){
        List<String> questions = new ArrayList<>();
        //addition
        questions.add("twenty + seventy = ");
        questions.add("twenty + fifty = ");
        questions.add("sixty + ten = ");
        questions.add("thirty + ten = ");
        questions.add("forty + fifty = ");
        questions.add("ten + eighty = ");
        questions.add("thirty + forty = ");
        questions.add("sixty + ten = ");
        //subtraction
        questions.add("ninety - fifty = ");
        questions.add("seventy - ten = ");
        questions.add("eighty - sixty = ");
        questions.add("seventy - twenty = ");
        questions.add("ninety - fifty = ");
        questions.add("ninety - eighty = ");
        questions.add("forty - ten = ");
        questions.add("twenty - ten = ");
        questions.add("thirty - twenty = ");
        questions.add("fifty - forty = ");

        return questions;

    }

    //missing some but whatever
    private List<String> fillInBlankQuestionAnswers(){
        List<String> answers = new ArrayList<>();
        //addition
        answers.add("ninety");
        answers.add("seventy");
        answers.add("seventy");
        answers.add("forty");
        answers.add("ninety");
        answers.add("ninety");
        answers.add("seventy");
        answers.add("seventy");
        //subtraction
        answers.add("forty");
        answers.add("sixty");
        answers.add("twenty");
        answers.add("fifty");
        answers.add("forty");
        answers.add("ten");
        answers.add("thirty");
        answers.add("ten");
        answers.add("ten");
        answers.add("ten");

        return answers;

    }

    private List<QuestionData> fillInBlankQuestion1(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = fillInBlankQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int limit = equation.size() / 2;
        for (int i=0; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestion2(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = fillInBlankQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int start = equation.size() / 2;
        int limit = equation.size();
        for (int i=start; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }
}
