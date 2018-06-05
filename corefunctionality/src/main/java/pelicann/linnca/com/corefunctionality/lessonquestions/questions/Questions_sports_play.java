package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.FeedbackPair;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonquestions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;

public class Questions_sports_play extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(fillInBlank());
        questions.add(spelling(data));
        questions.add(multipleChoice(data));
        questions.add(multipleChoice2(data));
        questions.add(chat(data));

        return questions;
    }

    private QuestionData fillInBlank(){
        String question = "Let's " + QuestionUniqueMarkers.FILL_IN_BLANK_INPUT_TEXT +
                " outside.";
        question += "\n外に行こう。";
        String answer = "go";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_INPUT);
        return questionData;
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

    private List<Translation> popularSports(){
        List<Translation> list = new LinkedList<>();
        list.add(new Translation("Q2736", "soccer", "サッカー"));
        list.add(new Translation("Q5369", "baseball", "野球"));
        list.add(new Translation("Q847", "tennis", "テニス"));
        list.add(new Translation("Q38108", "figure skating", "フィギュアスケート"));
        list.add(new Translation("Q3930", "table tennis", "卓球"));
        return list;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = "What will " + data.getPropertyAt(0).getEnglish() + " and 1 play?";
        String answer = data.getPropertyAt(1).getEnglish();
        String answerWikidataID = data.getPropertyAt(1).getWikidataID();
        List<Translation> otherSports = popularSports();
        Collections.shuffle(otherSports);
        List<String> choices = new ArrayList<>(4);
        for (Translation sport : otherSports){
            if (!sport.getWikidataID().equals(answerWikidataID)){
                choices.add(sport.getEnglish());
            }
            if (choices.size() == 3){
                break;
            }
        }
        choices.add(answer);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private List<Translation> popularSportsOccupations(){
        //since there are multiple professions with different names
        // (ie professional baseball player & baseball player)
        // the ID should be the sport name (no duplicate)
        List<Translation> occupations = new ArrayList<>(4);
        occupations.add(new Translation("Q2736","soccer player","サッカー選手"));
        occupations.add(new Translation("Q5369","baseball player","野球選手"));
        occupations.add(new Translation("Q31920","swimmer","競泳選手"));
        occupations.add(new Translation("Q38108","figure skater", "フィギュアスケート選手"));
        occupations.add(new Translation("Q847","tennis player", "テニス選手"));
        occupations.add(new Translation("Q3930","table tennis player", "卓球選手"));
        occupations.add(new Translation("Q1734","volleyball player","バレーボール選手"));
        return occupations;
    }

    private QuestionData multipleChoice2(EntityPropertyData data){
        String question = "What is " + data.getPropertyAt(0).getEnglish() + "'s occupation?";
        String answer = data.getPropertyAt(2).getEnglish();
        String sportWikidataID = data.getPropertyAt(1).getWikidataID();
        List<Translation> otherOccupations = popularSportsOccupations();
        Collections.shuffle(otherOccupations);
        List<String> choices = new ArrayList<>(4);
        for (Translation sport : otherOccupations){
            if (!sport.getWikidataID().equals(sportWikidataID)){
                choices.add(sport.getEnglish());
            }
            if (choices.size() == 3){
                break;
            }
        }
        choices.add(answer);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData chat(EntityPropertyData data){
        String sentence1 = "Let's go outside.";
        ChatQuestionItem item1 = new ChatQuestionItem(false, sentence1);
        String sentence2 = "Sure.";
        ChatQuestionItem item2 = new ChatQuestionItem(true, sentence2);
        String sentence3 = "What do you want to play?";
        ChatQuestionItem item3 = new ChatQuestionItem(false, sentence3);
        ChatQuestionItem item4 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(4);
        chatItems.add(item1);
        chatItems.add(item2);
        chatItems.add(item3);
        chatItems.add(item4);
        String question = QuestionSerializer.serializeChatQuestion(data.getPropertyAt(0).getJapanese(), chatItems);
        String answer = QuestionResponseChecker.ANYTHING;

        FeedbackPair feedbackPair = new FeedbackPair(FeedbackPair.ALL,
                "あなたは何を遊びたいですか",FeedbackPair.IMPLICIT);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.CHAT);
        return questionData;
    }
}
