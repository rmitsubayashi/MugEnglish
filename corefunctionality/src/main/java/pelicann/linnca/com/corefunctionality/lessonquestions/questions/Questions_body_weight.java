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

public class Questions_body_weight extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(translation());
        questions.add(multipleChoice(data));
        questions.add(trueFalse());
        questions.add(fillInBlankMC());
        questions.add(fillInBlank());

        return questions;
    }

    private QuestionData translation(){
        String question = "piggyback ride";
        String answer = "おんぶ";
        String acceptableAnswer = "負んぶ";
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = data.getPropertyAt(0).getJapanese() + "に何が起こった？";
        String answer = "足を攣った";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add("足を折った");
        choices.add("足を擦りむいた");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData trueFalse(){
        String question = ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish() +
                " is nice.";
        String answer = QuestionSerializer.serializeTrueFalseAnswer(true);
        FeedbackPair feedbackPair = new FeedbackPair(QuestionSerializer.serializeTrueFalseAnswer(false),
                "おんぶしてあげたから優しいと思うよ！",FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        return questionData;
    }

    private QuestionData fillInBlankMC(){
        String question = QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " me a piggyback ride.";
        question += "\nおんぶして。";
        String answer = "Give";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("Take");
        choices.add("Go");
        choices.add("Do");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData fillInBlank(){
        String question = "I weigh " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER + " kilograms.";
        String answer = QuestionResponseChecker.ANYTHING;
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }
}
