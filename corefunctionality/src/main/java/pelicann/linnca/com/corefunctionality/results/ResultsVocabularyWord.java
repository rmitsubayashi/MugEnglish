package pelicann.linnca.com.corefunctionality.results;

import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

public class ResultsVocabularyWord {
    private VocabularyWord vocabularyWord;
    private boolean isNew;

    public ResultsVocabularyWord(VocabularyWord vocabularyWord, boolean isNew) {
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
