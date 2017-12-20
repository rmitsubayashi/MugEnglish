package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Actions;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stand_up_sit_down extends Lesson {
    public static final String KEY = "Stand_up_sit_down";

    public Stand_up_sit_down(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<List<QuestionData>> questionSet = new ArrayList<>(5);
        List<QuestionData> multipleChoiceQuestion1 = multipleChoiceQuestion1();
        questionSet.add(multipleChoiceQuestion1);
        List<QuestionData> multipleChoiceQuestion2 = multipleChoiceQuestion2();
        questionSet.add(multipleChoiceQuestion2);
        List<QuestionData> translate1 = translateQuestion1();
        questionSet.add(translate1);
        List<QuestionData> translate2 = translateQuestion2();
        questionSet.add(translate2);
        List<QuestionData> actionQuestion = actionQuestion();
        questionSet.add(actionQuestion);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(0,2);
        Collections.shuffle(multipleChoiceQuestions);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "stand up","立つ",
                "Stand up.","立ちなさい。", KEY));
        words.add(new VocabularyWord("", "sit down","座る",
                "Sit down.","座りなさい。", KEY));
        return words;
    }

    private String multipleChoiceQuestionQuestion1(){
        return "座りなさい\nsit " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE;
    }

    private List<String> multipleChoiceQuestionChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("down");
        choices.add("up");
        return choices;
    }

    private List<QuestionData> multipleChoiceQuestion1(){
        String question = multipleChoiceQuestionQuestion1();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "down";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private String multipleChoiceQuestionQuestion2(){
        return "立ちなさい\nstand " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE;
    }

    private List<QuestionData> multipleChoiceQuestion2(){
        String question = multipleChoiceQuestionQuestion2();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "up";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<QuestionData> translateQuestion1(){
        String question = "座りなさい";
        String answer = "sit down";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<QuestionData> translateQuestion2(){
        String question = "立ちなさい";
        String answer = "stand up";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<QuestionData> actionQuestion(){
        String question = "";
        List<String> actions = getActions();
        String answer = Question_Actions.ANSWER_FINISHED;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_Actions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(actions);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);

        //different sequence of actions
        actions = getActions2();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(Question_Actions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(actions);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);
        questionVariations.add(data);

        return questionVariations;
    }

    private List<String> getActions(){
        List<String> actions = new ArrayList<>(5);
        actions.add("stand up");
        actions.add("sit down");
        actions.add("stand up");
        actions.add("sit down");
        actions.add("sit down");
        return actions;
    }

    private List<String> getActions2(){
        List<String> actions = new ArrayList<>(5);
        actions.add("stand up");
        actions.add("sit down");
        actions.add("sit down");
        actions.add("stand up");
        actions.add("sit down");
        return actions;
    }


}
