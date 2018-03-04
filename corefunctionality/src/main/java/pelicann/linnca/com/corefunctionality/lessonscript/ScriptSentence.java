package pelicann.linnca.com.corefunctionality.lessonscript;

import java.io.Serializable;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public class ScriptSentence implements Serializable {
    private String sentenceEN;
    private String sentenceJP;
    private String extraInfo;
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

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public ScriptSpeaker getSpeaker() {
        return speaker;
    }

    public Translation getSpeakerName(){ return speaker.getName(); }

    public String getSpeakerImageURL(){
        return speaker.getImageURL();
    }

    public void setSpeaker(ScriptSpeaker speaker) {
        this.speaker = speaker;
    }

    public void setSpeaker(Translation speaker, Translation imageURL, Translation nickname){
        if (imageURL == null){
            imageURL = new Translation(ScriptSpeaker.IMAGE_NONE, ScriptSpeaker.IMAGE_NONE);
        }
        this.speaker = new ScriptSpeaker(speaker, imageURL, nickname);
    }
}
