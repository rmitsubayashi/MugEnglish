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

public class Questions_food_tv_introduction extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(multipleChoice(data));
        questions.add(multipleChoice2(data));
        questions.add(fillInBlankMultipleChoice(data));
        questions.add(fillInBlankMultipleChoice2(data));
        questions.add(instructions());

        return questions;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "When will they eat " + data.getPropertyAt(2).getEnglish() + "?";
        List<String> choices = new ArrayList<>(3);
        choices.add("now");
        choices.add("later");
        choices.add("never");
        String answer = "now";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice2(EntityPropertyData data){
        String question = "What were they doing?";
        List<String> choices = new ArrayList<>(3);
        choices.add("eating " + data.getPropertyAt(2).getEnglish());
        choices.add("watching TV");
        choices.add("talking to " + data.getPropertyAt(0).getEnglish());
        String answer = "watching TV";

        List<String> feedbackResponses = new ArrayList<>(1);
        feedbackResponses.add("talking to " + data.getPropertyAt(0).getEnglish());
        String feedback = "\"talking to " + data.getPropertyAt(0).getEnglish() + "\"は"+
                "「" + data.getPropertyAt(0).getJapanese() + "に話している」という意味。\n" +
                "「" + data.getPropertyAt(0).getJapanese() + "について話している」は" +
                "\"talking about " + data.getPropertyAt(0).getEnglish() + "\"";
        FeedbackPair toMeaningFeedback = new FeedbackPair(feedbackResponses, feedback, FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.addFeedback(toMeaningFeedback);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    //we are not taking into account people with multiple nationalities,
    // but this is just based on the text, so it's OK??
    private List<Translation> fillInBlankMultipleChoiceOptions(Translation translation){
        List<Translation> optionList = new LinkedList<>();
        optionList.add(new Translation("Q142","France","フランス"));
        optionList.add(new Translation("Q17","Japan", "日本"));
        optionList.add(new Translation("Q30","the United States of America","アメリカ合衆国"));
        optionList.add(new Translation("Q884","South Korea","大韓民国"));
        optionList.add(new Translation("Q148","China","中華人民共和国"));
        optionList.add(new Translation("Q183","Germany","ドイツ"));
        optionList.add(new Translation("Q159","Russia","ロシア"));
        optionList.add(new Translation("Q145", "the United Kingdom", "イギリス"));
        optionList.add(new Translation("Q145","Vietnam","ベトナム"));
        //remove if it is in the list so we don't choose it at first.
        //insert later
        String countryWikidataID = translation.getWikidataID();
        for (Iterator<Translation> iterator = optionList.iterator(); iterator.hasNext();){
            Translation option = iterator.next();
            if (countryWikidataID.equals(option.getWikidataID())){
                iterator.remove();
            }
        }
        Collections.shuffle(optionList);
        List<Translation> result = new ArrayList<>(optionList.subList(0,3));
        result.add(translation);
        return result;
    }

    private QuestionData fillInBlankMultipleChoice(EntityPropertyData data){
        String question = data.getPropertyAt(0).getEnglish() + " is from " +
                QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + ".\n" +
                data.getPropertyAt(0).getJapanese() + "は" +
                data.getPropertyAt(1).getJapanese() + "出身です。";

        String answer = data.getPropertyAt(1).getEnglish();

        List<Translation> choiceTranslations = fillInBlankMultipleChoiceOptions(data.getPropertyAt(1));
        List<String> choices = new ArrayList<>(4);
        List<String> feedbackResponses = new ArrayList<>(4);
        StringBuilder feedbackSB = new StringBuilder();
        for (Translation country : choiceTranslations){
            choices.add(country.getEnglish());
            feedbackResponses.add(country.getEnglish());
            String feedbackString = country.getEnglish() + " : " + country.getJapanese() + "\n";
            feedbackSB.append(feedbackString);
        }
        FeedbackPair feedback = new FeedbackPair(feedbackResponses, feedbackSB.toString(), FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedback);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData fillInBlankMultipleChoice2(EntityPropertyData data){
        String question = "Let's " + QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " eat " + data.getPropertyAt(2).getEnglish() + " now!\n" +
                "今" + data.getPropertyAt(2).getJapanese() + "を食べに行こうよ!";
        List<String> choices = new ArrayList<>(4);
        choices.add("go");
        choices.add("come");
        choices.add("do");
        choices.add("send");
        String answer = "go";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData instructions(){
        String question = "What do you want to eat?";
        String answer = QuestionResponseChecker.ANYTHING;
        List<String> feedbackResponses = new ArrayList<>(1);
        feedbackResponses.add(FeedbackPair.ALL);
        String feedback = "あなたは何を食べたいですか？";
        FeedbackPair feedbackPair = new FeedbackPair(feedbackResponses, feedback, FeedbackPair.EXPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.addFeedback(feedbackPair);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        return questionData;
    }


}
