package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_ChooseCorrectSpelling;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Numbers_7_9 extends Lesson {
    public static final String KEY = "Numbers_7_9";

    public Numbers_7_9(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<QuestionData> fillInBlankQuestion1 = fillInBlankQuestion1();
        questionSet.add(fillInBlankQuestion1);
        List<QuestionData> fillInBlankQuestion2 = fillInBlankQuestion2();
        questionSet.add(fillInBlankQuestion2);
        List<QuestionData> fillInBlankQuestion2_1 = fillInBlankQuestion2_1();
        questionSet.add(fillInBlankQuestion2_1);
        List<QuestionData> fillInBlankQuestion2_2 = fillInBlankQuestion2_2();
        questionSet.add(fillInBlankQuestion2_2);
        List<QuestionData> sentencePuzzle = createSentencePuzzleQuestion();
        questionSet.add(sentencePuzzle);
        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> spelling = preGenericQuestions.subList(0,3);
        Collections.shuffle(spelling);
        List<List<QuestionData>> fillInBlank1 = preGenericQuestions.subList(3,5);
        Collections.shuffle(fillInBlank1);
        List<List<QuestionData>> fillInBlank2 = preGenericQuestions.subList(5,7);
        Collections.shuffle(fillInBlank2);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(4);
        List<String> wordList = english();
        List<String> translationList = numbers();
        for (int i=0; i<3; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord("", word, translation,
                    "", "", KEY));
        }
        return words;
    }

    private List<String> english(){
        List<String> choices = new ArrayList<>(3);
        choices.add("seven");
        choices.add("eight");
        choices.add("nine");
        return choices;
    }

    private List<String> numbers(){
        List<String> choices = new ArrayList<>(3);
        choices.add("7");
        choices.add("8");
        choices.add("9");
        return choices;
    }

    private List<List<QuestionData>> spellingQuestions(){
        List<List<QuestionData>> questions = new ArrayList<>(3);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(Question_ChooseCorrectSpelling.QUESTION_TYPE);
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
        questions.add("one + seven = ");
        questions.add("four + five = ");
        questions.add("six + two = ");
        questions.add("seven + zero = ");
        questions.add("three + four = ");
        questions.add("nine + zero = ");
        questions.add("five + two = ");
        questions.add("six + three = ");
        //subtraction
        questions.add("nine - one = ");
        questions.add("seven - one = ");
        questions.add("eight - one = ");
        questions.add("seven - two = ");
        questions.add("nine - five = ");
        questions.add("nine - eight = ");
        questions.add("eight - four = ");
        questions.add("nine - two = ");
        questions.add("seven - three = ");
        questions.add("eight - eight = ");

        return questions;

    }

    //missing some but whatever
    private List<String> fillInBlankQuestionAnswers(){
        List<String> answers = new ArrayList<>();
        //addition
        answers.add("eight");
        answers.add("nine");
        answers.add("eight");
        answers.add("seven");
        answers.add("seven");
        answers.add("nine");
        answers.add("seven");
        answers.add("nine");
        //subtraction
        answers.add("eight");
        answers.add("six");
        answers.add("seven");
        answers.add("five");
        answers.add("four");
        answers.add("one");
        answers.add("four");
        answers.add("seven");
        answers.add("four");
        answers.add("zero");

        return answers;

    }

    //missing some but whatever
    private List<String> fillInBlankQuestionAnswers2(){
        List<String> answers = new ArrayList<>();
        //addition
        answers.add("8");
        answers.add("9");
        answers.add("8");
        answers.add("7");
        answers.add("7");
        answers.add("9");
        answers.add("7");
        answers.add("9");
        //subtraction
        answers.add("8");
        answers.add("6");
        answers.add("7");
        answers.add("5");
        answers.add("4");
        answers.add("1");
        answers.add("4");
        answers.add("7");
        answers.add("4");
        answers.add("0");

        return answers;

    }

    private List<QuestionData> fillInBlankQuestion1(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = fillInBlankQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers2();
        int limit = equation.size() / 2;
        for (int i=0; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
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
        List<String> answers = fillInBlankQuestionAnswers2();
        int start = equation.size() / 2;
        int limit = equation.size();
        for (int i=start; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestion2_1(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = fillInBlankQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int limit = equation.size() / 2;
        for (int i=0; i<limit; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);

            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    private List<QuestionData> fillInBlankQuestion2_2(){
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

            data.setQuestionType(Question_FillInBlank_Input.QUESTION_TYPE);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    //cumulative review
    private String sentencePuzzleQuestion(){
        return "小さい数から順に並べてください";
    }

    private List<String> puzzlePieces(){
        List<String> pieces = new ArrayList<>();
        pieces.add("zero");
        pieces.add("one");
        pieces.add("two");
        pieces.add("three");
        pieces.add("four");
        pieces.add("five");
        pieces.add("six");
        pieces.add("seven");
        pieces.add("eight");
        pieces.add("nine");
        return pieces;
    }

    private String puzzlePiecesAnswer(){
        return Question_SentencePuzzle.formatAnswer(puzzlePieces());
    }

    private List<QuestionData> createSentencePuzzleQuestion(){
        String question = this.sentencePuzzleQuestion();
        List<String> choices = this.puzzlePieces();
        String answer = puzzlePiecesAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);

        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }
}
