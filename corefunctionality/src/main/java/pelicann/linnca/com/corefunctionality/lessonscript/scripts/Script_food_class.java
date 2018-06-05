package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_food_class extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2());
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5());
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Welcome to my ";
        String sentenceJP = "私の";
        if (!data.getPropertyAt(1).getEnglish().equals(Translation.NONE)){
            sentenceEN += data.getPropertyAt(1).getEnglish() + " ";
            sentenceJP += data.getPropertyAt(1).getJapanese();
        }
        sentenceEN += "cooking class.";
        sentenceJP += "料理教室へようこそ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What are we going to make today?";
        String sentenceJP = "今日は何を作るんですか。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "We are going to make " + data.getPropertyAt(0).getEnglish() + ".";
        String sentenceJP = "今日は" + data.getPropertyAt(0).getJapanese() + "を作ります。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I know that dish!";
        String sentenceJP = "その料理知ってます！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence5(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Me too!";
        String sentenceJP = "私も！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(3));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I saw it on TV the other day.";
        String sentenceJP = "この前テレビで見たんだ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(3));
        return sentence;
    }

    private ScriptSentence scriptSentence7(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "No way, I saw it on TV too!";
        String sentenceJP = "マジで、私もテレビで見たよ！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }
}
