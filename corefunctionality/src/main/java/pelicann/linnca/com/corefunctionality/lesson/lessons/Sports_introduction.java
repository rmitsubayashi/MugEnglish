package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_sports_introduction;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_sports_introduction;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_sports_introduction;

public class Sports_introduction extends Lesson {
    public static final String KEY = "sportsIntroduction";

    public Sports_introduction(){
        this.lessonInstanceGenerator = new Instance_sports_introduction();
        this.scriptGenerator = new Script_sports_introduction();
        this.questionGenerator = new Questions_sports_introduction();
    }
}
