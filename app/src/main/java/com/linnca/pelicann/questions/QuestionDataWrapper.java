package com.linnca.pelicann.questions;

import java.util.List;

//wrapper class for the question data and the related wikiData ID.
//we want the  wikiData ID  associated with the question so that
//we can store it as a key.
public class QuestionDataWrapper {
    private final List<List<QuestionData>> questionSet;
    private final String wikiDataID;
    private final String interestLabel;

    public QuestionDataWrapper(List<List<QuestionData>> questionSet, String wikiDataID, String interestLabel) {
        this.questionSet = questionSet;
        this.wikiDataID = wikiDataID;
        this.interestLabel = interestLabel;
    }

    public List<List<QuestionData>> getQuestionSet() {
        return questionSet;
    }

    public String getWikiDataID() {
        return wikiDataID;
    }

    public String getInterestLabel() {
        return interestLabel;
    }
}

