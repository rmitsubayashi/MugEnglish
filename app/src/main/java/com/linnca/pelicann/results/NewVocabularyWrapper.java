package com.linnca.pelicann.results;

import com.linnca.pelicann.vocabulary.VocabularyWord;

public class NewVocabularyWrapper {
    private VocabularyWord vocabularyWord;
    private boolean isNew;

    public NewVocabularyWrapper(VocabularyWord vocabularyWord, boolean isNew) {
        this.vocabularyWord = vocabularyWord;
        this.isNew = isNew;
    }

    public VocabularyWord getVocabularyWord() {
        return vocabularyWord;
    }

    public void setVocabularyWord(VocabularyWord vocabularyWord) {
        this.vocabularyWord = vocabularyWord;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
