package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_emergency_phone;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_emergency_phone;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_emergency_phone;

public class Emergency_phone extends Lesson {
    public static final String KEY = "emergencyPhone";

    public Emergency_phone(){
        this.instanceGenerator = new Instance_emergency_phone();
        this.scriptGenerator = new Script_emergency_phone();
        this.questionGenerator = new Questions_emergency_phone();
    }
}
