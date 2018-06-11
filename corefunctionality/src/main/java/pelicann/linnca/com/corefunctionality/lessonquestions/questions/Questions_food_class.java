package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;

public class Questions_food_class extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        questions.add(multipleChoice());
        questions.add(multipleChoice2());
        questions.add(sentencePuzzle());
        questions.add(translation());
        questions.add(instructions());

        return questions;
    }

    private QuestionData multipleChoice(){
        String question = "make";
        String answer = "作る";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("持つ");
        choices.add("置く");
        choices.add("行く");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice2(){
        String question = "「see」の過去形は?";
        String answer = "saw";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("seed");
        choices.add("sew");
        choices.add("see");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData sentencePuzzle(){
        List<String> puzzlePieces = new ArrayList<>();
        puzzlePieces.add("I");
        puzzlePieces.add("know");
        puzzlePieces.add("that");
        puzzlePieces.add("dish");
        String question = "その料理知ってます。";
        String answer = QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(puzzlePieces);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        return questionData;
    }

    private QuestionData translation(){
        String question = "Me too";
        String answer = "私も";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer("僕も");
        questionData.addAcceptableAnswer("俺も");
        questionData.addAcceptableAnswer("己も");
        questionData.addAcceptableAnswer("自分も");
        questionData.addAcceptableAnswer("わたしも");
        questionData.addAcceptableAnswer("ぼくも");
        questionData.addAcceptableAnswer("おれも");
        questionData.addAcceptableAnswer("おのれも");
        questionData.addAcceptableAnswer("じぶんも");
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData instructions(){
        String question = "Do you cook?";
        String answer = QuestionResponseChecker.ANYTHING;
        List<String> feedbackResponses = new ArrayList<>(1);
        feedbackResponses.add(FeedbackPair.ALL);
        String feedback = "あなたは料理しますか？";
        FeedbackPair feedbackPair = new FeedbackPair(feedbackResponses, feedback, FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        return questionData;
    }
}
