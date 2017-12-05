package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_MultipleChoice;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

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

    public Numbers_4_6(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questionSet = new ArrayList<>();
        List<List<QuestionData>> multipleChoiceQuestions = multipleChoiceQuestions();
        questionSet.addAll(multipleChoiceQuestions);
        List<List<QuestionData>> translateQuestions = translateQuestions();
        questionSet.addAll(translateQuestions);
        List<List<QuestionData>> fillInBlankQuestions = fillInBlankQuestions();
        questionSet.addAll(fillInBlankQuestions);

        return questionSet;

    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(3);
        List<String> wordList = english();
        List<String> translationList = numbers();
        for (int i=0; i<3; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord("",
                    word, translation, "", "", KEY));
        }
        return words;
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

    private List<List<QuestionData>> translateQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = english();
        List<String> numbers = japanese();
        for (int i=0; i<3; i++) {
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

    private List<List<QuestionData>> fillInBlankQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = numbers();
        List<String> numbers = english();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(numbers.get(i) + " = " + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }

    private List<List<QuestionData>> multipleChoiceQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setChoices(answers);
            data.setAcceptableAnswers(null);

            List<QuestionData> dataList = new ArrayList<>();
            dataList.add(data);
            questions.add(dataList);
        }

        return questions;
    }
}
