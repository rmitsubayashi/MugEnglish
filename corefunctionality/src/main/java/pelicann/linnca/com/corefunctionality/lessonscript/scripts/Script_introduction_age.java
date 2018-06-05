package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;
import pelicann.linnca.com.corefunctionality.lessonscript.StringUtils;

public class Script_introduction_age extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList){
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1(data));
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3());
        script.addSentence(scriptSentence4(data));
        script.addSentence(scriptSentence5());
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());
        script.addSentence(scriptSentence9(data));

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        Translation firstName = data.getPropertyAt(2);
        String sentenceEN = "Hi " + firstName.getEnglish() + "!";
        String sentenceJP = "こんにちは、" + firstName.getJapanese() + "！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        Translation person = data.getPropertyAt(0);
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Hey " + ScriptSpeaker.getGuestSpeaker(1).getName().getEnglish() + ".";
        sentenceEN = StringUtils.addPeriod(sentenceEN);
        String sentenceJP = "よっ、" + ScriptSpeaker.getGuestSpeaker(1).getName().getJapanese() + "。";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation nickname = data.getPropertyAt(2);
        sentence.setSpeaker(person, nickname);
        return sentence;
    }

    private ScriptSentence scriptSentence3(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "How have you been?";
        String sentenceJP = "元気だった？";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence4(EntityPropertyData data){
        Translation person = data.getPropertyAt(0);
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Great. What about you?";
        String sentenceJP = "元気だったよ。君は？";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation nickname = data.getPropertyAt(2);
        sentence.setSpeaker(person, nickname);
        return sentence;
    }

    private ScriptSentence scriptSentence5(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Meh.";
        String sentenceJP = "まあまあかな。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "You haven't changed a bit.";
        String sentenceJP = "君は全然変わってないね。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        Translation person = data.getPropertyAt(0);
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Haha";
        String sentenceJP = "（笑）";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation nickname = data.getPropertyAt(2);
        sentence.setSpeaker(person, nickname);
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "How old are you now?";
        String sentenceJP = "いま何歳？";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence9(EntityPropertyData data){
        Translation person = data.getPropertyAt(0);
        Translation birthday = data.getPropertyAt(1);
        int age = StringUtils.getAge(birthday.getEnglish());
        String year = age == 1 ? "year" : "years";
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I'm " + age + " " + year + " old.";
        String sentenceJP = age + "歳。";
        sentence.setSentence(sentenceEN, sentenceJP);
        Translation nickname = data.getPropertyAt(2);
        sentence.setSpeaker(person, nickname);
        return sentence;
    }
}
