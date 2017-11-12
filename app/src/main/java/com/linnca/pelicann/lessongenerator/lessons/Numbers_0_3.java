package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
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
        List<QuestionData> translateQuestions = translateQuestions();
        questions.addAll(translateQuestions);
        List<QuestionData> multipleChoiceQuestions = multipleChoiceQuestions();
        questions.addAll(multipleChoiceQuestions);
        List<QuestionData> fillInBlankQuestions = fillInBlankQuestions();
        questions.addAll(fillInBlankQuestions);
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
        List<Integer> temp = new ArrayList<>(4);
        for (int i=1; i<=4; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        ids.addAll(temp);
        temp.clear();

        int equationSize = multipleChoiceQuestionQuestions().size();
        for (int i=0; i<equationSize; i++){
            temp.add(i);
        }
        Collections.shuffle(temp);
        //we want four questions
        for (int i=0; i<4; i++) {
            ids.add(temp.get(i) + 5);
        }
        //four different ones
        for (int i=5; i<8; i++){
            ids.add(temp.get(i) + 5 + equationSize);
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
        List<String> wordList = translateAnswers();
        List<String> translationList = translateNumbers();
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
        List<String> wordList = translateAnswers();
        for (String word : wordList) {
            ids.add(LessonGeneratorUtils.formatGenericQuestionVocabularyID(lessonKey, word));
        }
        return ids;
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

    private List<QuestionData> translateQuestions(){
        List<QuestionData> questions = new ArrayList<>(4);
        List<String> answers = translateAnswers();
        List<String> numbers = translateNumbers();
        for (int i=0; i<4; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
            data.setQuestion(numbers.get(i));
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
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

    private List<QuestionData> multipleChoiceQuestions(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equations = multipleChoiceQuestionQuestions();
        List<String> answers = multipleChoiceQuestionAnswers();
        int equationsSize = equations.size();
        for (int i=0; i<equationsSize; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.MULTIPLE_CHOICE);
            data.setQuestion(equations.get(i));
            data.setChoices(multipleChoiceChoices());
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }

    //missing some but whatever
    private List<String> fillInBlankQuestionAnswers2(){
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

    private List<QuestionData> fillInBlankQuestions(){
        List<QuestionData> questions = new ArrayList<>();
        List<String> equation = multipleChoiceQuestionQuestions();
        List<String> answers = fillInBlankQuestionAnswers2();
        int equationSize = equation.size();
        for (int i=0; i<equationSize; i++) {
            QuestionData data = new QuestionData();
            String answer = answers.get(i);
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(TOPIC_GENERIC_QUESTION);
            data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
            data.setQuestion(equation.get(i) + Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(null);

            questions.add(data);
        }

        return questions;
    }
}
