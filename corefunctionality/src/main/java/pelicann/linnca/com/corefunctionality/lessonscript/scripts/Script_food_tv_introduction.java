package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_food_tv_introduction extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList){
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7());
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation person = data.getPropertyAt(0);
        String sentenceEN = "Hey " + person.getEnglish() + " is on TV!";
        String sentenceJP = "おい、" + person.getJapanese() + "がテレビ出てるぞ！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation country = data.getPropertyAt(1);
        Translation gender = data.getPropertyAt(3);
        String sentenceEN = "Is " + gender.getEnglish() + " from " + country.getEnglish() + "?";
        String sentenceJP = gender.getJapanese() + "は" + country.getJapanese() + "出身だったけ？";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation country = data.getPropertyAt(1);
        Translation food = data.getPropertyAt(2);
        String sentenceEN = "That's right. I hear " + country.getEnglish() +
                " is famous for its " + food.getEnglish() + ".";
        String sentenceJP = "そうだよ。" + country.getJapanese() + "は" +
                food.getJapanese() + "が有名らしいね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Oh really.";
        String sentenceJP = "そうなんだ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation food = data.getPropertyAt(2);
        String sentenceEN = "Let's go eat " + food.getEnglish() + " sometime.";
        String sentenceJP = "今度" + food.getJapanese() + "を食べに行こう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Let's go now!";
        String sentenceJP = "今行こうよ！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Ok.";
        String sentenceJP = "いいよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }
}
