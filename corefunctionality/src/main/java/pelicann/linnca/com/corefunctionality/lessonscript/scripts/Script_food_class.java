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
}
