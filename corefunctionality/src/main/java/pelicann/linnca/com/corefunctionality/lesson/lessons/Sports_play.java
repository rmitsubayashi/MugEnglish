package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_sports_play;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_sports_play;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_sports_play;

public class Sports_play extends Lesson {
    public static final String KEY = "sportsPlay";

    public Sports_play(){
        this.lessonInstanceGenerator = new Instance_sports_play();
        this.scriptGenerator = new Script_sports_play();
        this.questionGenerator = new Questions_sports_play();
    }
}
