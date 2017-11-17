package com.linnca.pelicann.questions;

import com.linnca.pelicann.lessongenerator.FeedbackPair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionFeedbackFormatterTest {

    @Test
    public void formatFeedback_wrongResponseWithNoSpecificFeedback_feedbackShouldAtLeastContainTheCorrectAnswer(){
        QuestionData data = new QuestionData("questionID1","lessonID1", "topic1", Question_MultipleChoice.QUESTION_TYPE,
                "question1", null, "answer1", null, null);
        String response = "wrong response";
        List<String> allWrongResponses = new ArrayList<>(1);
        allWrongResponses.add(response);
        String feedback = QuestionFeedbackFormatter.formatFeedback(false, data, response, allWrongResponses);
        assertTrue(feedback.contains("answer1"));
    }

    @Test
    public void formatFeedback_correctResponseWithNoSpecificFeedback_feedbackShouldBeEmpty(){
        QuestionData data = new QuestionData("questionID1","lessonID1", "topic1", Question_MultipleChoice.QUESTION_TYPE,
                "question1", null, "answer1", null, null);
        String response = "correct response";
        List<String> allWrongResponses = new ArrayList<>(1);
        String feedback = QuestionFeedbackFormatter.formatFeedback(true, data, response, allWrongResponses);
        assertEquals("", feedback);
    }

    @Test
    public void formatFeedback_wrongResponseWithMatchingFeedback_shouldReturnSpecificFeedback(){
        String response = "wrong response";
        String feedback = "feedback";
        List<String> feedbackResponses = new ArrayList<>(1);
        feedbackResponses.add(response);
        FeedbackPair feedbackPair = new FeedbackPair(feedbackResponses, feedback, FeedbackPair.IMPLICIT);
        List<FeedbackPair> feedbackList = new ArrayList<>(1);
        feedbackList.add(feedbackPair);
        QuestionData data = new QuestionData("questionID1","lessonID1", "topic1", Question_MultipleChoice.QUESTION_TYPE,
                "question1", null, "answer1", null, feedbackList);
        List<String> allWrongResponses = new ArrayList<>(1);
        allWrongResponses.add(response);
        String feedbackReturned = QuestionFeedbackFormatter.formatFeedback(false, data, response, allWrongResponses);
        assertEquals(feedback, feedbackReturned);
    }

    @Test
    public void formatFeedback_correctResponseWithMatchingFeedback_shouldReturnSpecificFeedback(){
        String response = "correct response";
        String feedback = "feedback";
        List<String> feedbackResponses = new ArrayList<>(1);
        feedbackResponses.add(response);
        FeedbackPair feedbackPair = new FeedbackPair(feedbackResponses, feedback, FeedbackPair.IMPLICIT);
        List<FeedbackPair> feedbackList = new ArrayList<>(1);
        feedbackList.add(feedbackPair);
        QuestionData data = new QuestionData("questionID1","lessonID1", "topic1", Question_MultipleChoice.QUESTION_TYPE,
                "question1", null, "answer1", null, feedbackList);
        List<String> allWrongResponses = new ArrayList<>(1);
        String feedbackReturned = QuestionFeedbackFormatter.formatFeedback(true, data, response, allWrongResponses);
        assertEquals(feedback, feedbackReturned);
    }

    @Test
    public void formatFeedback_correctResponseWithMatchingImplicitFeedbackButNotExplicitFeedback_explicitShouldReturnEmptyFeedback(){
        String response = "correct response";
        String explicitResponse = "Correct response";
        String feedback = "feedback";
        List<String> explicitFeedbackResponses = new ArrayList<>(1);
        explicitFeedbackResponses.add(explicitResponse);
        FeedbackPair feedbackPair = new FeedbackPair(explicitFeedbackResponses, feedback, FeedbackPair.EXPLICIT);
        List<FeedbackPair> feedbackList = new ArrayList<>(1);
        feedbackList.add(feedbackPair);
        QuestionData data = new QuestionData("questionID1","lessonID1", "topic1", Question_MultipleChoice.QUESTION_TYPE,
                "question1", null, "answer1", null, feedbackList);
        List<String> allWrongResponses = new ArrayList<>(1);
        String feedbackReturned = QuestionFeedbackFormatter.formatFeedback(true, data, response, allWrongResponses);
        assertEquals("", feedbackReturned);

        //try the implicit version as well
        feedbackPair = new FeedbackPair(explicitFeedbackResponses, feedback, FeedbackPair.IMPLICIT);
        feedbackList = new ArrayList<>(1);
        feedbackList.add(feedbackPair);
        data.setFeedback(feedbackList);
        feedbackReturned = QuestionFeedbackFormatter.formatFeedback(true, data, response, allWrongResponses);
        assertEquals(feedback, feedbackReturned);
    }
}
