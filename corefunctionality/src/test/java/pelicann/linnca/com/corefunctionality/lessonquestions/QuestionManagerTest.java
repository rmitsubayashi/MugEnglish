package pelicann.linnca.com.corefunctionality.lessonquestions;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.db.MockFirebaseDB;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class QuestionManagerTest {
    private QuestionManager questionManager;
    private MockFirebaseDB db;

    @Before
    public void init(){
        db = new MockFirebaseDB();
    }

    @Test
    public void startQuestions_startQuestions_shouldCallOnNextQuestionMethodInListener(){
        final boolean[] called = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                called[0] = true;
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(listener);
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);
        questionManager.startQuestions(questions, lessonInstanceData);
        assertTrue(called[0]);
    }

    @Test
    public void startQuestions_withLessonInstance_firstQuestionShouldBeFirstQuestionInLessonInstance(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                assertTrue(questionData.getId().equals("questionID1"));
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                //not testing this
            }
            
        };
        questionManager = new QuestionManager(listener);
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);

        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        questionManager.startQuestions(questions, lessonInstanceData);

    }

    @Test
    public void startQuestions_withAQuestion_firstQuestionShouldHaveQuestionIndexOfOne(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                assertEquals(1, questionNumber);
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(listener);
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);

        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        questionManager.startQuestions(questions, lessonInstanceData);
    }

    @Test
    public void goToNextQuestions_withTwoQuestions_nextQuestionShouldBeSecondQuestionInLessonInstance(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (questionNumber == 1)
                    assertTrue(questionData.getId().equals("questionID1"));
                if (questionNumber == 2)
                    assertTrue(questionData.getId().equals("questionID2"));
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(listener);
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question2", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(secondQuestion);
        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        //q1
        questionManager.startQuestions(questions, lessonInstanceData);
        //q2
        questionManager.nextQuestion();

    }

    @Test
    public void goToNextQuestions_withTwoQuestions_nextQuestionShouldHaveQuestionNumberOfTwo(){
        final boolean[] twoCalled = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (questionNumber == 2){
                    twoCalled[0] = true;
                }
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                //not testing this
            }
        };
        questionManager = new QuestionManager(listener);
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question2", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(secondQuestion);
        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        //q1
        questionManager.startQuestions(questions, lessonInstanceData);
        //q2
        questionManager.nextQuestion();
        assertTrue(twoCalled[0]);
    }

    @Test
    public void endQuestions_goThroughAllQuestions_shouldCallOnQuestionsFinished(){
        final boolean[] called = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                //not testing this
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                called[0] = true;
            }

            
        };
        questionManager = new QuestionManager(listener);
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question2", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(secondQuestion);
        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        //q1
        questionManager.startQuestions(questions, lessonInstanceData);
        //q2
        questionManager.nextQuestion();
        //end questions
        questionManager.nextQuestion();
        assertTrue(called[0]);
    }

    @Test
    public void goThroughQuestions_dontcallNextQuestion_shouldNotCallOnQuestionsFinished(){
        final boolean[] called = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                //not testing this
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, List<QuestionData> missedQuestions) {
                called[0] = true;
            }


        };
        questionManager = new QuestionManager(listener);
        List<QuestionData> questions = new ArrayList<>();
        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question1", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  QuestionTypeMappings.TRUEFALSE,
                "question2", null, QuestionSerializer.serializeTrueFalseAnswer(true), null, null);
        questions.add(secondQuestion);
        LessonInstanceData lessonInstanceData = new LessonInstanceData();
        //q1
        questionManager.startQuestions(questions, lessonInstanceData);
        //q2
        questionManager.nextQuestion();
        assertFalse(called[0]);
    }

}
