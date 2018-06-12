package pelicann.linnca.com.corefunctionality.lesson;

//create an instance of the required lesson class

import pelicann.linnca.com.corefunctionality.lesson.lessons.Body_height;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Body_weight;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Emergency_blood;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Emergency_phone;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Entertainment_actors;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Entertainment_movie;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Entertainment_music;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_class;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_restaurant;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_tv_introduction;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_age;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_team_from;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Social_media_blog;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Social_media_twitter;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Sports_introduction;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Sports_play;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_about;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_fired;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_hired;

public final class LessonFactory {
    private static final String TAG = "LessonFactory";
    private LessonFactory(){}
    public static Lesson getLesson(String lessonKey){
        //alphabetical order (roughly)
        switch (lessonKey){
            case Body_weight.KEY :
                return new Body_weight();
            case Body_height.KEY :
                return new Body_height();
            case Emergency_phone.KEY :
                return new Emergency_phone();
            case Emergency_blood.KEY :
                return new Emergency_blood();
            case Entertainment_movie.KEY :
                return new Entertainment_movie();
            case Entertainment_music.KEY :
                return new Entertainment_music();
            case Entertainment_actors.KEY :
                return new Entertainment_actors();
            case Food_tv_introduction.KEY :
                return new Food_tv_introduction();
            case Food_restaurant.KEY :
                return new Food_restaurant();
            case Food_class.KEY :
                return new Food_class();
            case Introduction_team_from.KEY :
                return new Introduction_team_from();
            case Introduction_age.KEY :
                return new Introduction_age();
            case Social_media_blog.KEY :
                return new Social_media_blog();
            case Social_media_twitter.KEY :
                return new Social_media_twitter();
            case Sports_play.KEY :
                return new Sports_play();
            case Sports_introduction.KEY :
                return new Sports_introduction();
            case Work_hired.KEY :
                return new Work_hired();
            case Work_fired.KEY :
                return new Work_fired();
            case Work_about.KEY :
                return new Work_about();
            default:
                return null;
        }
    }
}
