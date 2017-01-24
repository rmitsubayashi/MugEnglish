package com.example.ryomi.myenglish.questiongenerator;


import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionUtils {
    public static String TRUE_FALSE_QUESTION_TRUE = "true";
    public static String TRUE_FALSE_QUESTION_FALSE = "false";
    public static String BLANK_MARKER = "@blank@";

    public static void shuffle(List<String> choices){
        Collections.shuffle(choices, new Random(System.currentTimeMillis()));
    }

    public static String formatPuzzlePieceAnswer(List<String> choices){
        String answer = "";
        for (String choice : choices){
            answer += choice + "|";
        }

        answer = answer.substring(0, answer.length()-1);

        return answer;
    }

}
