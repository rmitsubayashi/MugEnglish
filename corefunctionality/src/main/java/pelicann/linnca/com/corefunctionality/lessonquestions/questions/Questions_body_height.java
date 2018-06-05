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
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Questions_body_height extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlankMC());
        questions.add(sentencePuzzle());
        questions.add(fillInBlankMC2());
        questions.add(trueFalse(data));
        questions.add(fillInBlank());

        return questions;
    }

    private QuestionData fillInBlankMC(){
        String question = "that can " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " the shelf";
        question += "\nその棚の缶";
        String answer = "on";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("in");
        choices.add("on top of");
        choices.add("under");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData sentencePuzzle() {
        String question = "助けてくれますか？";
        List<String> puzzlePieces = new ArrayList<>();
        puzzlePieces.add("Can");
        puzzlePieces.add("you");
        puzzlePieces.add("help");
        puzzlePieces.add("me");
        puzzlePieces.add("out");
        puzzlePieces.add("?");
        String answer = QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(puzzlePieces);
        questionData.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);

        return questionData;
    }

    private QuestionData fillInBlankMC2(){
        String question = "Can you " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " that can?";
        question += "\nその缶をとってくれますか。";
        String answer = "get";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("place");
        choices.add("hold");
        choices.add("take");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData trueFalse(EntityPropertyData data){
        String question = data.getPropertyAt(0).getEnglish() + " helped " +
                ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish() + " out.";
        String answer = QuestionSerializer.serializeTrueFalseAnswer(true);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        return questionData;
    }

    private QuestionData fillInBlank(){
        String question = "I am " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER +
                " cm tall.";
        String answer = QuestionResponseChecker.ANYTHING;

        FeedbackPair feedbackPair = new FeedbackPair(FeedbackPair.ALL, "あなたの身長を教えてください", FeedbackPair.IMPLICIT);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }
}
