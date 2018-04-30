package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;

public class Questions_food_restaurant extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(translate());
        questions.add(fillInBlankMultipleChoice());
        questions.add(fillInBlank());
        questions.add(trueFalse(data));
        questions.add(multipleChoice(data));

        return questions;
    }

    private QuestionData translate(){
        String question = "料理(名詞)";
        String answer = "dish";
        List<String> acceptableAnswers = new ArrayList<>();
        acceptableAnswers.add("recipe");
        acceptableAnswers.add("cuisine");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setAcceptableAnswers(acceptableAnswers);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData fillInBlankMultipleChoice(){
        String question = "Let's " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " here again soon.\n" +
                "また来ようね。";
        String answer = "come";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("go");
        choices.add("do");
        choices.add("take");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData fillInBlank(){
        String question = "Let's " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + " here again soon.\n" +
                "また来ようね。";
        String answer = "come";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }

    private QuestionData trueFalse(EntityPropertyData data){
        String question = "They liked the " + data.getPropertyAt(0).getEnglish() + ".";
        String answer = QuestionSerializer.serializeTrueFalseAnswer(true);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        return questionData;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "Can you make " + data.getPropertyAt(0).getEnglish() + "?";
        String answer = "yes";
        String acceptableAnswer = "no";
        List<String> choices = new ArrayList<>(2);
        choices.add(answer);
        choices.add(acceptableAnswer);

        FeedbackPair yesFeedback = new FeedbackPair(
                answer,
                data.getPropertyAt(0).getJapanese() + "作れるんだ!",
                 FeedbackPair.EXPLICIT);
        FeedbackPair noFeedback = new FeedbackPair(
                acceptableAnswer,
                data.getPropertyAt(0).getJapanese() + "作れるように頑張ろう!",
                FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.addFeedback(yesFeedback);
        questionData.addFeedback(noFeedback);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }
}
