package pelicann.linnca.com.corefunctionality.vocabulary;

import java.io.Serializable;

public class VocabularyWord implements Serializable {
    //a word may have multiple instances of this class,
    //since a word may have multiple meanings
    private String id;
    private String word;
    private String meaning;
    private String exampleSentence;
    private String exampleSentenceTranslation;
    private String lessonID;

    public VocabularyWord() {
    }

    public VocabularyWord(String id, String word, String meaning,
                          String exampleSentence, String exampleSentenceTranslation,
                          String lessonID) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
        this.exampleSentenceTranslation = exampleSentenceTranslation;
        this.lessonID = lessonID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getExampleSentenceTranslation() {
        return exampleSentenceTranslation;
    }

    public void setExampleSentenceTranslation(String exampleSentenceTranslation) {
        this.exampleSentenceTranslation = exampleSentenceTranslation;
    }

    public String getLessonID() {
        return lessonID;
    }

    public void setLessonID(String lessonID) {
        this.lessonID = lessonID;
    }
}
