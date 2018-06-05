package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

public class Questions_emergency_phone extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlank(data));
        questions.add(translation());
        questions.add(translation2());
        questions.add(multipleChoice(data));
        questions.add(instructions(data));

        return questions;
    }

    private QuestionData fillInBlank(EntityPropertyData data){
        String answer = data.getPropertyAt(1).getEnglish();
        String question = StringUtils.convertPhoneNumberToPhoneNumberWords(answer) + " = " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_NUMBER;

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }

    private QuestionData translation(){
        String question = "emergency";
        String answer = "緊急";
        String acceptableAnswer = "緊急事態";
        String acceptableAnswer2 = "緊急時";
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.addAcceptableAnswer(acceptableAnswer2);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData translation2(){
        String question = "頭";
        String answer = "head";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private List<Translation> multipleChoiceWrongChoices(Translation answer) {
        List<Translation> optionList = new LinkedList<>();
        optionList.add(new Translation("France", "フランス"));
        optionList.add(new Translation("Japan", "日本"));
        optionList.add(new Translation("the United States of America", "アメリカ合衆国"));
        optionList.add(new Translation("South Korea", "大韓民国"));
        optionList.add(new Translation("China", "中華人民共和国"));
        optionList.add(new Translation("Germany", "ドイツ"));
        optionList.add(new Translation("Russia", "ロシア"));
        optionList.add(new Translation("the United Kingdom", "イギリス"));
        optionList.add(new Translation("Vietnam", "ベトナム"));

        for (Iterator<Translation> iterator = optionList.iterator(); iterator.hasNext(); ) {
            Translation option = iterator.next();
            if (option.mostLikelyEquals(answer)) {
                iterator.remove();
            }
        }
        Collections.shuffle(optionList);
        return optionList.subList(0, 3);
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "Where is " + data.getPropertyAt(0).getEnglish() + "?";
        String answer = data.getPropertyAt(2).getEnglish();

        List<Translation> choiceTranslations = multipleChoiceWrongChoices(data.getPropertyAt(2));
        List<String> choices = new ArrayList<>(4);
        List<FeedbackPair> feedbackPairs = new ArrayList<>(3);
        for (Translation choice : choiceTranslations) {
            choices.add(choice.getEnglish());
            List<String> responses = new ArrayList<>(1);
            responses.add(choice.getEnglish());
            String feedback = choice.getEnglish() + " = " + choice.getJapanese();
            FeedbackPair wrongChoiceFeedback = new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
            feedbackPairs.add(wrongChoiceFeedback);
        }
        choices.add(answer);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setFeedback(feedbackPairs);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData instructions(EntityPropertyData data){
        String question = "What happened to " + data.getPropertyAt(0).getEnglish() + "?";
        String answer = QuestionResponseChecker.ANYTHING;
        String feedback = "最初にガシャンと鳴ってたことから、事故に遭ったと推測できます。\n";
        feedback += data.getPropertyAt(0).getEnglish() + " got in an accident.\n";
        feedback += "の様な解答が正解です。";
        FeedbackPair pair = new FeedbackPair(FeedbackPair.ALL, feedback, FeedbackPair.IMPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(pair);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        return questionData;
    }
}
