package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;

public class QuestionFeedbackFormatter {
    public static String formatFeedback(boolean correct, QuestionData questionData, String response, List<String> previousResponses){
        //first check if we give feedback regardless.
        // (used when the user inputs a free-form answer)
        List<FeedbackPair> feedback = questionData.getFeedback();
        for (FeedbackPair pair : feedback){
            if (pair.getResponse().contains(FeedbackPair.ALL)){
                return pair.getFeedback();
            }
        }

        if (correct)
            return formatCorrectFeedback(questionData, response);
        else {
            return formatWrongFeedback(questionData, previousResponses);
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

    private static String formatWrongFeedback(QuestionData questionData, List<String> allWrongResponses){
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
        if (questionType == QuestionTypeMappings.TRUEFALSE){
            return "";
        }

        String answer = questionData.getAnswer();
        //we need to remove any tags we have
        answer = answer.replaceAll(QuestionResponseChecker.ANYTHING, "~");
        if (questionType == QuestionTypeMappings.SENTENCEPUZZLE){
            answer = formatSentencePuzzleAnswer(answer);
        }
        return wrongFeedbackTemplate(answer);
    }

    private static String wrongFeedbackTemplate(String answer){
        return "正解: " + answer;
    }

    private static String formatSentencePuzzleAnswer(String answer){
        return answer.replace("|"," ");
    }
}
