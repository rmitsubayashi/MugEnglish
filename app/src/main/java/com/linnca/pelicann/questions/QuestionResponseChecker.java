package com.linnca.pelicann.questions;

import java.util.ArrayList;
import java.util.List;

public class QuestionResponseChecker {
    private QuestionResponseChecker(){}
    //should check the answer
    public static boolean checkResponse(QuestionData questionData, String response){
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(questionData.getAnswer());
        if (questionData.getAcceptableAnswers() != null){
            allAnswers.addAll(questionData.getAcceptableAnswers());
        }

        int answersLength = allAnswers.size();
        for (int i=0; i<answersLength; i++){
            allAnswers.set(i, formatAnswer(allAnswers.get(i)));
        }
        response = formatAnswer(response);

        return allAnswers.contains(response);
    }

    public static String formatAnswer(String answer){
        //we still accept technically wrong answers for example
        //names should always be capitalized.
        //this should be considered correct and
        //reinforced in the feedback section

        //lower case
        answer = answer.toLowerCase();
        //remove whitespace
        answer = answer.trim();
        //remove last punctuation
        answer = answer.replaceAll("\\p{Punct}+$", "");

        return answer;
    }
}
