package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class VerbQuestionManager {
    private List<QuestionData> questions;
    private int currentQuestionIndex;
    private final int QUESTION_CT = 10;
    private VerbQuestionManagerListener listener;
    private int correctCt;
    //referenced to find the wrong words
    private List<WordDefinitionPair> answerWords;
    private List<WordDefinitionPair> wrongWords;

    public interface VerbQuestionManagerListener {
        //not questionIndex but questionNumber (that we show the user)
        void onNextQuestion(QuestionData data, int questionNumber, int totalQuestions, boolean isFirstQuestion);
        void onQuestionsFinished();
    }

    public VerbQuestionManager(VerbQuestionManagerListener listener){
        this.listener = listener;
        initQuestions();
    }



    public boolean isVerbQuestionsStarted(){
        return currentQuestionIndex != -1;
    }

    private void initQuestions(){
        //incremented to 0 on nextQuestion()
        currentQuestionIndex = -1;
        correctCt = 0;
        wrongWords = new ArrayList<>(QUESTION_CT);

        List<WordDefinitionPair> allWords = new ArrayList<>();
        allWords.add(new WordDefinitionPair("be","です"));
        allWords.add(new WordDefinitionPair("have","持つ"));
        allWords.add(new WordDefinitionPair("do","する"));
        allWords.add(new WordDefinitionPair("make","作る"));
        allWords.add(new WordDefinitionPair("get","得る"));
        allWords.add(new WordDefinitionPair("give","与える"));
        allWords.add(new WordDefinitionPair("put","置く"));
        allWords.add(new WordDefinitionPair("take","持っていく"));
        allWords.add(new WordDefinitionPair("keep","保つ"));
        allWords.add(new WordDefinitionPair("let","させる"));
        allWords.add(new WordDefinitionPair("go","行く"));
        allWords.add(new WordDefinitionPair("come","来る"));
        allWords.add(new WordDefinitionPair("seem","~ようだ"));
        allWords.add(new WordDefinitionPair("say","言う"));
        allWords.add(new WordDefinitionPair("see","見る"));
        allWords.add(new WordDefinitionPair("send","送る"));

        Collections.shuffle(allWords);

        questions = new ArrayList<>(QUESTION_CT);
        answerWords = new ArrayList<>(QUESTION_CT);
        for (int questionIndex=0; questionIndex<QUESTION_CT; questionIndex++){
            answerWords.add(allWords.get(questionIndex));
            //make question
            QuestionData verbQuestion = new QuestionData();
            //for first half, word -> def
            //for second half, def -> word
            List<Integer> choiceIndexes = getChoiceIndexes(allWords.size(), questionIndex);
            List<String> choices = new ArrayList<>(choiceIndexes.size());
            for (Integer choiceIndex : choiceIndexes){
                if (questionIndex < QUESTION_CT/2) {
                    choices.add(allWords.get(choiceIndex).getDefinition());
                } else {
                    choices.add(allWords.get(choiceIndex).getWord());
                }
            }
            verbQuestion.setChoices(choices);
            String word = allWords.get(questionIndex).getWord();
            String definition = allWords.get(questionIndex).getDefinition();
            if (questionIndex < QUESTION_CT/2) {
                verbQuestion.setQuestion(word);
                verbQuestion.setAnswer(definition);
            } else {
                verbQuestion.setQuestion(definition);
                verbQuestion.setAnswer(word);
            }
            verbQuestion.setQuestionType(QuestionTypeMappings.MULTIPLECHOICE);
            this.questions.add(verbQuestion);
        }
    }

    //gets ALL choices including the right answer
    private List<Integer> getChoiceIndexes(int wordCt, int questionIndex){
        //wrongChoiceCt + 1 = total choice ct (1 = correct choice)
        final int wrongChoiceCt = 3;
        Set<Integer> coveredIndexes = new HashSet<>(wrongChoiceCt);
        coveredIndexes.add(questionIndex);
        Random random = new Random();
        for (int i=0; i<wrongChoiceCt; i++){
            while (true){
                int randomIndex = random.nextInt(wordCt);
                if (!coveredIndexes.contains(randomIndex)){
                    coveredIndexes.add(randomIndex);
                    break;
                }
            }
        }

        return new ArrayList<>(coveredIndexes);
    }

    public void startQuestions(){
        initQuestions();
        nextQuestion(true);
    }

    public void nextQuestion(boolean isFirstQuestion){
        //we want the NEXT question
        currentQuestionIndex++;
        if (currentQuestionIndex >= QUESTION_CT){
            //reset to -1 (next question should be -1+1=0
            currentQuestionIndex = -1;
            listener.onQuestionsFinished();
        } else {
            int questionNumber = currentQuestionIndex + 1;
            listener.onNextQuestion(questions.get(currentQuestionIndex), questionNumber, QUESTION_CT, isFirstQuestion);
        }
    }

    public int getQuestionCt(){
        return QUESTION_CT;
    }

    public void incrementCorrectCt(){
        correctCt++;
    }

    public int getCorrectCt(){
        return correctCt;
    }

    public void addCurrentWrongWord(){
        wrongWords.add(
            answerWords.get(currentQuestionIndex)
        );
    }

    public List<WordDefinitionPair> getWrongWords(){
        return wrongWords;
    }
}
