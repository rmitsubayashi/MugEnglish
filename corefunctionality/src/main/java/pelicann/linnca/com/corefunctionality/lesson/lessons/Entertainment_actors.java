package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_entertainment_actors;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_entertainment_actors;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_entertainment_actors;

public class Entertainment_actors extends Lesson {
    public static final String KEY = "entertainmentActors";

    public Entertainment_actors(){
        this.instanceGenerator = new Instance_entertainment_actors();
        this.scriptGenerator = new Script_entertainment_actors();
        this.questionGenerator = new Questions_entertainment_actors();
    }
}
