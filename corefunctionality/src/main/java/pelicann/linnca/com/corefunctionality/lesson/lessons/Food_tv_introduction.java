package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_food_tv_introduction;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_food_tv_introduction;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_food_tv_introduction;

public class Food_tv_introduction extends Lesson {
    public static final String KEY = "foodTVIntroduction";

    public Food_tv_introduction(){
        this.lessonInstanceGenerator = new Instance_food_tv_introduction();
        this.scriptGenerator = new Script_food_tv_introduction();
        this.questionGenerator = new Questions_food_tv_introduction();
    }
}
