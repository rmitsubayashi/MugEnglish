package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Questions_entertainment_actors extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlankMC());
        questions.add(multipleChoice(data));
        questions.add(multipleChoice2());
        questions.add(multipleChoice3(data));
        questions.add(multipleChoice4());

        return questions;
    }
    private QuestionData fillInBlankMC(){
        String question = "Hey " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + "!";
        question += "\n見てよ！";
        String answer = "look";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add("see");
        choices.add("view");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "Where are " + ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish()
                + " and " +
                ScriptSpeaker.getGuestSpeaker(2).getName().getEnglish() + "?";
        String answer = "at the Oscars";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add("at " + data.getPropertyAt(0).getEnglish() + "'s house");
        choices.add("in a movie theater");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice2(){
        String question = "winの過去形は";
        String answer = "won";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("winned");
        choices.add("wan");
        choices.add("wined");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice3(EntityPropertyData data){
        String question = "Who won the award?";
        String answer = data.getPropertyAt(0).getEnglish();
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add(ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish());
        choices.add(ScriptSpeaker.getGuestSpeaker(2).getName().getEnglish());

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice4(){
        String question = "Have you won anything before?";
        String yes = "yes";
        String no = "no";
        List<String> choices = new ArrayList<>(3);
        choices.add(yes);
        choices.add(no);

        FeedbackPair feedbackPair = new FeedbackPair(choices,
                "あなたは何か賞をもらったことがありますか",FeedbackPair.EXPLICIT);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(yes);
        questionData.addAcceptableAnswer(no);
        questionData.setChoices(choices);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }
}
