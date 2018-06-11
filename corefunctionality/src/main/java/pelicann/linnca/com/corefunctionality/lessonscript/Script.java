package pelicann.linnca.com.corefunctionality.lessonscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Script implements Serializable {
    private final List<ScriptSentence> sentences = new ArrayList<>();
    private String imageURL;

    public Script() {
    }

    public void addSentence(ScriptSentence scriptSentence){
        sentences.add(scriptSentence);
    }

    public List<ScriptSentence> getSentences(){
        return sentences;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
