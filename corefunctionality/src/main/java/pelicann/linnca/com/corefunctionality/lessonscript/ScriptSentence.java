package pelicann.linnca.com.corefunctionality.lessonscript;

import java.io.Serializable;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public class ScriptSentence implements Serializable {
    private String sentenceEN;
    private String sentenceJP;
    private String extraInfo;
    private String speaker;

    public static String SPEAKER_USER = "@user";
    public static String SPEAKER_NONE = "@none";

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

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speakerEN) {
        this.speaker = speakerEN;
    }

    public void setSpeaker(Translation speaker){ this.speaker = speaker.getEnglish(); }
}
