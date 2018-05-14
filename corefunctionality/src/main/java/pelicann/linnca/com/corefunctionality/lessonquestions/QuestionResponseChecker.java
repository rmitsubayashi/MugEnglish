package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.util.ArrayList;
import java.util.List;

public class QuestionResponseChecker {
    public static final String ANYTHING = "@anything@";

    private QuestionResponseChecker(){}
    //should check the answer
    public static boolean checkResponse(QuestionData questionData, String response){
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(questionData.getAnswer());
        if (questionData.getAcceptableAnswers() != null){
            allAnswers.addAll(questionData.getAcceptableAnswers());
        }

        //if any of the answers match,
        // the response is correct
        for (String correctAnswer : allAnswers){
            //clean the string so we can easily compare the strings
            response = formatAnswer(response);
            correctAnswer = formatAnswer(correctAnswer);
            if (compareResponse(response, correctAnswer)){
                return true;
            }
        }
        return false;
    }

    public static boolean isFreeForm(QuestionData questionData){
        if (questionData.getAnswer().contains(ANYTHING)){
            return true;
        }

        for (String acceptableAnswer : questionData.getAcceptableAnswers()){
            if (acceptableAnswer.contains(ANYTHING)){
                return true;
            }
        }
        return false;
    }

    static String formatAnswer(String answer){
        //we still accept technically wrong answers for example
        //names should always be capitalized.
        //this should be considered correct and
        //reinforced in the feedback section

        //lower case
        answer = answer.toLowerCase();
        //remove unnecessary whitespace
        answer = answer.trim();
        //trim is necessary before calling this regex
        answer = answer.replaceAll(" +", " ");
        //remove last punctuation
        // except '@' because that's used for marking tags like @anything@
        answer = answer.replaceAll("[\\p{Punct}&&[^@]]+$", "");

        return answer;
    }

    static boolean compareResponse(String response, String answer){
        //ANYTHING says that the user can put anything in there
        if (answer.contains(ANYTHING)){
            //escapes the whole string
            String escapedWholeString = "\\Q" + answer + "\\E";
            // but that will also escape the ANYTHING regex,
            //so break it up so it's
            // \Q ... \E regex \Q ... \E
            // instead of
            // \Q ... regex(escaped) ... \E
            String anythingRegex = "(.*?)";
            String escapedStringWithRegex = escapedWholeString.replaceAll(ANYTHING,
                    "\\\\E" + anythingRegex + "\\\\Q");
            //just clean it up if the string starts/ends with ANYTHING
            escapedStringWithRegex = escapedStringWithRegex.replaceAll("\\\\Q\\\\E","");
            return response.matches(escapedStringWithRegex);
        } else {
            return response.equals(answer);
        }
    }
}
