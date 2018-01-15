package pelicann.linnca.com.corefunctionality.lessongeneration.lessons;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.lessongeneration.Lesson;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.questions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class Walk_run extends Lesson {
    public static final String KEY = "Walk_run";

    public Walk_run(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
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
        List<QuestionData> spellingSuggestiveQuestion1 = spellingSuggestiveQuestion1();
        questionSet.add(spellingSuggestiveQuestion1);
        List<QuestionData> spellingSuggestiveQuestion2 = spellingSuggestiveQuestion2();
        questionSet.add(spellingSuggestiveQuestion2);
        List<QuestionData> actionQuestion = actionQuestion();
        questionSet.add(actionQuestion);
        List<QuestionData> translate1 = translateQuestion1();
        questionSet.add(translate1);
        List<QuestionData> translate2 = translateQuestion2();
        questionSet.add(translate2);

        return questionSet;

    }

    @Override
    protected void shufflePreGenericQuestions(List<List<QuestionData>> preGenericQuestions){
        List<List<QuestionData>> spelling = preGenericQuestions.subList(0,2);
        Collections.shuffle(spelling);
        List<List<QuestionData>> translate = preGenericQuestions.subList(3,5);
        Collections.shuffle(translate);
    }

    @Override
    protected List<VocabularyWord> getGenericQuestionVocabulary(){
        List<VocabularyWord> words = new ArrayList<>(2);
        words.add(new VocabularyWord("", "run","走る",
                "Run.","走りなさい。", KEY));
        words.add(new VocabularyWord("", "walk","歩く",
                "Walk.","歩きなさい。", KEY));
        return words;
    }

    private List<QuestionData> spellingSuggestiveQuestion1(){
        String question = "歩く";
        String answer = "walk";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
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

    private List<QuestionData> spellingSuggestiveQuestion2(){
        String question = "走る";
        String answer = "run";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
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

        List<QuestionData> questionVariations = new ArrayList<>();
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
        actions.add("run");
        actions.add("walk");
        actions.add("run");
        actions.add("walk");
        actions.add("run");
        return actions;
    }

    private List<String> getActions2(){
        List<String> actions = new ArrayList<>(5);
        actions.add("walk");
        actions.add("run");
        actions.add("walk");
        actions.add("walk");
        actions.add("run");
        return actions;
    }

    private List<QuestionData> translateQuestion1(){
        String question = "歩きなさい";
        String answer = "walk";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
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
        String question = "走りなさい";
        String answer = "run";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);

        data.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }
}
