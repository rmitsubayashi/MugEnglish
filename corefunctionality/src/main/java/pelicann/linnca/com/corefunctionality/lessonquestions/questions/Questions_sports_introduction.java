package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;

public class Questions_sports_introduction extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(spelling(data));
        questions.add(fillInBlank(data));
        questions.add(multipleChoice(data));
        questions.add(fillInBlank2());
        questions.add(instructions());

        return questions;
    }

    private QuestionData spelling(EntityPropertyData data){
        String question = data.getPropertyAt(1).getJapanese();
        String answer = data.getPropertyAt(1).getEnglish();

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.CHOOSECORRECTSPELLING);
        return questionData;
    }

    private QuestionData fillInBlank(EntityPropertyData data){
        String question = data.getPropertyAt(0).getEnglish() + "'s favorite number is " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER + ".";
        String answer = data.getPropertyAt(3).getEnglish();

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "Why is " + data.getPropertyAt(3).getEnglish() +
                " " +data.getPropertyAt(0).getEnglish() + "'s favorite number?";
        String answer = "It is " + data.getPropertyAt(0).getEnglish() + "'s uniform number.";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add(data.getPropertyAt(0).getEnglish() + " is " + data.getPropertyAt(3).getEnglish() + " years old.");
        choices.add(data.getPropertyAt(0).getEnglish() + " plays for " + data.getPropertyAt(2).getEnglish() + ".");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData fillInBlank2(){
        String question = "My favorite number is " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER + ".";
        String answer = QuestionResponseChecker.ANYTHING;

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }

    private QuestionData instructions(){
        String question = "名前を教えてください";
        String answer = "My name is " + QuestionResponseChecker.ANYTHING + ".";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        return questionData;
    }
}
