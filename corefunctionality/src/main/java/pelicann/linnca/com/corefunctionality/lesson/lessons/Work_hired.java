package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_work_hired;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_work_hired;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_work_hired;

public class Work_hired extends Lesson {
    public static final String KEY = "workHired";
    public Work_hired(){
        this.instanceGenerator = new Instance_work_hired();
        this.scriptGenerator = new Script_work_hired();
        this.questionGenerator = new Questions_work_hired();
    }
}
