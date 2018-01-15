package pelicann.linnca.com.corefunctionality.questions;

import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import java.util.List;

//wrapper class for the question data and the related wikiData ID.
//we want the  wikiData ID  associated with the question so that
//we can store it as a key.
//the interest label is for when we display the lesson instance to the user.
//the vocabulary and the questions will be saved in their respective nodes,
//we will get all the keys and along with the interest label,
// we will save it in the database as a QuestionSet
public class QuestionSetData {
    private final List<List<QuestionData>> questionSet;
    private final String interestID;
    private final String interestLabel;
    private final List<VocabularyWord> vocabulary;

    public QuestionSetData(List<List<QuestionData>> questionSet, String interestID, String interestLabel, List<VocabularyWord> vocabulary) {
        this.questionSet = questionSet;
        this.interestID = interestID;
        this.interestLabel = interestLabel;
        this.vocabulary = vocabulary;
    }

    public List<List<QuestionData>> getQuestionSet() {
        return questionSet;
    }

    public String getInterestID() {
        return interestID;
    }

    public String getInterestLabel() {
        return interestLabel;
    }

    public List<VocabularyWord> getVocabulary() {
        return vocabulary;
    }
}

