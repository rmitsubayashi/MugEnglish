package com.linnca.pelicann.questions;

import com.linnca.pelicann.lessongenerator.FeedbackPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class QuestionFeedbackFormatter {
    static String formatFeedback(boolean correct, QuestionData questionData, String response, List<String> previousResponses){
        if (correct)
            return formatCorrectFeedback(questionData, response);
        else {
            return formatWrongFeedback(questionData, response, previousResponses);
        }
    }

    private static String formatCorrectFeedback(QuestionData questionData, String response){
        //even  if the answer is correct, we might want to give feedback.
        //for example, we accept a country as an answer that is not capitalized,
        // but we want to tell the user that the country name should be
        // capitalized.
        if (questionData.getFeedback() != null){
            List<FeedbackPair> feedbackPairs = questionData.getFeedback();
            for (FeedbackPair feedbackPair : feedbackPairs) {
                List<String> responses = feedbackPair.getResponse();
                if (feedbackPair.getResponseCheckType() == FeedbackPair.EXPLICIT) {
                    if (responses.contains(response)) {
                        return feedbackPair.getFeedback();
                    }
                } else if (feedbackPair.getResponseCheckType() == FeedbackPair.IMPLICIT){
                    String formattedResponse = QuestionResponseChecker.formatAnswer(response);
                    for (String r : responses){
                        String fr = QuestionResponseChecker.formatAnswer(r);
                        if (fr.equals(formattedResponse)){
                            return feedbackPair.getFeedback();
                        }
                    }
                }
            }
        }

        return "";
    }

    private static String formatWrongFeedback(QuestionData questionData, String response, List<String> allWrongResponses){
        if (questionData.getFeedback() != null) {
            List<FeedbackPair> feedbackPairs = questionData.getFeedback();
            List<String> implicitAllWrongResponses = null;
            for (FeedbackPair feedbackPair : feedbackPairs) {
                List<String> responsesToCompare = feedbackPair.getResponse();
                if (feedbackPair.getResponseCheckType() == FeedbackPair.IMPLICIT) {
                    //format the answer and compare
                    if (implicitAllWrongResponses == null) {
                        implicitAllWrongResponses = new ArrayList<>();
                        for (String wrongResponse : allWrongResponses) {
                            implicitAllWrongResponses.add(QuestionResponseChecker.formatAnswer(wrongResponse));
                        }
                    }

                    for (String responseToCompare : responsesToCompare){
                        responseToCompare = QuestionResponseChecker.formatAnswer(responseToCompare);
                        if (implicitAllWrongResponses.contains(responseToCompare)){
                            return feedbackPair.getFeedback();
                        }
                    }
                } else if (feedbackPair.getResponseCheckType() == FeedbackPair.EXPLICIT) {
                    //we should check directly to avoid formatAnswer() hiding the feedback
                    //we want to match
                    if (!Collections.disjoint(responsesToCompare, allWrongResponses)) {
                        return feedbackPair.getFeedback();
                    }
                }
            }
        }
        int questionType = questionData.getQuestionType();
        //no feedback for true/false except if there is specific feedback
        // ('the answer was true, not false!' doesn't give any information)
        if (questionType == Question_TrueFalse.QUESTION_TYPE){
            return "";
        }
        //default should be just 'Answer: answer'
        String answer = questionData.getAnswer();
        if (questionType == Question_SentencePuzzle.QUESTION_TYPE){
            answer = answer.replace("|", " ");
        }
        return wrongFeedbackTemplate(answer);
    }

    private static String wrongFeedbackTemplate(String answer){
        return "正解: " + answer;
    }
}
