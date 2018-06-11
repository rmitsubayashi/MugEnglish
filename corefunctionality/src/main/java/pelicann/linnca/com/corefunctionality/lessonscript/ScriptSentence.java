package pelicann.linnca.com.corefunctionality.lessonscript;

import java.io.Serializable;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public class ScriptSentence implements Serializable {
    private String sentenceEN;
    private String sentenceJP;
    private ScriptSpeaker speaker;

    public ScriptSentence() {
    }

    public String getSentenceEN() {
        return sentenceEN;
    }

    public String getSentenceJP() {
        return sentenceJP;
    }

    public void setSentence(String sentenceEN, String sentenceJP) {
        this.sentenceEN = sentenceEN;
        this.sentenceJP = sentenceJP;
    }

    public ScriptSpeaker getSpeaker() {
        return speaker;
    }

    public Translation getSpeakerName(){ return speaker.getName(); }


    public void setSpeaker(ScriptSpeaker speaker) {
        this.speaker = speaker;
    }
}
