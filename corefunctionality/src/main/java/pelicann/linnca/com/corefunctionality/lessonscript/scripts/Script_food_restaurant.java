package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_food_restaurant extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2());
        if (!data.getPropertyAt(1).getEnglish().equals(Translation.NONE)) {
            script.addSentence(scriptSentence3(data));
        }
        script.addSentence(scriptSentence4(data));
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7());
        script.addSentence(scriptSentence8());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "This is delicious!";
        String sentenceJP = "これおいしいね！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I know, right?";
        String sentenceJP = "だろ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I love the " + data.getPropertyAt(1).getEnglish() + ".";
        String sentenceJP = data.getPropertyAt(1).getJapanese() + "がいいね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence4(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What is this dish called again?";
        String sentenceJP = "これ何て言う料理だっけ？";
        //前の文は材料なかったら表示しない
        if (!data.getPropertyAt(1).getEnglish().equals(Translation.NONE)){
            sentenceEN = "Yea. " + sentenceEN;
            sentenceJP = "そうだね。" + sentenceJP;
        }
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = GrammarRules.uppercaseFirstLetterOfSentence(
                data.getPropertyAt(0).getEnglish() + "."
        );
        String sentenceJP = data.getPropertyAt(0).getJapanese() + "。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "That's right.";
        String sentenceJP = "そうだったね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Let's come here again soon.";
        String sentenceJP = "また来ようね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Definitely!";
        String sentenceJP = "絶対!";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }
}
