package com.example.ryomi.myenglish.questiongenerator.themes;

import com.example.ryomi.myenglish.db.datawrappers.QuestionData;

import java.util.List;

//wrapper class for the question data and the related wikiData ID.
//we don't need to save the wikiData ID into the question data,
//but we want the  wikiData ID  associated with the question so that
//we can store it as a key in question topics.
// (IDs can never be duplicate, but topic names can)
public class QuestionDataWrapper {
    private List<QuestionData> questionSet;
    private  String wikiDataID;

    public QuestionDataWrapper(List<QuestionData> questionSet, String wikiDataID) {
        this.questionSet = questionSet;
        this.wikiDataID = wikiDataID;
    }

    public List<QuestionData> getQuestionSet() {
        return questionSet;
    }

    public String getWikiDataID() {
        return wikiDataID;
    }
}

