package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_social_media_twitter extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);
        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2());
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5());
        script.addSentence(scriptSentence6(data));
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "It was nice talking to you.";
        String sentenceJP = "お話できてよかった。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence2(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Me too.";
        String sentenceJP = "私も。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "We should keep in touch.";
        String sentenceJP = "これからも連絡を取り合おうよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Definitely.";
        String sentenceJP = "もちろん。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Do you have a Twitter account?";
        String sentenceJP = "ツイッター持ってる？";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence6(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Yeah, follow me.";
        String sentenceJP = "うん、フォローして。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "My username is @" + data.getPropertyAt(1).getEnglish() + ".";
        String sentenceJP = "ユーザー名は@" + data.getPropertyAt(1).getEnglish() + "。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "OK, followed!";
        String sentenceJP = "オッケー、フォローしたよ！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }
}
