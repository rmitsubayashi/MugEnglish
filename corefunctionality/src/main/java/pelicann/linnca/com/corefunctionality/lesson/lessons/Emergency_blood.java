package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_emergency_blood;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_emergency_blood;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_emergency_blood;

public class Emergency_blood extends Lesson {
    public static final String KEY = "emergencyBlood";

    public Emergency_blood(){
        this.lessonInstanceGenerator = new Instance_emergency_blood();
        this.scriptGenerator = new Script_emergency_blood();
        this.questionGenerator = new Questions_emergency_blood();
    }
}
