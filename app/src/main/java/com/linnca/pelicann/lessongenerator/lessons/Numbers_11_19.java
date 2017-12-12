package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Numbers_11_19 extends Lesson {
    public static final String KEY = "Numbers_11_19";

    public Numbers_11_19(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> spelling = preGenericQuestions.subList(0,9);
        Collections.shuffle(spelling);
        List<List<QuestionData>> translate = preGenericQuestions.subList(9,18);
        Collections.shuffle(translate);
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

    private List<List<QuestionData>> translateQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(9);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<9; i++) {
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
}
