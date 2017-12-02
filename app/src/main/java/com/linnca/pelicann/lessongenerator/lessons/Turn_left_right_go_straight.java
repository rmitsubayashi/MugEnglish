package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.Question_Actions;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_Spelling_Suggestive;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class Turn_left_right_go_straight extends Lesson {
    public static final String KEY = "Turn_left_right_go_straight";

    public Turn_left_right_go_straight(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<QuestionData> questions = new ArrayList<>(4);
        List<QuestionData> multipleChoiceQuestion1 = multipleChoiceQuestion1();
        questions.addAll(multipleChoiceQuestion1);
        List<QuestionData> multipleChoiceQuestion2 = multipleChoiceQuestion2();
        questions.addAll(multipleChoiceQuestion2);
        List<QuestionData> spellingQuestion = spellingQuestion();
        questions.addAll(spellingQuestion);
        List<QuestionData> actionQuestion = actionQuestion();
        questions.addAll(actionQuestion);
        for (int i=0; i<4; i++){
            QuestionData data = questions.get(i);
            data.setId(formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>(4);
        for (int i=1; i<=4; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(5);
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "turn"),
                "turn","曲がる","Turn left.","左折してください。", KEY));
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "left"),
                "left","左","Turn left.","左折してください。", KEY));
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "right"),
                "right","右","Turn right.","右折してください。", KEY));
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "straight"),
                "straight","まっすぐ","Go straight.","まっすぐに行ってください。", KEY));
        words.add(new VocabularyWord(formatGenericQuestionVocabularyID(lessonKey, "go"),
                "go","行く","Go straight.","まっすぐに行ってください。", KEY));
        return words;
    }

    @Override
    protected List<String> getGenericQuestionVocabularyIDs(){
        List<String> ids =new ArrayList<>(5);
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "turn"));
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "left"));
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "right"));
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "go"));
        ids.add(formatGenericQuestionVocabularyID(lessonKey, "straight"));
        return ids;
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
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<String> getActions(){
        List<String> actions = new ArrayList<>(5);
        actions.add("go straight");
        actions.add("turn left");
        actions.add("turn left");
        actions.add("go straight");
        actions.add("turn right");
        return actions;
    }


}
