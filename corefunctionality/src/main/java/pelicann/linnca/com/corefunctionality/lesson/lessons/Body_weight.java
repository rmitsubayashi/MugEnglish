package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_body_weight;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_body_weight;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_body_weight;

public class Body_weight extends Lesson {
    public static final String KEY = "bodyWeight";

    public Body_weight(){
        this.lessonInstanceGenerator = new Instance_body_weight();
        this.scriptGenerator = new Script_body_weight();
        this.questionGenerator = new Questions_body_weight();
    }
}
