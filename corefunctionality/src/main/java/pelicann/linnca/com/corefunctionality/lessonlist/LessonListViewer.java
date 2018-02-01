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
    protected final List<List<LessonCategory>> lessonLevels = new ArrayList<>();

    public LessonListViewer(){
        populateLessons();
    }

    abstract protected void populateLessons();

    public List<LessonCategory> getLessonsAtLevel(int level){
        level = level - 1;
        return lessonLevels.get(level);
    }

    //1, 2, 3, ...
    public int getLessonLevel(String lessonKey){
        int levelCt = lessonLevels.size();
        for (int i=0; i<levelCt; i++) {
            List<LessonCategory> lessonCategories = lessonLevels.get(i);
            for (LessonCategory row : lessonCategories) {
                if (row.hasLesson(lessonKey)){
                    return i+1;
                }
            }
        }
        return -1;
    }
}
