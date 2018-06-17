package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_food_class;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_food_class;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_food_class;

public class Food_class extends Lesson {
    public static final String KEY = "foodClass";

    public Food_class(){
        this.instanceGenerator = new Instance_food_class();
        this.scriptGenerator = new Script_food_class();
        this.questionGenerator = new Questions_food_class();
    }
}
