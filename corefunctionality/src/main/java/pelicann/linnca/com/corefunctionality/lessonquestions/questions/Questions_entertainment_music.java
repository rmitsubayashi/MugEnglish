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
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Questions_entertainment_music extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> dataList) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData data = dataList.get(0);
        questions.add(spelling());
        questions.add(translate());
        questions.add(multipleChoice(data));
        questions.add(sentencePuzzle());
        questions.add(instructions());

        return questions;
    }

    private QuestionData spelling(){
        String question = "悪かったね";
        String answer = "my bad";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.SPELLING_SUGGESTIVE);
        return questionData;
    }

    private QuestionData translate(){
        String question = "漏れる";
        String answer = "leak";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);
        return questionData;
    }

    private QuestionData sentencePuzzle(){
        String question = "どうしてわかったの。";
        List<String> puzzlePieces = new ArrayList<>();
        puzzlePieces.add("How");
        puzzlePieces.add("did");
        puzzlePieces.add("you");
        puzzlePieces.add("know");
        puzzlePieces.add("?");
        String answer = QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces);


        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(puzzlePieces);
        questionData.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);
        return questionData;
    }

    private QuestionData multipleChoice(EntityPropertyData data){
        String question = ScriptSpeaker.getGuestSpeaker(1).getName().getJapanese() +
                "は何故" + ScriptSpeaker.getGuestSpeaker(2).getName().getJapanese() +
                "が" + data.getPropertyAt(2).getJapanese() + "を聴いていると分かったのか。";
        String answer = "音が漏れていたから";
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        choices.add(ScriptSpeaker.getGuestSpeaker(2).getName().getJapanese() + "の一番好きな曲だから");
        choices.add(data.getPropertyAt(0).getJapanese() + "の代表曲だから");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData instructions(){
        String question = "Who is your favorite artist?";
        String answer = QuestionResponseChecker.ANYTHING;

        FeedbackPair feedbackPair = new FeedbackPair(FeedbackPair.ALL, "あなたの好きな歌手は誰ですか。", FeedbackPair.IMPLICIT);
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addFeedback(feedbackPair);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);
        return questionData;
    }
}
