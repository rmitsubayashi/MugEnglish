package pelicann.linnca.com.corefunctionality.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VocabularyListWord implements Comparable<VocabularyListWord>, Serializable {
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
    public boolean equals(Object object){
        //the words should be equal if they have the same key
        if (object == null)
            return false;

        if (!(object instanceof VocabularyListWord))
            return false;

        VocabularyListWord word = ((VocabularyListWord) object);
        return word.getKey().equals(this.key);
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public int compareTo(VocabularyListWord word2){
        //we want to order in alphabetical order
        return this.word.toLowerCase().compareTo(word2.getWord().toLowerCase());
    }
}
