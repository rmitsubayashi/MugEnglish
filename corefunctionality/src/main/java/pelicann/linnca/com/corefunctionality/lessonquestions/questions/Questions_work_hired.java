package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessonquestions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Questions_work_hired extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlank(data));
        questions.add(fillInBlank2(data));
        questions.add(chatMC());
        questions.add(multipleChoice());
        questions.add(instruction());
        return questions;
    }

    private QuestionData fillInBlank(EntityPropertyData data) {
        String question = data.getPropertyAt(0).getEnglish() + " is my " +
                QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT + ".";
        question += "\n" + data.getPropertyAt(0).getJapanese() + "は私のあこがれだよ。";
        String answer = "idol";
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);

        return questionData;
    }

    private QuestionData fillInBlank2(EntityPropertyData data) {
        String question = "I " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " a job at " + data.getPropertyAt(1).getEnglish() + "!";
        question += "\n" + data.getPropertyAt(1).getJapanese() + "に就職したよ！";
        String answer = "got";
        String feedbackResponse = "get";
        String feedback = "getは現在形。過去形のgotにしましょう！";
        FeedbackPair pair = new FeedbackPair(feedbackResponse, feedback, FeedbackPair.IMPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(pair);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);

        return questionData;
    }

    private QuestionData chatMC() {
        String sentence1 = "Guess what?";
        ChatQuestionItem item1 = new ChatQuestionItem(false, sentence1);
        ChatQuestionItem item2 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(2);
        chatItems.add(item1);
        chatItems.add(item2);
        String question = QuestionSerializer.serializeChatQuestion("1",chatItems);
        String answer = "What?";
        String acceptableAnswer = "Chicken butt.";
        String acceptableAnswer2 = "What's up?";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add(acceptableAnswer);
        choices.add(acceptableAnswer2);
        List<String> feedbackResponses = new ArrayList<>(choices);
        String feedback = "どれも正解です！";
        FeedbackPair pair = new FeedbackPair(feedbackResponses, feedback, FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.addAcceptableAnswer(acceptableAnswer2);
        questionData.addFeedback(pair);
        questionData.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLECHOICE);

        return questionData;
    }
    private QuestionData multipleChoice() {
        String question = "Is " + ScriptSpeaker.getGuestSpeaker(2).getName().getEnglish() +
                " happy for " + ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish() + "?";
        String answer = "yes";
        String wrong = "no";
        List<String> choices = new ArrayList<>(2);
        choices.add(answer);
        choices.add(wrong);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData instruction() {
        String question = "Where do you work?";
        String answer = QuestionResponseChecker.ANYTHING;

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);

        return questionData;
    }
}
