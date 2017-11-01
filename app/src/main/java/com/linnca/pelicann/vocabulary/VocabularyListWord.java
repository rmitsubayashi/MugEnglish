package com.linnca.pelicann.vocabulary;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class VocabularyListWord implements Comparable<VocabularyListWord>{
    //make sure 'word' matches in FireBaseDBHeaders
    private String word;
    private List<String> meanings;
    private String key;

    public VocabularyListWord() {
    }

    public VocabularyListWord(String word, List<String> meanings, String key) {
        this.word = word;
        this.meanings = new ArrayList<>(meanings);
        this.key = key;
    }

    public VocabularyListWord(VocabularyWord word, String key){
        this.word = word.getWord();
        List<String> meaningsList = new ArrayList<>(1);
        meaningsList.add(word.getMeaning());
        this.meanings = meaningsList;
        this.key = key;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<String> meanings) {
        this.meanings = meanings;
    }

    public void addMeaning(String meaning){
        this.meanings.add(meaning);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int compareTo(@NonNull VocabularyListWord word2){
        return this.word.toLowerCase().compareTo(word2.getWord().toLowerCase());
    }
}
