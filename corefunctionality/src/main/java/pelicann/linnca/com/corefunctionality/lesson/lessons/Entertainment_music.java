package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_entertainment_music;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_entertainment_music;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_entertainment_music;

public class Entertainment_music extends Lesson {
    public static final String KEY = "entertainmentMusic";

    public Entertainment_music(){
        this.lessonInstanceGenerator = new Instance_entertainment_music();
        this.scriptGenerator = new Script_entertainment_music();
        this.questionGenerator = new Questions_entertainment_music();
    }
}
