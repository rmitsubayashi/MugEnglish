package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_food_restaurant;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_food_restaurant;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_food_restaurant;

public class Food_restaurant extends Lesson {
    public static final String KEY = "foodRestaurant";

    public Food_restaurant(){
        this.lessonInstanceGenerator = new Instance_food_restaurant();
        this.questionGenerator = new Questions_food_restaurant();
        this.scriptGenerator = new Script_food_restaurant();
    }
}
