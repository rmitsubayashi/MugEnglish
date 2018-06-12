package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_work_about;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_work_about;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_work_about;

public class Work_about extends Lesson {
    public static final String KEY = "workAbout";

    public Work_about(){
        this.lessonInstanceGenerator = new Instance_work_about();
        this.scriptGenerator = new Script_work_about();
        this.questionGenerator = new Questions_work_about();
    }
}
