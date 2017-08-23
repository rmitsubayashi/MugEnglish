package com.linnca.pelicann.questiongenerator;

import com.linnca.pelicann.db.datawrappers.QuestionData;

import java.util.List;

//wrapper class for the question data and the related wikiData ID.
//we want the  wikiData ID  associated with the question so that
//we can store it as a key.
public class QuestionDataWrapper {
    private List<List<QuestionData>> questionSet;
    private String wikiDataID;
    private String interestLabel;

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

