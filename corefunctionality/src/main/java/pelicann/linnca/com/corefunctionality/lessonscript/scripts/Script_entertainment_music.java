package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_entertainment_music extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3());
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());
        script.addSentence(scriptSentence9());
        script.addSentence(scriptSentence10());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What are you listening to?";
        String sentenceJP = "何を聴いてるの。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(1).getEnglish() + ".";
        sentenceEN = GrammarRules.uppercaseFirstLetterOfSentence(sentenceEN);
        String sentenceJP = data.getPropertyAt(1).getJapanese() + "。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence3(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Nice.";
        String sentenceJP = "いいね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What artist?";
        String sentenceJP = "どの歌手。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(0).getEnglish() + ".";
        String sentenceJP = data.getPropertyAt(0).getJapanese() + "。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Let me guess.";
        String sentenceJP = "当ててみよう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(2).getEnglish() + "?";
        String sentenceJP = data.getPropertyAt(2).getJapanese() + "かな。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "How did you know?";
        String sentenceJP = "どうしてわかったの。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence9(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "The sound's leaking.";
        String sentenceJP = "音が漏れてるから。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence10(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Oh, my bad.";
        String sentenceJP = "あっ、悪かったね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }
}
