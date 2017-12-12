package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
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
public class Numbers_0_3 extends Lesson {
    public static final String KEY = "Numbers_0_3";

    public Numbers_0_3(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        //every number
        List<List<QuestionData>> translateQuestions = translateQuestions();
        questionSet.addAll(translateQuestions);
        //out of all the equation questions, we only want three questions
        List<QuestionData> multipleChoiceQuestion1 = multipleChoiceQuestions1();
        questionSet.add(multipleChoiceQuestion1);
        List<QuestionData> multipleChoiceQuestion2 = multipleChoiceQuestions2();
        questionSet.add(multipleChoiceQuestion2);
        List<QuestionData> multipleChoiceQuestion3 = multipleChoiceQuestions3();
        questionSet.add(multipleChoiceQuestion3);
        //same with the fill in the blank
        List<QuestionData> fillInBlankQuestion1 = fillInBlankQuestions1();
        questionSet.add(fillInBlankQuestion1);
        List<QuestionData> fillInBlankQuestion2 = fillInBlankQuestions2();
        questionSet.add(fillInBlankQuestion2);
        List<QuestionData> fillInBlankQuestion3 = fillInBlankQuestions3();
        questionSet.add(fillInBlankQuestion3);

        return questionSet;
    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> translate = preGenericQuestions.subList(0,4);
        Collections.shuffle(translate);
        List<List<QuestionData>> multipleChoice = preGenericQuestions.subList(4,7);
        Collections.shuffle(multipleChoice);
        List<List<QuestionData>> fillInBlank = preGenericQuestions.subList(7,10);
        Collections.shuffle(fillInBlank);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(4);
        List<String> wordList = translateAnswers();
        List<String> translationList = translateNumbers();
        for (int i=0; i<4; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord("",
                    word, translation, "", "", KEY));
        }
        return words;
    }

    private List<String> translateAnswers(){
        List<String> choices = new ArrayList<>(4);
        choices.add("zero");
        choices.add("one");
        choices.add("two");
        choices.add("three");
        return choices;
    }

    private List<String> translateNumbers(){
        List<String> choices = new ArrayList<>(4);
        choices.add("0");
        choices.add("1");
        choices.add("2");
        choices.add("3");
        return choices;
    }

    private List<List<QuestionData>> translateQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(4);
        List<String> answers = translateAnswers();
        List<String> numbers = translateNumbers();
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
    
    //missing some but whatever
    private List<String> multipleChoiceQuestionQuestions(){
        List<String> questions = new ArrayList<>();
        //addition
        questions.add("one + one = ");
        questions.add("one + two = ");
        questions.add("two + one = ");
        questions.add("one + zero = ");
        questions.add("zero + one = ");
        questions.add("two + zero = ");
        questions.add("zero + two = ");
        questions.add("zero + zero = ");
        //subtraction
        questions.add("two - one = ");
        questions.add("three - one = ");
        questions.add("one - one = ");
        questions.add("three - two = ");
        questions.add("three - zero = ");
        questions.add("zero - zero = ");
        questions.add("three - three = ");
        //multiplication
        questions.add("two × one = ");
        questions.add("one × zero = ");
        questions.add("one × three = ");
        questions.add("one × one = ");
        
        return questions;
        
    }

    //missing some but whatever
    private List<String> multipleChoiceQuestionAnswers(){
        List<String> answers = new ArrayList<>();
        //addition
        answers.add("two");
        answers.add("three");
        answers.add("three");
        answers.add("one");
        answers.add("one");
        answers.add("two");
        answers.add("two");
        answers.add("zero");
        //subtraction
        answers.add("one");
        answers.add("two");
        answers.add("zero");
        answers.add("one");
        answers.add("three");
        answers.add("zero");
        answers.add("zero");
        //multiplication
        answers.add("two");
        answers.add("zero");
        answers.add("three");
        answers.add("one");

        return answers;

    }

    private List<String> multipleChoiceChoices(){
        List<String> choices = new ArrayList<>(4);
        choices.add("zero");
        choices.add("one");
        choices.add("two");
        choices.add("three");
        return choices;
    }

    //we have three questions so divide the equations up into three parts
    private List<QuestionData> multipleChoiceQuestions1(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equations = multipleChoiceQuestionQuestions();
        List<String> answers = multipleChoiceQuestionAnswers();
        int limit = equations.size() / 3;
        for (int i=0; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(equations.get(i));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> multipleChoiceQuestions2(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equations = multipleChoiceQuestionQuestions();
        List<String> answers = multipleChoiceQuestionAnswers();
        int start = equations.size() / 3;
        int limit = start * 2;
        for (int i=start; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(equations.get(i));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> multipleChoiceQuestions3(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equations = multipleChoiceQuestionQuestions();
        List<String> answers = multipleChoiceQuestionAnswers();
        int start = (equations.size() / 3) * 2;
        int limit = equations.size();
        for (int i=start; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_MultipleChoice.QUESTION_TYPE);
            data.setQuestion(equations.get(i));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    //missing some but whatever
    private List<String> fillInBlankQuestionAnswers(){
        List<String> answers = new ArrayList<>();
        //addition
        answers.add("2");
        answers.add("3");
        answers.add("3");
        answers.add("1");
        answers.add("1");
        answers.add("2");
        answers.add("2");
        answers.add("0");
        //subtraction
        answers.add("1");
        answers.add("2");
        answers.add("0");
        answers.add("1");
        answers.add("3");
        answers.add("0");
        answers.add("0");
        //multiplication
        answers.add("2");
        answers.add("0");
        answers.add("3");
        answers.add("1");

        return answers;

    }

    private List<QuestionData> fillInBlankQuestions1(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = multipleChoiceQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int limit = equation.size() / 3;
        for (int i=0; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestions2(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = multipleChoiceQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int start = equation.size() / 3;
        int limit = start * 2;
        for (int i=start; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestions3(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = multipleChoiceQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int start = (equation.size() / 3) * 2;
        int limit = equation.size();
        for (int i=start; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }
}
