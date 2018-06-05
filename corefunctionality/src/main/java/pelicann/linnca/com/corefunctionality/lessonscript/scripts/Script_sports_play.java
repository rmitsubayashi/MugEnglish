package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_sports_play extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);
        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3());
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6(data));
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What's up " + data.getPropertyAt(3).getEnglish() + "?";
        String sentenceJP = data.getPropertyAt(3).getJapanese() + "どうした。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Let's go outside and play.";
        String sentenceJP = "外に行って遊ぼうよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence3(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Sure.";
        String sentenceJP = "いいよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What do you want to play?";
        String sentenceJP = "何を遊びたい。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(1).getEnglish() + ".";
        sentenceEN = GrammarRules.uppercaseFirstLetterOfSentence(sentenceEN);
        String sentenceJP = data.getPropertyAt(1).getJapanese() + "。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence6(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Of course, you're ";
        String indefinite = GrammarRules.indefiniteArticleBeforeNoun(data.getPropertyAt(2).getEnglish());
        sentenceEN += indefinite + ".";
        String sentenceJP = "当然だ、君は" + data.getPropertyAt(2).getJapanese() + "だからね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "So are we going?";
        String sentenceJP = "で、行くの。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Yea, let's go.";
        String sentenceJP = "ああ、行こう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

}
