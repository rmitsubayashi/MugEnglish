package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

public class Script_introduction_team_from extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList){
        EntityPropertyData data = dataList.get(0);
        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3(data));
        script.addSentence(scriptSentence4(data));
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6(data));
        script.addSentence(scriptSentence7(data));
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation firstName = data.getPropertyAt(6);
        String sentenceEN = "Hey " + firstName.getEnglish() + ", nice game!";
        String sentenceJP = "やあ" + firstName.getJapanese() + "、良い試合だったよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation person = data.getPropertyAt(0);
        String sentenceEN = "Thanks.";
        String sentenceJP = "ありがとう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation imageURL = data.getPropertyAt(5);
        Translation nickname = data.getPropertyAt(6);
        sentence.setSpeaker(person, imageURL, nickname);
        return sentence;
    }

    private ScriptSentence scriptSentence3(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation team = data.getPropertyAt(1);
        String sentenceEN = "You're on " + team.getEnglish();
        sentenceEN = StringUtils.addPeriod(sentenceEN);
        String sentenceJP = "君は"+ team.getJapanese() +"の一員だね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence4(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation teamCity = data.getPropertyAt(2);
        String sentenceEN = "Are you from " + teamCity.getEnglish() + "?";
        String sentenceJP = "君は"+ teamCity.getJapanese() +"の出身かな。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation person = data.getPropertyAt(0);
        Translation city = data.getPropertyAt(3);
        String sentenceEN = "Nope, I'm from " + city.getEnglish();
        sentenceEN = StringUtils.addPeriod(sentenceEN);
        String sentenceJP = "いや、" + city.getJapanese() + "の出身だよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation imageURL = data.getPropertyAt(5);
        Translation nickname = data.getPropertyAt(6);
        sentence.setSpeaker(person, imageURL, nickname);
        return sentence;
    }

    private ScriptSentence scriptSentence6(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation country = data.getPropertyAt(4);
        String sentenceEN = "Oh. " + country.getEnglish() + ", right?";
        String sentenceJP = "そっか。" + country.getJapanese() + "だよね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getUserSpeaker());
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation person = data.getPropertyAt(0);
        String sentenceEN = "Yep!";
        String sentenceJP = "そう。";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation imageURL = data.getPropertyAt(5);
        Translation nickname = data.getPropertyAt(6);
        sentence.setSpeaker(person, imageURL, nickname);
        return sentence;
    }
}
