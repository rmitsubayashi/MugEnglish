package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_entertainment_movie extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4(data));
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6(data));
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());
        script.addSentence(scriptSentence9());
        script.addSentence(scriptSentence10(data));

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Let's go watch a movie.";
        String sentenceJP = "映画を見に行こう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Yeah, let's go.";
        String sentenceJP = "いいね、行こう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What movie do you want to watch?";
        String sentenceJP = "どの映画を観たい。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence4(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "How about " + data.getPropertyAt(1).getEnglish() + "?";
        String sentenceJP = data.getPropertyAt(1).getJapanese() + "はどうかな。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "No way!";
        String sentenceJP = "絶対嫌だ！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence6(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I'm in it.";
        String sentenceJP = "自分が出てるから。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "It's embarrassing.";
        String sentenceJP = "恥ずかしいよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Ok ok.";
        String sentenceJP = "はいはい。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence9(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "How about this one?";
        String sentenceJP = "これはどう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence10(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Sounds good, let's go.";
        String sentenceJP = "いいね、行こう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(new ScriptSpeaker(data.getPropertyAt(0)));
        return sentence;
    }
}
