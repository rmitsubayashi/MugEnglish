package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_entertainment_movie;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_entertainment_movie;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_entertainment_movie;

public class Entertainment_movie extends Lesson {
    public static final String KEY = "entertainmentMovie";

    public Entertainment_movie(){
        this.lessonInstanceGenerator = new Instance_entertainment_movie();
        this.scriptGenerator = new Script_entertainment_movie();
        this.questionGenerator = new Questions_entertainment_movie();
    }
}
