package pelicann.linnca.com.corefunctionality.lessonscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Script implements Serializable {
    private List<ScriptSentence> sentences = new ArrayList<>();

    public Script() {
    }

    public void addSentence(ScriptSentence scriptSentence){
        sentences.add(scriptSentence);
    }

    public List<ScriptSentence> getSentences(){
        return sentences;
    }

}
