package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_emergency_phone extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4(data));
        script.addSentence(scriptSentence5());
        script.addSentence(scriptSentence6(data));
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());
        script.addSentence(scriptSentence9());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Crash!";
        String sentenceJP = "ガシャン！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getNoSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Ahh!";
        String sentenceJP = "あー！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "My head is bleeding!";
        String sentenceJP = "頭から流血してる！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence4(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Someone call an ambulance!";
        String sentenceJP = "誰か救急車を呼んで！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence5(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "OK!";
        String sentenceJP = "了解！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence6(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String country = GrammarRules.definiteArticleBeforeCountry(data.getPropertyAt(2).getEnglish());
        String sentenceEN = "Umm, what was the emergency phone number of " +
                country + "?";
        String sentenceJP = "えーと、" + data.getPropertyAt(2).getJapanese() +
                "の緊急通報用電話番号は何だっけ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(1).getEnglish() + "!";
        String sentenceJP = data.getPropertyAt(1).getEnglish() + "！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Oh right." ;
        String sentenceJP = "そうだったな。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence9(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I will call right away." ;
        String sentenceJP = "いま掛けるよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }
}
