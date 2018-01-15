package pelicann.linnca.com.corefunctionality.questions;

import java.util.List;

public class QuestionSerializer {
    private QuestionSerializer(){}

    public static String serializeSentencePuzzleAnswer(List<String> answerPieces){
        StringBuilder answer = new StringBuilder("");
        for (String piece : answerPieces){
            answer.append(piece);
            answer.append("|");
        }

        return answer.substring(0, answer.length()-1);
    }

    public static String serializeTrueFalseAnswer(boolean isTrue){
        return isTrue ? "true" : "false";
    }

    public static String serializeChatQuestion(String from, List<ChatQuestionItem> chatItems){
        StringBuilder question = new StringBuilder(from + "::");
        for (ChatQuestionItem item : chatItems){
            if (item.isUser()){
                question.append("(u)");
            } else {
                question.append("(o)");
            }
            question.append(item.getText());
        }

        return question.toString();
    }
}
