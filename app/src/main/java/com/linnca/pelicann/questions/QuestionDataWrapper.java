package com.linnca.pelicann.questions;

import com.linnca.pelicann.vocabulary.VocabularyWord;

import java.util.List;

//wrapper class for the question data and the related wikiData ID.
//we want the  wikiData ID  associated with the question so that
//we can store it as a key.
//the interest label is for when we display the lesson instance to the user.
//the vocabulary and the questions will be saved in their respective nodes,
//we will get all the keys and along with the interest label,
// we will save it in the database as a QuestionSet
public class QuestionDataWrapper {
    private final List<List<QuestionData>> questionSet;
    private final String wikiDataID;
    private final String interestLabel;
    private final List<VocabularyWord> vocabulary;

    public QuestionDataWrapper(List<List<QuestionData>> questionSet, String wikiDataID, String interestLabel, List<VocabularyWord> vocabulary) {
        this.questionSet = questionSet;
        this.wikiDataID = wikiDataID;
        this.interestLabel = interestLabel;
        this.vocabulary = vocabulary;
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

    public List<VocabularyWord> getVocabulary() {
        return vocabulary;
    }
}

