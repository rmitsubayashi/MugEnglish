package com.linnca.pelicann.questions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionResponseCheckerTest {

    @Test
    public void checkResponse_correctAnswer_shouldReturnCorrect(){
        String answer = "correct";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String correctResponse = "correct";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, correctResponse));

    }

    @Test
    public void checkResponse_incorrectCorrectAnswer_shouldReturnIncorrect(){
        String answer = "correct";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String incorrectResponse = "incorrect";
        assertFalse(QuestionResponseChecker.checkResponse(questionData, incorrectResponse));

    }

    @Test
    public void checkResponse_correctAnswerInAcceptableAnswers_shouldReturnCorrect(){
        String answer = "correct";
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add("acceptableAnswer");
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, acceptableAnswers, null);
        String acceptableResponse = "acceptableAnswer";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, acceptableResponse));

    }

    @Test
    public void checkResponseWithAnythingTagInMiddle_correctAnswer_shouldReturnCorrect(){
        String answer = "answer " + QuestionResponseChecker.ANYTHING + " 1";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String correctResponse = "answer in between 1";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, correctResponse));

    }

    @Test
    public void checkResponseWithAnythingTagInMiddle_responseWithEmptyEnd_shouldReturnIncorrect(){
        String answer = "answer " + QuestionResponseChecker.ANYTHING + " 1";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String incorrectResponse = "answer in between";
        assertFalse(QuestionResponseChecker.checkResponse(questionData, incorrectResponse));

    }

    @Test
    public void checkResponseWithAnythingTagInMiddle_responseWithEmptyBeginning_shouldReturnIncorrect(){
        String answer = "answer " + QuestionResponseChecker.ANYTHING + " 1";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String incorrectResponse = "in between 1";
        assertFalse(QuestionResponseChecker.checkResponse(questionData, incorrectResponse));
    }

    @Test
    public void checkResponseWithAnythingAtEnd_correctResponse_shouldReturnCorrect(){
        String answer = "answer " + QuestionResponseChecker.ANYTHING;
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String correctResponse = "answer at end";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, correctResponse));
    }

    @Test
    public void checkResponseWithAnythingAtBeginning_correctResponse_shouldReturnCorrect(){
        String answer = QuestionResponseChecker.ANYTHING + " answer";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String correctResponse = "beginning answer";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, correctResponse));
    }


    @Test
    public void checkResponseWithAnythingAtEnd_responseWithNothingAtEnd_shouldReturnIncorrect(){
        String answer = "answer " + QuestionResponseChecker.ANYTHING;
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String incorrectResponse = "answer ";
        assertFalse(QuestionResponseChecker.checkResponse(questionData, incorrectResponse));
    }

    @Test
    public void checkResponseWithPunctuation_responseWithNoPunctuation_shouldReturnCorrect(){
        String answer = "This is the answer.";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String correctResponse = "This is the answer";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, correctResponse));
    }

    @Test
    public void checkResponseWithCapitalization_responseWithLowercase_shouldReturnCorrect(){
        String answer = "This is the answer.";
        //question type doesn't matter
        QuestionData questionData = new QuestionData("questionID1","lessonID1", "topic1", Question_FillInBlank_MultipleChoice.QUESTION_TYPE,
                "question1", null, answer, null, null);
        String correctResponse = "this is the answer.";
        assertTrue(QuestionResponseChecker.checkResponse(questionData, correctResponse));
    }
}
