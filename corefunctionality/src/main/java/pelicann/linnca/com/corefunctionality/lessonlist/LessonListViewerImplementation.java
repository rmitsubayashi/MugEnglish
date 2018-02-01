package pelicann.linnca.com.corefunctionality.lessonlist;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_age;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Introduction_team_from;

public class LessonListViewerImplementation extends LessonListViewer {
    public void populateLessons(){
        //level 1
        List<LessonCategory> level1 = new ArrayList<>();
        LessonCategory introduction = new LessonCategory();
        introduction.setKey("INTRODUCTION");
        introduction.setTitleJP("自己紹介");
        introduction.addLessonKey(Introduction_team_from.KEY);
        introduction.addLessonKey(Introduction_age.KEY);
        level1.add(introduction);
        lessonLevels.add(level1);
    }
}
