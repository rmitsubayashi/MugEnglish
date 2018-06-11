package pelicann.linnca.com.corefunctionality.lessonlist;

import java.util.ArrayList;
import java.util.List;

//stores the lesson list information
// and formats it for viewing
public abstract class LessonListViewer {
    // +level
    //   +category
    //     +lesson
    //     +lesson
    //   +category
    //     +lesson
    final List<LessonCategory> lessons = new ArrayList<>();

    LessonListViewer(){
        populateLessons();
    }

    abstract protected void populateLessons();

    public List<LessonCategory> getLessons(){
        //we only have one level
        return lessons;
    }
}
