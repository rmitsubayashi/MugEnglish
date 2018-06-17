package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_body_height;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_body_height;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_body_height;

public class Body_height extends Lesson {
    public static final String KEY = "bodyHeight";

    public Body_height(){
        this.instanceGenerator = new Instance_body_height();
        this.scriptGenerator = new Script_body_height();
        this.questionGenerator = new Questions_body_height();
    }
}
