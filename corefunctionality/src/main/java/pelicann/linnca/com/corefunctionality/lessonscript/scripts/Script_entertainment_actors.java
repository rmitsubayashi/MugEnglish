package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_entertainment_actors extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2());
        script.addSentence(scriptSentence3());
        script.addSentence(scriptSentence4(data));
        script.addSentence(scriptSentence5());
        script.addSentence(scriptSentence6(data));
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());
        script.addSentence(scriptSentence9(data));
        script.addSentence(scriptSentence10());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I can't believe we're at the Oscars!";
        String sentenceJP = "アカデミー賞授賞式に来ているなんて信じられない！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I know, right?";
        String sentenceJP = "でしょ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence3(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Hey look!";
        String sentenceJP = "見てよ！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence4(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "That's " + data.getPropertyAt(0).getEnglish() + "!";
        String sentenceJP = "あれは" + data.getPropertyAt(0).getJapanese() + "じゃない！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence5(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Who?";
        String sentenceJP = "誰？";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence6(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "You don't know " + data.getPropertyAt(0).getEnglish() + "?";
        String sentenceJP = data.getPropertyAt(0).getJapanese() + "を知らないの？";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(3).getEnglish() + " won " +
                GrammarRules.definiteArticleBeforeAward(data.getPropertyAt(1).getEnglish()) +
                " in " + data.getPropertyAt(2).getEnglish() + ".";
        sentenceEN = GrammarRules.uppercaseFirstLetterOfSentence(sentenceEN);
        String sentenceJP = data.getPropertyAt(3).getJapanese() + "は" + data.getPropertyAt(2).getJapanese() +
                "年に" + data.getPropertyAt(1).getJapanese() + "を受賞したんだよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Oh really!";
        String sentenceJP = "ホントか！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence9(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "We should try to get " + data.getPropertyAt(4).getEnglish() + " autograph.";
        String sentenceJP = data.getPropertyAt(4).getJapanese() + "のサインをもらいに行くべきだよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence10(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Yea let's try.";
        String sentenceJP = "やってみよう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }
}
