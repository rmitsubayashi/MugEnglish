package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;

public class Questions_work_about extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(multipleChoice());
        questions.add(fillInBlank(data));
        questions.add(spelling());
        questions.add(multipleChoice2());
        questions.add(instruction());
        return questions;
    }

    private QuestionData multipleChoice() {
        String question = "つくる";
        String answer = "make";
        List<String> choices =new ArrayList<>(3);
        choices.add(answer);
        choices.add("do");
        choices.add("take");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData fillInBlank(EntityPropertyData data){
        String question = data.getPropertyAt(0).getEnglish() + "'s company " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + " " + data.getPropertyAt(1).getEnglish() + ".";
        String answer = "makes";
        List<String> acceptableAnswers = new ArrayList<>();
        acceptableAnswers.add("make");
        acceptableAnswers.add("creates");
        acceptableAnswers.add("create");
        acceptableAnswers.add("manufactures");
        acceptableAnswers.add("manufacture");
        acceptableAnswers.add("builds");
        acceptableAnswers.add("build");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setAcceptableAnswers(acceptableAnswers);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);

        return questionData;
    }

    private QuestionData spelling(){
        String question = "想像する";
        String answer = "imagine";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.CHOOSECORRECTSPELLING);

        return questionData;
    }

    private QuestionData multipleChoice2() {
        QuestionData questionData = new QuestionData();
        String question = "How hard is your work?";
        questionData.setQuestion(question);
        String answer = "hard";
        questionData.setAnswer(answer);
        String acceptableAnswer = "very hard";
        String acceptableAnswer2 = "easy";
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.addAcceptableAnswer(acceptableAnswer2);
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add(acceptableAnswer);
        choices.add(acceptableAnswer2);
        questionData.setChoices(choices);
        FeedbackPair feedbackPair = new FeedbackPair(choices,
                "あなたの仕事はどれだけ大変ですか。", FeedbackPair.EXPLICIT);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData instruction() {
        String question = "What does your company make?";
        String answer = QuestionResponseChecker.ANYTHING;

        FeedbackPair feedbackPair = new FeedbackPair(FeedbackPair.ALL,
                "あなたの会社は何をつくっていますか。", FeedbackPair.IMPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);

        return questionData;
    }
}
