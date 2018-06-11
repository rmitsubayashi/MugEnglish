package pelicann.linnca.com.corefunctionality.lessonquestions;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerbQuestionManagerTest {
    private VerbQuestionManager manager;
    @Before
    public void init(){
        manager = new VerbQuestionManager(
                new VerbQuestionManager.VerbQuestionManagerListener() {
                    @Override
                    public void onNextQuestion(QuestionData data, int questionIndex, int totalQuestions, boolean isFirstQuestion) {

                    }

                    @Override
                    public void onQuestionsFinished() {

                    }
                }
        );
    }

    @Test
    public void incrementCorrect_shouldIncrementCorrectCt(){
        manager.incrementCorrectCt();
        assertEquals(manager.getCorrectCt(), 1);
    }

    @Test
    public void startQuestions_shouldStartQuestions(){
        assertFalse(manager.isVerbQuestionsStarted());
        manager.startQuestions();
        assertTrue(manager.isVerbQuestionsStarted());
    }

    @Test
    public void onQuestionsFinished_goThroughAllQuestions_shouldBeCalled(){
        final boolean[] called = {false};
        manager = new VerbQuestionManager(new VerbQuestionManager.VerbQuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData data, int questionIndex, int totalQuestions, boolean isFirstQuestion) {

            }

            @Override
            public void onQuestionsFinished() {
                called[0] = true;
            }
        });

        manager.startQuestions();
        int questionCt = manager.getQuestionCt();
        for (int i=0; i<questionCt-1; i++) {
            manager.nextQuestion(false);
        }
        assertFalse(called[0]);
        manager.nextQuestion(false);
        assertTrue(called[0]);
    }

    @Test
    public void onNextQuestion_startQuestions_shouldBeCalled(){
        final boolean[] called = {false};
        manager = new VerbQuestionManager(new VerbQuestionManager.VerbQuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData data, int questionIndex, int totalQuestions, boolean isFirstQuestion) {
                called[0] = true;
            }

            @Override
            public void onQuestionsFinished() {

            }
        });

        manager.startQuestions();
        assertTrue(called[0]);
    }

    //assumes onNextQuestion_startQuestions_shouldBeCalled() passed
    @Test
    public void onNextQuestion_startQuestions_shouldBeFirstQuestion(){
        manager = new VerbQuestionManager(new VerbQuestionManager.VerbQuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData data, int questionIndex, int totalQuestions, boolean isFirstQuestion) {
                assertTrue(isFirstQuestion);
            }

            @Override
            public void onQuestionsFinished() {

            }
        });

        manager.startQuestions();
    }

    //assumes onNextQuestion_startQuestions_shouldBeCalled() passed
    @Test
    public void onNextQuestion_nextQuestion_shouldNotBeFirstQuestion(){
        final boolean[] isFirst = {false, true};
        manager = new VerbQuestionManager(new VerbQuestionManager.VerbQuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData data, int questionIndex, int totalQuestions, boolean isFirstQuestion) {
                isFirst[questionIndex-1] = isFirstQuestion;
            }

            @Override
            public void onQuestionsFinished() {

            }
        });

        manager.startQuestions();
        assertTrue(isFirst[0]);
        manager.nextQuestion(false);
        assertFalse(isFirst[1]);
    }
}
