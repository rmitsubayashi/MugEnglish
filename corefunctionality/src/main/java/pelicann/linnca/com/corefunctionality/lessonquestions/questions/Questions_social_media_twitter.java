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
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Questions_social_media_twitter extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(translate());
        questions.add(fillInBlank());
        questions.add(trueFalse(data));
        questions.add(multipleChoice2(data));
        questions.add(multipleChoice3(data));

        return questions;
    }

    private QuestionData translate(){
        String question = "フォローする";
        String answer = "follow";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData fillInBlank(){
        String question = QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " in touch";
        question += "\n連絡を取り合う";
        String answer = "keep";
        String acceptableAnswer = "stay";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add(acceptableAnswer);
        choices.add("repeat");
        choices.add("grab");

        List<FeedbackPair> feedbackPairs = new ArrayList<>(2);
        feedbackPairs.add(new FeedbackPair(answer, "stay in touchも正解です",FeedbackPair.EXPLICIT));
        feedbackPairs.add(new FeedbackPair(acceptableAnswer, "keep in touchも正解です",FeedbackPair.EXPLICIT));

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.setChoices(choices);
        questionData.setFeedback(feedbackPairs);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData trueFalse(EntityPropertyData data){
        String question = data.getPropertyAt(0).getJapanese() + "はツイッターのアカウントを持っています";
        String answer = QuestionSerializer.serializeTrueFalseAnswer(true);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRUEFALSE);
        return questionData;
    }

    private QuestionData multipleChoice2(EntityPropertyData data){
        String question = data.getPropertyAt(0).getJapanese() + "は" +
                ScriptSpeaker.getGuestSpeaker(1).getName().getJapanese() + "をフォローしましたか。";
        String answer = "わからない";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add("はい");
        choices.add("いいえ");

        String feedback = ScriptSpeaker.getGuestSpeaker(1).getName().getJapanese() +
                "は" + data.getPropertyAt(0).getJapanese() + "をフォローしましたが、逆はわかりませんね。";
        FeedbackPair feedbackPair = new FeedbackPair(choices, feedback, FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice3(EntityPropertyData data){
        String question = "What were " + data.getPropertyAt(0).getEnglish() + " and " +
                ScriptSpeaker.getGuestSpeaker(1).getName() + " doing?";
        String answer = "talking";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add("walking");
        choices.add("giving");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }
}
