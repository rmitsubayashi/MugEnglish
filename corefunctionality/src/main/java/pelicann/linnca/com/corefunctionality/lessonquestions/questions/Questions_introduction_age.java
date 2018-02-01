package pelicann.linnca.com.corefunctionality.lessonquestions.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessonquestions.ChatQuestionItem;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionResponseChecker;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionSerializer;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionTypeMappings;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionUniqueMarkers;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

public class Questions_introduction_age extends QuestionGenerator {
    @Override
    public List<QuestionData> makeQuestions(List<EntityPropertyData> data) {
        List<QuestionData> questions = new ArrayList<>(5);
        EntityPropertyData personData = data.get(0);
        questions.add(chat(personData));
        questions.add(translate());
        questions.add(sentencePuzzle());
        questions.add(fillInBlankMC(personData));
        questions.add(instruction());
        return questions;
    }

    private QuestionData chat(EntityPropertyData data){
        String sentence1 = "Hi " + data.getPropertyAt(0).getEnglish() + "!";
        ChatQuestionItem item1 = new ChatQuestionItem(true, sentence1);
        String sentence2 = "Hey, how have you been?";
        ChatQuestionItem item2 = new ChatQuestionItem(false, sentence2);
        ChatQuestionItem item3 = new ChatQuestionItem(true, ChatQuestionItem.USER_INPUT);
        List<ChatQuestionItem> chatItems = new ArrayList<>(3);
        chatItems.add(item1);
        chatItems.add(item2);
        chatItems.add(item3);
        String question = QuestionSerializer.serializeChatQuestion(data.getPropertyAt(0).getJapanese(), chatItems);
        List<String> choices = new ArrayList<>(3);
        choices.add("Great");
        choices.add("What about you?");
        choices.add("Meh");
        String answer = "Great";
        String acceptableAnswer = "Meh";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setChoices(choices);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer);
        questionData.setQuestionType(QuestionTypeMappings.CHAT_MULTIPLECHOICE);
        return questionData;
    }

    private QuestionData translate() {
        String question = "（笑）";
        String answer = "haha";
        List<String> acceptableAnswers = new ArrayList<>();
        acceptableAnswers.add("lol");
        acceptableAnswers.add("lmao");
        acceptableAnswers.add("rofl");
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setAcceptableAnswers(acceptableAnswers);
        questionData.setQuestionType(QuestionTypeMappings.TRANSLATEWORD);

        return questionData;
    }

    private QuestionData sentencePuzzle() {
        String question = "元気だった？";
        List<String> puzzlePieces = new ArrayList<>();
        puzzlePieces.add("How");
        puzzlePieces.add("have");
        puzzlePieces.add("you");
        puzzlePieces.add("been");
        puzzlePieces.add("?");
        String answer = QuestionSerializer.serializeSentencePuzzleAnswer(puzzlePieces);

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(puzzlePieces);
        questionData.setQuestionType(QuestionTypeMappings.SENTENCEPUZZLE);

        return questionData;
    }

    private QuestionData fillInBlankMC(EntityPropertyData data) {
        String question = data.getPropertyAt(0).getEnglish() + " is " +
                QuestionUniqueMarkers.FILL_IN_BLANK_MULTIPLE_CHOICE + " years old.";
        int age = StringUtils.getAge(data.getPropertyAt(1).getEnglish());
        String answer = Integer.toString(age);
        //one higher, one lower
        List<String> choices = new ArrayList<>(3);
        choices.add(answer);
        Random random = new Random();
        // 2 ~ age-1
        int lower = random.nextInt(age-2) + 2;
        // age+1 ~ 100
        int higher = random.nextInt(100-age-1) + age+1;
        choices.add(Integer.toString(lower));
        choices.add(Integer.toString(higher));
        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.setChoices(choices);
        questionData.setQuestionType(QuestionTypeMappings.FILLINBLANK_MULTIPLECHOICE);

        return questionData;
    }

    private QuestionData instruction() {
        String question = "How old are you?";
        String answer = "I'm " + QuestionResponseChecker.ANYTHING + " years old.";
        String acceptableAnswer1 = "I am " + QuestionResponseChecker.ANYTHING + " years old.";
        String acceptableAnswer2 = "I'm " + QuestionResponseChecker.ANYTHING + ".";
        String acceptableAnswer3 = "I am " + QuestionResponseChecker.ANYTHING + ".";

        QuestionData questionData = new QuestionData();
        questionData.setQuestion(question);
        questionData.setAnswer(answer);
        questionData.addAcceptableAnswer(acceptableAnswer1);
        questionData.addAcceptableAnswer(acceptableAnswer2);
        questionData.addAcceptableAnswer(acceptableAnswer3);
        questionData.setQuestionType(QuestionTypeMappings.INSTRUCTIONS);

        return questionData;
    }
}
