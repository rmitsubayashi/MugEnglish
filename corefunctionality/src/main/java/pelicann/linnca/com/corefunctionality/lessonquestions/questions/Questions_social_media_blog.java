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

public class Questions_social_media_blog extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlank());
        questions.add(multipleChoice());
        questions.add(spelling());
        questions.add(multipleChoice2(data));
        questions.add(multipleChoice3());

        return questions;
    }

    private QuestionData fillInBlank(){
        String question = QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " a blog";
        question += "\nブログを始める";
        String answer = "start";
        String acceptableAnswer ="open";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }

    private QuestionData multipleChoice(){
        String question = "What will  "  +
                ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish() +
                " write about?";
        String answer = "仕事について";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add("趣味について");
        choices.add("何も");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData spelling(){
        String question = "見てみる";
        String answer = "take a look";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.SPELLING);
        return questionData;
    }

    private QuestionData multipleChoice2(EntityPropertyData data){
        String question = "What blog service does " + data.getPropertyAt(0).getEnglish() + " use?";
        String answer = "アメブロ";
        List<String> choices = new ArrayList<>(4);
        choices.add(answer);
        choices.add("ライブドア");
        choices.add("FC2");
        choices.add("seesaa");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice3(){
        String question = "Do you have a blog?";
        String yes = "yes";
        String no = "no";
        List<String> choices = new ArrayList<>(2);
        choices.add(yes);
        choices.add(no);

        FeedbackPair feedback = new FeedbackPair(choices, "あなたはブログを持っていますか", FeedbackPair.EXPLICIT);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(yes);
        questionData.addAcceptableAnswer(no);
        questionData.setChoices(choices);
        questionData.addFeedback(feedback);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }
}
