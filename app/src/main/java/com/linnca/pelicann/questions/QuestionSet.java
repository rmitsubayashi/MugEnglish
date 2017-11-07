package com.linnca.pelicann.questions;

import java.util.List;

//very similar to QuestionDataWrapper
//except for that we only have the IDs,
//not the actual data
public class QuestionSet {
    private String key;
    private String interestLabel;
    //question 1
    //   |
    //   +---- question variation 1
    //   +---- question variation 2
    //question 2
    // ....
    private List<List<String>> questionIDs;
    private List<String> vocabularyIDs;

    public QuestionSet() {
    }

    public QuestionSet(String key, String interestLabel, List<List<String>> questionIDs, List<String> vocabularyIDs) {
        this.key = key;
        this.interestLabel = interestLabel;
        this.questionIDs = questionIDs;
        this.vocabularyIDs = vocabularyIDs;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}
