package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_ChooseCorrectSpelling;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
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
        List<QuestionData> fillInBlankQuestions = fillInBlankQuestions();
        questions.addAll(fillInBlankQuestions);
        List<QuestionData> fillInBlankQuestions2 = fillInBlankQuestions2();
        questions.addAll(fillInBlankQuestions2);
        int questionCt = questions.size();
        for (int i=0; i<questionCt; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<Integer> ids = new ArrayList<>(9);
        //the first three questions are introductory
        // and should all be displayed first
        List<Integer> temp = new ArrayList<>(3);
        for (int i=1; i<=3; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        ids.addAll(temp);
        temp.clear();

        int equationSize = fillInBlankQuestionQuestions().size();
        for (int i=0; i<equationSize; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        //we want four questions
        for (int i=0; i<4; i++) {
            ids.add(temp.get(i) + 4);
        }
        //four different ones
        for (int i=5; i<8; i++){
            ids.add(temp.get(i) + 4 + equationSize);
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
        for (int i=0; i<3; i++) {
            String word = wordList.get(i);
            String translation = translationList.get(i);
            words.add(new VocabularyWord(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, word),
                    word, translation, "", "", KEY));
        }
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(3);
        List<String> wordList = english();
        for (String word : wordList) {
            ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, word));
        }
        return ids;
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

    private List<QuestionData> spellingQuestions(){
        List<QuestionData> questions = new ArrayList<>(3);
        List<String> answers = english();
        List<String> numbers = numbers();
        for (int i=0; i<3; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(Question_ChooseCorrectSpelling.QUESTION_TYPE);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
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

    private List<QuestionData> fillInBlankQuestions(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = fillInBlankQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers2();
        int equationSize = equation.size();
        for (int i=0; i<equationSize; i++) {
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
        List<String> equation = fillInBlankQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers();
        int equationSize = equation.size();
        for (int i=0; i<equationSize; i++) {
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
