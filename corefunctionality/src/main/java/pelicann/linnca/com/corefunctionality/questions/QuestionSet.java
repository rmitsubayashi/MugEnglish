package pelicann.linnca.com.corefunctionality.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//very similar to QuestionSetData
//except for that we only have the IDs,
//not the actual data
public class QuestionSet {
    private String key;
    private String interestID;
    private String interestLabel;
    //question 1
    //   |
    //   +---- question variation 1
    //   +---- question variation 2
    //question 2
    // ....
    private List<List<String>> questionIDs;
    private List<String> vocabularyIDs;
    //how many of this question set is used currently by users
    private int count;

    public QuestionSet() {
    }

    public QuestionSet(String key, String interestID, String interestLabel, List<List<String>> questionIDs, List<String> vocabularyIDs, int count) {
        this.key = key;
        this.interestID = interestID;
        this.interestLabel = interestLabel;
        this.questionIDs = questionIDs;
        this.vocabularyIDs = vocabularyIDs;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInterestID() {
        return interestID;
    }

    public void setInterestID(String interestID) {
        this.interestID = interestID;
    }

    public String getInterestLabel() {
        return interestLabel;
    }

    public void setInterestLabel(String interestLabel) {
        this.interestLabel = interestLabel;
    }

    public List<List<String>> getQuestionIDs() {
        return questionIDs;
    }

    public void setQuestionIDs(List<List<String>> questionIDs) {
        this.questionIDs = questionIDs;
    }

    public List<String> getVocabularyIDs() {
        return vocabularyIDs;
    }

    public void setVocabularyIDs(List<String> vocabularyIDs) {
        this.vocabularyIDs = vocabularyIDs;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    //choose random questions that will be used for an instance
    public List<String> pickQuestions(){
        Random random = new Random();
        List<String> questions = new ArrayList<>();
        for (List<String> questionVariations : questionIDs){
            int index = random.nextInt(questionVariations.size());
            String questionToAdd = questionVariations.get(index);
            questions.add(questionToAdd);
        }
        return questions;
    }
}
