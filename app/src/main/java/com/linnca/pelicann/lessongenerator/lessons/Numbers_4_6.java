package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_MultipleChoice;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.questions.Question_TrueFalse;
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
        List<List<QuestionData>> multipleChoice = multipleChoiceQuestions();
        questionSet.addAll(multipleChoice);
        List<List<QuestionData>> translate = translateQuestions();
        questionSet.addAll(translate);
        List<List<QuestionData>> fillInBlank = fillInBlankQuestions();
        questionSet.addAll(fillInBlank);
        List<List<QuestionData>> trueFalse = trueFalseQuestions();
        questionSet.addAll(trueFalse);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoice = preGenericQuestions.subList(0,3);
        Collections.shuffle(multipleChoice);
        List<List<QuestionData>> translate = preGenericQuestions.subList(3,6);
        Collections.shuffle(translate);
        List<List<QuestionData>> fillInBlank = preGenericQuestions.subList(6,9);
        Collections.shuffle(fillInBlank);
        List<List<QuestionData>> trueFalse = preGenericQuestions.subList(9,12);
        Collections.shuffle(trueFalse);
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

    private List<List<QuestionData>> trueFalseQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> english = english();
        List<String> japanese = japanese();
        for (int i=0; i<3; i++) {
            List<QuestionData> dataList = new ArrayList<>();
            //one true question and one false question
            QuestionData data = new QuestionData();
            String question = english.get(i) + " = " + japanese.get(i);
            String answer = Question_TrueFalse.getTrueFalseString(true);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
            data.setQuestion(question);
            data.setAnswer(answer);
            data.setChoices(null);
            data.setAcceptableAnswers(null);
            dataList.add(data);
            //false question
            data = new QuestionData();
            question = english.get(i) + " = " + japanese.get((i+1)%3);
            answer = Question_TrueFalse.getTrueFalseString(false);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_TrueFalse.QUESTION_TYPE);
            data.setQuestion(question);
            data.setAnswer(answer);
            data.setChoices(null);
            data.setAcceptableAnswers(null);
            dataList.add(data);

            questions.add(dataList);
        }

        return questions;
    }
}
