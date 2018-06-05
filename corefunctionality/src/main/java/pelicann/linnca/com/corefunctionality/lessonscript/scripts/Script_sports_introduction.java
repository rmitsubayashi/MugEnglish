package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_sports_introduction extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);
        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8(data));

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Tell me about yourself.";
        String sentenceJP = "君について教えて。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "My name is " + data.getPropertyAt(0).getEnglish() + ".";
        String sentenceJP = "私の名前は" + data.getPropertyAt(0).getJapanese() + "です。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I play for " + data.getPropertyAt(2).getEnglish() + ".";
        String sentenceJP = data.getPropertyAt(2).getJapanese() + "に所属しています。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What position do you play?";
        String sentenceJP = "ポジションはどこですか。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I play " + data.getPropertyAt(1).getEnglish() + ".";
        String sentenceJP = data.getPropertyAt(1).getJapanese() + "をやっています。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What's your favorite number?";
        String sentenceJP = "君の一番好きな数字は何ですか。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(3).getEnglish() + ".";
        String sentenceJP = data.getPropertyAt(3).getEnglish() + "です。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence8(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Because it's my uniform number.";
        String sentenceJP = "ユニフォームの番号だからです。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }
}
