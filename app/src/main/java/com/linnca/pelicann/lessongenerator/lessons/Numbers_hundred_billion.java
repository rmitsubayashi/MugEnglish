package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.StringUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Numbers_hundred_billion extends Lesson {
    public static final String KEY = "Numbers_hundred_billion";

    public Numbers_hundred_billion(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> spellingQuestions = spellingQuestions();
        questionSet.addAll(spellingQuestions);
        List<List<QuestionData>> translateQuestions = translateQuestions();
        questionSet.addAll(translateQuestions);
        List<QuestionData> translateQuestions2_1 = translateQuestions2_1();
        questionSet.add(translateQuestions2_1);
        List<QuestionData> translateQuestions2_2 = translateQuestions2_2();
        questionSet.add(translateQuestions2_2);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> spelling = preGenericQuestions.subList(0,4);
        Collections.shuffle(spelling);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(4);
        List<String> wordList = english();
        List<String> translationList = numbers();
        for (int i=0; i<4; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord("", word, translation,
                    "", "", KEY));
        }
        return words;
    }

    private List<String> numbers(){
        List<String> choices = new ArrayList<>(4);
        choices.add("100");
        choices.add("1,000");
        choices.add("1,000,000");
        choices.add("1,000,000,000");
        return choices;
    }

    private List<String> english(){
        List<String> choices = new ArrayList<>(4);
        choices.add("hundred");
        choices.add("thousand");
        choices.add("million");
        choices.add("billion");
        return choices;
    }

    private List<List<QuestionData>> spellingQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(4);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<4; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<List<QuestionData>> translateQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(4);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<4; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<String> getRandomNumberWords(){
        List<String> numberStrings = new ArrayList<>(20);
        numberStrings.add(StringUtils.convertIntToWord(12000));
        numberStrings.add(StringUtils.convertIntToWord(739));
        numberStrings.add(StringUtils.convertIntToWord(220090));
        numberStrings.add(StringUtils.convertIntToWord(30000));
        numberStrings.add(StringUtils.convertIntToWord(1000000));
        numberStrings.add(StringUtils.convertIntToWord(44444));
        numberStrings.add(StringUtils.convertIntToWord(2100000000));
        numberStrings.add(StringUtils.convertIntToWord(8008));
        numberStrings.add(StringUtils.convertIntToWord(650000));
        numberStrings.add(StringUtils.convertIntToWord(1212));
        numberStrings.add(StringUtils.convertIntToWord(900));
        numberStrings.add(StringUtils.convertIntToWord(500000));
        numberStrings.add(StringUtils.convertIntToWord(10001));
        numberStrings.add(StringUtils.convertIntToWord(70095));
        numberStrings.add(StringUtils.convertIntToWord(400600));
        numberStrings.add(StringUtils.convertIntToWord(300000000));
        numberStrings.add(StringUtils.convertIntToWord(1000000000));
        numberStrings.add(StringUtils.convertIntToWord(23000));
        numberStrings.add(StringUtils.convertIntToWord(1111));
        numberStrings.add(StringUtils.convertIntToWord(100034));

        return numberStrings;
    }

    private List<String> getRandomNumberStrings(){
        List<String> numberStrings = new ArrayList<>(20);
        numberStrings.add("12,000");
        numberStrings.add("739");
        numberStrings.add("220,090");
        numberStrings.add("30,000");
        numberStrings.add("1,000,000");
        numberStrings.add("44,444");
        numberStrings.add("2,100,000,000");
        numberStrings.add("8,008");
        numberStrings.add("650,000");
        numberStrings.add("1,212");
        numberStrings.add("900");
        numberStrings.add("500,000");
        numberStrings.add("10,001");
        numberStrings.add("70,095");
        numberStrings.add("400,600");
        numberStrings.add("300,000,000");
        numberStrings.add("1,000,000,000");
        numberStrings.add("23,000");
        numberStrings.add("1,111");
        numberStrings.add("100,034");

        return numberStrings;
    }

    private List<QuestionData> translateQuestions2_1(){
        List<QuestionData> questions = new ArrayList<>(20);
        List<String> answers = getRandomNumberWords();
        List<String> numbers = getRandomNumberStrings();
        int limit = answers.size() / 2;
        for (int i=0; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            //allow twenty two instead of twenty-two
            String acceptableAnswer = answer.replace("-"," ");
            List<String> acceptableAnswers = new ArrayList<>(1);
            acceptableAnswers.add(acceptableAnswer);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> translateQuestions2_2(){
        List<QuestionData> questions = new ArrayList<>(20);
        List<String> answers = getRandomNumberWords();
        List<String> numbers = getRandomNumberStrings();
        int start = answers.size() / 2;
        int end = answers.size();
        for (int i=start; i<end; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            //allow twenty two instead of twenty-two
            String acceptableAnswer = answer.replace("-"," ");
            List<String> acceptableAnswers = new ArrayList<>(1);
            acceptableAnswers.add(acceptableAnswer);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);

            questions.add(data);
        }

        return questions;
    }
}
