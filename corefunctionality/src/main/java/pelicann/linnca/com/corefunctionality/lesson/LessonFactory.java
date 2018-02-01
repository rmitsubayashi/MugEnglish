package pelicann.linnca.com.corefunctionality.lesson;

//create an instance of the required lesson class

import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_age;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_team_from;

public final class LessonFactory {
    private static final String TAG = "LessonFactory";
    private LessonFactory(){}
    public static Lesson getLesson(String lessonKey){
        switch (lessonKey){
            case Introduction_team_from.KEY :
                return new Introduction_team_from();
            case Introduction_age.KEY :
                return new Introduction_age();
        }
        return null;
    }
}
