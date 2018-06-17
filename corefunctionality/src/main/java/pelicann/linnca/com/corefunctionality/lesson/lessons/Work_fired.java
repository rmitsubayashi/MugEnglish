package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_work_fired;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_work_fired;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_work_fired;

public class Work_fired extends Lesson{
    public static final String KEY = "workFired";

    public Work_fired(){
        this.instanceGenerator = new Instance_work_fired();
        this.scriptGenerator = new Script_work_fired();
        this.questionGenerator = new Questions_work_fired();
    }
}
