package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;


import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Find_stand_sit_on_a_bed extends Lesson {
    public static final String KEY = "Find_stand_sit_on_a_bed";

    public Find_stand_sit_on_a_bed(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<QuestionData> multipleChoiceQuestion3 = multipleChoiceQuestion3();
        questionSet.add(multipleChoiceQuestion3);
        List<QuestionData> fillInBlankMultipleChoiceQuestion1 = fillInBlankMultipleChoiceQuestion1();
        questionSet.add(fillInBlankMultipleChoiceQuestion1);
        List<QuestionData> fillInBlankMultipleChoiceQuestion2 = fillInBlankMultipleChoiceQuestion2();
        questionSet.add(fillInBlankMultipleChoiceQuestion2);
        List<QuestionData> fillInBlankMultipleChoiceQuestion3 = fillInBlankMultipleChoiceQuestion3();
        questionSet.add(fillInBlankMultipleChoiceQuestion3);
        List<QuestionData> translateQuestion = translateQuestion();
        questionSet.add(translateQuestion);
        List<QuestionData> actionQuestion = actionQuestion();
        questionSet.add(actionQuestion);

        return questionSet;
    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> multipleChoiceQuestions = preGenericQuestions.subList(0,3);
        Collections.shuffle(multipleChoiceQuestions);
        List<List<QuestionData>> fillInBlankMultipleChoiceQuestions = preGenericQuestions.subList(3,6);
        Collections.shuffle(fillInBlankMultipleChoiceQuestions);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(4);
        words.add(new VocabularyWord("", "bed","ベッド",
                "Sit on the bed.","ベッドに座りなさい", KEY));
        words.add(new VocabularyWord("",
                "find","探す","Find a bed.","ベッドを探しなさい", KEY));
        words.add(new VocabularyWord("", "sit","座る",
                "Sit on the bed.","ベッドに座りなさい", KEY));
        words.add(new VocabularyWord("", "stand","立つ",
                "Stand on the bed.","ベッドの上で立ちなさい", KEY));
        return words;
    }

    private String multipleChoiceQuestionQuestion1(){
        return "座る";
    }

    private List<String> multipleChoiceQuestionChoices(){
        List<String> choices = new ArrayList<>(3);
        choices.add("sit");
        choices.add("stand");
        choices.add("find");
        return choices;
    }

    private List<QuestionData> multipleChoiceQuestion1(){
        String question = multipleChoiceQuestionQuestion1();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "sit";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
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
        return "立つ";
    }

    private List<QuestionData> multipleChoiceQuestion2(){
        String question = multipleChoiceQuestionQuestion2();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "stand";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
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

    private String multipleChoiceQuestionQuestion3(){
        return "探す";
    }

    private List<QuestionData> multipleChoiceQuestion3(){
        String question = multipleChoiceQuestionQuestion3();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "find";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
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

    private String fillInBlankMultipleChoiceQuestionQuestion1(){
        return "Sit " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " a bed.";
    }

    private List<String> fillInBlankMultipleChoiceQuestionChoices(){
        List<String> choices = new ArrayList<>(4);
        choices.add("on");
        choices.add("");
        choices.add("from");
        choices.add("at");
        return choices;
    }

    private List<QuestionData> fillInBlankMultipleChoiceQuestion1(){
        String question = fillInBlankMultipleChoiceQuestionQuestion1();
        List<String> choices = fillInBlankMultipleChoiceQuestionChoices();
        String answer = "on";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
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

    private String fillInBlankMultipleChoiceQuestionQuestion2(){
        return "Stand " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " a bed.";
    }

    private List<QuestionData> fillInBlankMultipleChoiceQuestion2(){
        String question = fillInBlankMultipleChoiceQuestionQuestion2();
        List<String> choices = fillInBlankMultipleChoiceQuestionChoices();
        String answer = "on";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
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

    private String fillInBlankMultipleChoiceQuestionQuestion3(){
        return "Find " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " a bed.";
    }

    private List<QuestionData> fillInBlankMultipleChoiceQuestion3(){
        String question = fillInBlankMultipleChoiceQuestionQuestion3();
        List<String> choices = fillInBlankMultipleChoiceQuestionChoices();
        String answer = "";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
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

    private List<QuestionData> translateQuestion(){
        String question = "ベッド";
        String answer = "bed";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        //for suggestive, we don't need to lowercase everything
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
        String answer = QuestionUniqueMarkers.ACTIONS_ANSWER;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.ACTIONS);
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

        data.setQuestionType(QuestionTypeMappings.ACTIONS);
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
        actions.add("find a bed");
        actions.add("sit on the bed");
        actions.add("stand on the bed");
        actions.add("sit on the bed");
        actions.add("stand on the bed");
        return actions;
    }

    private List<String> getActions2(){
        List<String> actions = new ArrayList<>(5);
        actions.add("find a bed");
        actions.add("stand on the bed");
        actions.add("sit on the bed");
        actions.add("stand on the bed");
        actions.add("sit on the bed");
        return actions;
    }


}
