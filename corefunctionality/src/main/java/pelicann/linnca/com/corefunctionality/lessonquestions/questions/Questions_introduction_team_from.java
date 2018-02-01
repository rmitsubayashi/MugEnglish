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
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;

public class Questions_introduction_team_from extends QuestionGenerator {

    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> data) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData personData = data.get(0);
        questions.add(translate());
        questions.add(fillInBlankMC(personData));
        questions.add(trueFalse(personData));
        questions.add(fillInBlankMC2(personData));
        questions.add(instruction());
        return questions;
    }

    private QuestionData fillInBlankMC(EntityPropertyData data) {
        String question = data.getPropertyAt(0).getEnglish() + " is on " +
                QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        String answer = data.getPropertyAt(1).getEnglish();
        List<String> choices = new ArrayList<>(4);
        choices.add(data.getPropertyAt(2).getEnglish());
        choices.add(data.getPropertyAt(3).getEnglish());
        choices.add(data.getPropertyAt(4).getEnglish());
        //the choices so far are all wrong choices (which are all cities/countries)
        String feedback = "国や都市といった大きな場所の場合、onではなくinを使います。";
        FeedbackPair feedbackPair = new FeedbackPair(new ArrayList<>(choices),
                feedback, FeedbackPair.EXPLICIT);
        //add the right choice
        choices.add(answer);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData translate() {
        String question = "いい試合だったね";
        String answer = "Nice game!";
        List<String> acceptableAnswers = new ArrayList<>();
        acceptableAnswers.add("good game");
        acceptableAnswers.add("that was a good game");
        acceptableAnswers.add("that was a nice game");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setAcceptableAnswers(acceptableAnswers);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);

        return questionData;
    }

    private QuestionData trueFalse(EntityPropertyData data) {
        int hash = data.hashCode();
        boolean tf = hash % 2 == 0;
        String tfString = tf ? data.getPropertyAt(3).getEnglish() :
                data.getPropertyAt(2).getEnglish();
        String question = data.getPropertyAt(0).getEnglish() + " is from "
                + tfString + ".";
        String answer = QuestionSerializer.serializeTrueFalseAnswer(tf);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRUEFALSE);

        return questionData;
    }

    private List<Translation> fillInBlankMC2WrongChoices(Translation answer) {
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


    private QuestionData fillInBlankMC2(EntityPropertyData data) {
        String question = data.getPropertyAt(3).getEnglish() + " is in " +
                QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        String answer = data.getPropertyAt(4).getEnglish();
        List<String> choices = new ArrayList<>(4);
        List<Translation> wrongChoices = fillInBlankMC2WrongChoices(data.getPropertyAt(4));
        List<FeedbackPair> feedbackPairs = new ArrayList<>(3);
        for (Translation choice : wrongChoices) {
            choices.add(choice.getEnglish());
            List<String> responses = new ArrayList<>(1);
            responses.add(choice.getEnglish());
            String feedback = choice.getEnglish() + " = " + choice.getJapanese();
            FeedbackPair wrongChoiceFeedback = new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
            feedbackPairs.add(wrongChoiceFeedback);
        }
        //add the right choice
        choices.add(answer);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setFeedback(feedbackPairs);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData instruction() {
        String question = "Where are you from?";
        String answer = "I'm from " + QuestionResponseChecker.ANYTHING + ".";
        String acceptableAnswer = "I am from " + QuestionResponseChecker.ANYTHING + ".";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);

        return questionData;
    }

}