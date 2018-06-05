package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;

public class Questions_emergency_blood extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlank());
        questions.add(multipleChoice(data));
        questions.add(spelling());
        questions.add(multipleChoice2(data));
        questions.add(multipleChoice3());

        return questions;
    }

    private QuestionData fillInBlank(){
        String question = "Please " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " this way.";
        question += "\nこちらに来てください。";
        String answer = "come";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
    }

    private List<String> getCompatibleBloodTypes(String bloodType){
        List<String> compatibleBloodTypes = new ArrayList<>(4);
        switch (bloodType){
            case "A":
                compatibleBloodTypes.add("A");
                compatibleBloodTypes.add("O");
                break;
            case "B":
                compatibleBloodTypes.add("B");
                compatibleBloodTypes.add("O");
                break;
            case "AB":
                compatibleBloodTypes.add("AB");
                compatibleBloodTypes.add("A");
                compatibleBloodTypes.add("B");
                compatibleBloodTypes.add("O");
                break;
            case "O":
                compatibleBloodTypes.add("O");
                break;
        }
        return compatibleBloodTypes;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = data.getPropertyAt(0).getEnglish() + "'s blood type is " +
                data.getPropertyAt(1).getEnglish() + ".\nWhat blood type do we need?";

        List<String> possibleBloodTypes = getCompatibleBloodTypes(data.getPropertyAt(1).getEnglish());
        String answer = possibleBloodTypes.get(0);
        possibleBloodTypes.remove(0);

        List<String> choices = new ArrayList<>(4);
        choices.add("A");
        choices.add("B");
        choices.add("AB");
        choices.add("O");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setAcceptableAnswers(possibleBloodTypes);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData spelling(){
        String question = "ちょうどいい";
        String answer = "perfect";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.SPELLING);
        return questionData;
    }
    private QuestionData multipleChoice2(EntityPropertyData data){
        String question = "What will happen to " + data.getPropertyAt(0).getEnglish() + "?";
        String capitalizedPronoun = GrammarRules.uppercaseFirstLetterOfSentence(data.getPropertyAt(2).getEnglish());
        String answer = capitalizedPronoun + " will get a blood transfusion.";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add(capitalizedPronoun + " will die.");
        choices.add(capitalizedPronoun + " got in an accident.");

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData multipleChoice3(){
        String question = "What is your blood type?";

        List<String> possibleBloodTypes = new ArrayList<>(4);
        possibleBloodTypes.add("A");
        possibleBloodTypes.add("B");
        possibleBloodTypes.add("AB");
        possibleBloodTypes.add("O");

        List<FeedbackPair> allFeedback = new ArrayList<>(4);
        for (String bloodType : possibleBloodTypes){
            FeedbackPair pair = new FeedbackPair(bloodType, "あなたの血液型は何ですか。", FeedbackPair.EXPLICIT);
            allFeedback.add(pair);
        }

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(possibleBloodTypes);
        questionData.setAnswer(possibleBloodTypes.get(0));
        questionData.addAcceptableAnswer(possibleBloodTypes.get(1));
        questionData.addAcceptableAnswer(possibleBloodTypes.get(2));
        questionData.addAcceptableAnswer(possibleBloodTypes.get(3));
        questionData.setFeedback(allFeedback);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }
}
