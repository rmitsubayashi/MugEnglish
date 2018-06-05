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

public class Questions_entertainment_movie extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(translation());
        questions.add(multipleChoice(data));
        questions.add(trueFalse(data));
        questions.add(fillInBlankMC());
        questions.add(instructions());

        return questions;
    }

    private QuestionData translation(){
        String question = "映画";
        String answer = "movie";
        String acceptableAnswer = "film";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "何故" + data.getPropertyAt(0).getJapanese() + "は" +
                data.getPropertyAt(1).getJapanese() + "を観たくなかったのでしょうか。";
        String answer = "自分が出ているから";
        List<String> choices = new ArrayList<>(3);
        choices.add("評判が良くないから");
        choices.add("怖そうだったから");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData trueFalse(EntityPropertyData data){
        String question = data.getPropertyAt(0).getEnglish() + " and " +
                ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish() + " will watch " +
                data.getPropertyAt(1).getEnglish() + ".";
        String answer = QuestionSerializer.serializeTrueFalseAnswer(false);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        return questionData;
    }

    private QuestionData fillInBlankMC(){
        String question = "let's " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE;
        question += "\n行こう";
        String answer = "go";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("come");
        choices.add("put");
        choices.add("give");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData instructions(){
        String question = "What movie do you want to watch?";
        String answer = QuestionResponseChecker.ANYTHING;

        FeedbackPair feedbackPair = new FeedbackPair(FeedbackPair.ALL, "あなたはどの映画を観たいですか", FeedbackPair.IMPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        return questionData;
    }
}
