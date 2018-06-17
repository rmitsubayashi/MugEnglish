package pelicann.linnca.com.corefunctionality.lessonquestions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MaxNumberOfQuestionAttemptsHelperTest {
    @Test
    public void getQuestionAttempts_unlimitedAttemptsQuestionType_shouldReturnAttemptCountSetByTheUser(){
        MaxNumberOfQuestionAttemptsHelper.UserGetter userGetter = new MaxNumberOfQuestionAttemptsHelper.UserGetter() {
            @Override
            public int getMaxNumberOfQuestionAttemptsSetByUser() {
                return 1;
            }
        };
        QuestionData unlimitedQuestion = new QuestionData("questionID1", QuestionTypeMappings.SENTENCEPUZZLE,
                "question1", null, "answer1", null, null);

        int maxAttempts = MaxNumberOfQuestionAttemptsHelper.getMaxNumberOfQuestionAttempts(unlimitedQuestion, userGetter);
        assertEquals(1, maxAttempts);
    }

    @Test
    public void getQuestionAttempts_limitedAttemptsQuestionTypeMoreThanAttemptCountSetByUser_shouldReturnAttemptCountSetByTheUser(){
        MaxNumberOfQuestionAttemptsHelper.UserGetter userGetter = new MaxNumberOfQuestionAttemptsHelper.UserGetter() {
            @Override
            public int getMaxNumberOfQuestionAttemptsSetByUser() {
                return 1;
            }
        };
        List<String> choices = new ArrayList<>(4);
        choices.add("choice1");
        choices.add("choice2");
        choices.add("choice3");
        choices.add("answer1");
        QuestionData limitedQuestion = new QuestionData("questionID1", QuestionTypeMappings.MULTIPLECHOICE,
                "question1", choices, "answer1", null, null);

        int maxAttempts = MaxNumberOfQuestionAttemptsHelper.getMaxNumberOfQuestionAttempts(limitedQuestion, userGetter);
        assertEquals(1, maxAttempts);
    }

    @Test
    public void getQuestionAttempts_limitedAttemptsQuestionTypeLessThanAttemptCountSetByUser_shouldReturnQuestionTypeAttemptCount(){
        MaxNumberOfQuestionAttemptsHelper.UserGetter userGetter = new MaxNumberOfQuestionAttemptsHelper.UserGetter() {
            @Override
            public int getMaxNumberOfQuestionAttemptsSetByUser() {
                return 5;
            }
        };
        List<String> choices = new ArrayList<>(4);
        choices.add("choice1");
        choices.add("choice2");
        choices.add("choice3");
        choices.add("answer1");
        QuestionData limitedQuestion = new QuestionData("questionID1", QuestionTypeMappings.MULTIPLECHOICE,
                "question1", choices, "answer1", null, null);

        int maxAttempts = MaxNumberOfQuestionAttemptsHelper.getMaxNumberOfQuestionAttempts(limitedQuestion, userGetter);
        assertEquals(3, maxAttempts);
    }

    @Test
    public void getQuestionAttempts_limitedAttemptsQuestionTypeEqualToAttemptCountSetByUser_shouldReturnQuestionTypeAttemptCount(){
        MaxNumberOfQuestionAttemptsHelper.UserGetter userGetter = new MaxNumberOfQuestionAttemptsHelper.UserGetter() {
            @Override
            public int getMaxNumberOfQuestionAttemptsSetByUser() {
                return 3;
            }
        };
        List<String> choices = new ArrayList<>(4);
        choices.add("choice1");
        choices.add("choice2");
        choices.add("choice3");
        choices.add("answer1");
        QuestionData limitedQuestion = new QuestionData("questionID1", QuestionTypeMappings.MULTIPLECHOICE,
                "question1", choices, "answer1", null, null);

        int maxAttempts = MaxNumberOfQuestionAttemptsHelper.getMaxNumberOfQuestionAttempts(limitedQuestion, userGetter);
        assertEquals(3, maxAttempts);
    }
}
