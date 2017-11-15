package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
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
        List<QuestionData> translateQuestions2 = translateQuestions2();
        questions.addAll(translateQuestions2);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<Integer> ids = new ArrayList<>(12);
        List<Integer> temp = new ArrayList<>(20);
        for (int i=0; i<4; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        for (Integer i : temp){
            ids.add(i+1);
            ids.add(i+5);
        }
        temp.clear();
        for (int i=9; i<29; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        for (int i=0; i<4; i++){
            ids.add(temp.get(i));
        }

        List<List<String>> questionSet = new ArrayList<>();
        for (Integer i : ids) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(4);
        List<String> wordList = english();
        List<String> translationList = numbers();
        for (int i=0; i<4; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, word),
                    word, translation, "", "", KEY));
        }
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(4);
        List<String> wordList = english();
        for (String word : wordList) {
            ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, word));
        }
        return ids;
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

    private List<QuestionData> translateQuestions(){
        List<QuestionData> questions = new ArrayList<>(4);
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

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> spellingQuestions(){
        List<QuestionData> questions = new ArrayList<>(4);
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

            questions.add(data);
        }

        return questions;
    }

    private List<String> getRandomNumberWords(){
        List<String> numberStrings = new ArrayList<>(20);
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(12000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(739));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(220090));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(30000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(1000000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(44444));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(2100000000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(8008));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(650000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(1212));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(900));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(500000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(10001));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(70095));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(400600));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(300000000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(1000000000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(23000));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(1111));
        numberStrings.add(LessonGeneratorUtils.convertIntToWord(100034));

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

    private List<QuestionData> translateQuestions2(){
        List<QuestionData> questions = new ArrayList<>(20);
        List<String> answers = getRandomNumberWords();
        List<String> numbers = getRandomNumberStrings();
        for (int i=0; i<20; i++) {
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
