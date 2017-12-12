package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Actions;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_SentencePuzzle;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Turn_left_right_go_straight extends Lesson {
    public static final String KEY = "Turn_left_right_go_straight";

    public Turn_left_right_go_straight(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<QuestionData> spellingQuestion = spellingQuestion();
        questionSet.add(spellingQuestion);
        List<QuestionData> actionQuestion = actionQuestion();
        questionSet.add(actionQuestion);
        List<QuestionData> sentencePuzzle = createSentencePuzzleQuestion();
        questionSet.add(sentencePuzzle);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(0,2);
        Collections.shuffle(multipleChoiceQuestions);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(5);
        words.add(new VocabularyWord("", "turn","曲がる",
                "Turn left.","左折してください。", KEY));
        words.add(new VocabularyWord("", "left","左",
                "Turn left.","左折してください。", KEY));
        words.add(new VocabularyWord("", "right","右",
                "Turn right.","右折してください。", KEY));
        words.add(new VocabularyWord("", "straight","まっすぐ",
                "Go straight.","まっすぐに行ってください。", KEY));
        words.add(new VocabularyWord("", "go","行く",
                "Go straight.","まっすぐに行ってください。", KEY));
        return words;
    }

    private String multipleChoiceQuestionQuestion1(){
        String sentence1 = "左折してください";
        String sentence2 = "turn " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE;
        return sentence1 + "\n\n" + sentence2;
    }

    private List<String> multipleChoiceQuestionChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("left");
        choices.add("right");
        return choices;
    }

    private List<QuestionData> multipleChoiceQuestion1(){
        String question = multipleChoiceQuestionQuestion1();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "left";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private String multipleChoiceQuestionQuestion2(){
        String sentence1 = "右折してください";
        String sentence2 = "turn " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE;
        return sentence1 + "\n\n" + sentence2;    }

    private List<QuestionData> multipleChoiceQuestion2(){
        String question = multipleChoiceQuestionQuestion2();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "right";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<QuestionData> spellingQuestion(){
        String question = "まっすぐに行ってください";
        String answer = "go straight";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Spelling_Suggestive.QUESTION_TYPE);
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
        List<QuestionData> questionVariations = new ArrayList<>();
        String question = "";
        List<String> actions = getActions();
        String answer = Question_Actions.ANSWER_FINISHED;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Actions.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(actions);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);

        questionVariations.add(data);

        actions = getActions2();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
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
        actions.add("turn right");
        actions.add("go straight");
        actions.add("turn right");
        actions.add("go straight");
        actions.add("turn left");
        return actions;
    }

    private List<String> getActions2(){
        List<String> actions = new ArrayList<>(5);
        actions.add("turn left");
        actions.add("go straight");
        actions.add("turn left");
        actions.add("go straight");
        actions.add("turn right");
        return actions;
    }

    private String sentencePuzzleQuestion(){
        String sentence1 = "道案内をしてください。";
        String sentence2 = "ここをまっすぐに行って、右に曲がって、まっすぐに行ってください。";
        return sentence1 + "\n" + sentence2;
    }

    private String sentencePuzzleAnswer(){
        List<String> answer = new ArrayList<>(3);
        answer.add("go straight");
        answer.add("turn right");
        answer.add("go straight");
        return Question_SentencePuzzle.formatAnswer(answer);
    }

    private String sentencePuzzleQuestion2(){
        String sentence1 = "道案内をしてください。";
        String sentence2 = "ここを左に曲がって、まっすぐに行って、左に曲がってください。";
        return sentence1 + "\n" + sentence2;
    }

    private String sentencePuzzleAnswer2(){
        List<String> answer = new ArrayList<>(3);
        answer.add("turn left");
        answer.add("go straight");
        answer.add("turn left");
        return Question_SentencePuzzle.formatAnswer(answer);
    }

    private List<String> puzzlePieces(){
        //not all will be used
        List<String> pieces = new ArrayList<>(6);
        pieces.add("go straight");
        pieces.add("go straight");
        pieces.add("turn left");
        pieces.add("turn left");
        pieces.add("turn right");
        pieces.add("turn right");
        return pieces;
    }

    private List<QuestionData> createSentencePuzzleQuestion(){
        List<QuestionData> dataList = new ArrayList<>();

        String question = sentencePuzzleQuestion();
        List<String> choices = puzzlePieces();
        String answer = sentencePuzzleAnswer();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        question = sentencePuzzleQuestion2();
        answer = sentencePuzzleAnswer2();
        data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_SentencePuzzle.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        dataList.add(data);

        return dataList;
    }
}
