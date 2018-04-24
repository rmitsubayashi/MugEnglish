package pelicann.linnca.com.corefunctionality.lessonlist;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_restaurant;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Food_tv_introduction;
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
        introduction.setPersonConsistency(false);
        level1.add(introduction);
        LessonCategory food = new LessonCategory();
        food.setKey("FOOD");
        food.setTitleJP("食べ物");
        food.addLessonKey(Food_tv_introduction.KEY);
        food.addLessonKey((Food_restaurant.KEY));
        food.setPersonConsistency(true);
        level1.add(food);
        lessonLevels.add(level1);
    }
}
