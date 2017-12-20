package com.linnca.pelicann.questions;

import com.linnca.pelicann.lessongenerator.FeedbackPair;

import java.util.ArrayList;
import java.util.List;

public class QuestionFeedbackFormatter {
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
                List<String> feedbackResponses = feedbackPair.getResponse();
                if (feedbackPair.getResponseCheckType() == FeedbackPair.EXPLICIT) {
                    //we don't format the answer because we might want to be catching
                    // lowercase when it should have been uppercase
                    for (String feedbackResponse : feedbackResponses){
                        if (QuestionResponseChecker.compareResponse(response, feedbackResponse)){
                            return feedbackPair.getFeedback();
                        }
                    }
                } else if (feedbackPair.getResponseCheckType() == FeedbackPair.IMPLICIT){
                    //we want to format the strings
                    response = QuestionResponseChecker.formatAnswer(response);
                    for (String feedbackResponse : feedbackResponses){
                        feedbackResponse = QuestionResponseChecker.formatAnswer(feedbackResponse);
                        if (QuestionResponseChecker.compareResponse(response, feedbackResponse)){
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
                    //save the formatted responses so we don't have to re-format them for every feedback pair
                    if (implicitAllWrongResponses == null) {
                        implicitAllWrongResponses = new ArrayList<>();
                        for (String wrongResponse : allWrongResponses) {
                            implicitAllWrongResponses.add(QuestionResponseChecker.formatAnswer(wrongResponse));
                        }
                    }

                    //format the answer and compare
                    for (String responseToCompare : responsesToCompare){
                        responseToCompare = QuestionResponseChecker.formatAnswer(responseToCompare);
                        for (String wrongResponse : implicitAllWrongResponses){
                            if (QuestionResponseChecker.compareResponse(wrongResponse, responseToCompare)){
                                return feedbackPair.getFeedback();
                            }
                        }
                    }
                } else if (feedbackPair.getResponseCheckType() == FeedbackPair.EXPLICIT) {
                    //we should not format the string
                    for (String wrongResponse : allWrongResponses){
                        for (String responseToCompare : responsesToCompare){
                            if (QuestionResponseChecker.compareResponse(wrongResponse, responseToCompare)){
                                return feedbackPair.getFeedback();
                            }
                        }
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

        String answer = questionData.getAnswer();
        //we need to remove any tags we have
        answer = answer.replaceAll(QuestionResponseChecker.ANYTHING, "~");
        if (questionType == Question_SentencePuzzle.QUESTION_TYPE){
            answer = formatSentencePuzzleAnswer(answer);
        }
        return wrongFeedbackTemplate(answer);
    }

    private static String wrongFeedbackTemplate(String answer){
        return "正解: " + answer;
    }

    public static String formatSentencePuzzleAnswer(String answer){
        return answer.replace("|"," ");
    }
}
