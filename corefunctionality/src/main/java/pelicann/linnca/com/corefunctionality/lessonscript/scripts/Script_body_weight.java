package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_body_weight extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3());
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7());
        script.addSentence(scriptSentence8(data));
        script.addSentence(scriptSentence9());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Give me a piggyback ride.";
        String sentenceJP = "おんぶして。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "My legs are cramping.";
        String sentenceJP = "足が攣った。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence3(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Fine.";
        String sentenceJP = "しょうがない。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Hop on.";
        String sentenceJP = "乗って。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Thanks, appreciate it.";
        String sentenceJP = "ありがとう、感謝してるよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Wow, you're heavy.";
        String sentenceJP = "おー、重いね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "How much do you weigh?";
        String sentenceJP = "体重どれくらいなの。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence8(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(2).getEnglish() + " " + data.getPropertyAt(1).getEnglish() + "s.";
        String sentenceJP = data.getPropertyAt(2).getJapanese() + data.getPropertyAt(1).getJapanese() + "です。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence9(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "No wonder!";
        String sentenceJP = "どうりで！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }
}
