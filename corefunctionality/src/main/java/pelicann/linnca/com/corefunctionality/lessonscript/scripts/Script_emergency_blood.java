package pelicann.linnca.com.corefunctionality.lessonscript.scripts;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.GrammarRules;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class Script_emergency_blood extends ScriptGenerator {
    @Override
    public Script makeScript(List<EntityPropertyData> dataList) {
        EntityPropertyData data = dataList.get(0);

        Script script = new Script();
        script.addSentence(scriptSentence1());
        script.addSentence(scriptSentence2(data));
        script.addSentence(scriptSentence3());
        script.addSentence(scriptSentence4());
        script.addSentence(scriptSentence5(data));
        script.addSentence(scriptSentence6());
        script.addSentence(scriptSentence7(data));
        script.addSentence(scriptSentence8());

        script.setImageURL(data.getImageURL());
        return script;
    }

    private ScriptSentence scriptSentence1(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Please!";
        String sentenceJP = "お願いします！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence2(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Someone, " + data.getPropertyAt(0).getEnglish() +
                " needs a blood transfusion!";
        String sentenceJP = "誰か、" + data.getPropertyAt(0).getJapanese() +
                "さんが輸血を必要としています！";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence3(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "I can help.";
        String sentenceJP = "私手伝えますよ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private List<String> getCompatibleBloodTypes(String bloodType){
        List<String> compatibleBloodTypes = new ArrayList<>(4);
        switch (bloodType){
            case "A":
                compatibleBloodTypes.add("A");
                compatibleBloodTypes.add("O");
                break;
            case "B":
                compatibleBloodTypes.add("B");
                compatibleBloodTypes.add("O");
                break;
            case "AB":
                compatibleBloodTypes.add("AB");
                compatibleBloodTypes.add("A");
                compatibleBloodTypes.add("B");
                compatibleBloodTypes.add("O");
                break;
            case "O":
                compatibleBloodTypes.add("O");
                break;
        }
        return compatibleBloodTypes;
    }

    private ScriptSentence scriptSentence4(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "What blood type do you need?";
        String sentenceJP = "どの血液型が必要ですか。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence5(EntityPropertyData data){
        List<String> compatibleBloodTypes = getCompatibleBloodTypes(data.getPropertyAt(1).getEnglish());
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = data.getPropertyAt(2).getEnglish() +
                " blood type is " + data.getPropertyAt(1).getEnglish() +
                ", so we need " + GrammarRules.commasInASeries(compatibleBloodTypes, "or") + ".";
        sentenceEN = GrammarRules.uppercaseFirstLetterOfSentence(sentenceEN);
        String sentenceJP = data.getPropertyAt(2).getJapanese() +
                "の血液型は" + data.getPropertyAt(1).getEnglish() + "型ですから" +
                GrammarRules.commasInASeriesJP(compatibleBloodTypes, "または") + "型が必要です。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(1));
        return sentence;
    }

    private ScriptSentence scriptSentence6(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Perfect.";
        String sentenceJP = "ちょうどいい。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence7(EntityPropertyData data){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "My blood type is " + data.getPropertyAt(1).getEnglish() + ".";
        String sentenceJP = "私は" + data.getPropertyAt(1).getEnglish() + "型です。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }

    private ScriptSentence scriptSentence8(){
        ScriptSentence sentence = new ScriptSentence();
        String sentenceEN = "Ok, please come this way.";
        String sentenceJP = "承知しました、こちらへどうぞ。";
        sentence.setSentence(sentenceEN, sentenceJP);
        sentence.setSpeaker(ScriptSpeaker.getGuestSpeaker(2));
        return sentence;
    }
}
