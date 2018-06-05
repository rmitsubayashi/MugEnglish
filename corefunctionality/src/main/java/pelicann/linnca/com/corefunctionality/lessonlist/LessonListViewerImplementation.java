package pelicann.linnca.com.corefunctionality.lessonlist;

import java.util.ArrayList;
import java.util.List;

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
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_fired;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Work_hired;

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
        food.addLessonKey(Food_restaurant.KEY);
        food.addLessonKey(Food_class.KEY);
        food.setPersonConsistency(true);
        level1.add(food);
        LessonCategory work = new LessonCategory();
        work.setKey("WORK");
        work.setTitleJP("仕事");
        work.addLessonKey(Work_hired.KEY);
        work.addLessonKey(Work_fired.KEY);
        work.setPersonConsistency(true);
        level1.add(work);
        LessonCategory sports = new LessonCategory();
        sports.setKey("SPORTS");
        sports.setTitleJP("スポーツ");
        sports.addLessonKey(Sports_play.KEY);
        sports.addLessonKey(Sports_introduction.KEY);
        level1.add(sports);
        LessonCategory socialMedia = new LessonCategory();
        socialMedia.setKey("SOCIALMEDIA");
        socialMedia.setTitleJP("ソーシャルメディア");
        socialMedia.addLessonKey(Social_media_blog.KEY);
        socialMedia.addLessonKey(Social_media_twitter.KEY);
        level1.add(socialMedia);
        LessonCategory emergency = new LessonCategory();
        emergency.setKey("EMERGENCY");
        emergency.setTitleJP("緊急時");
        emergency.addLessonKey(Emergency_phone.KEY);
        emergency.addLessonKey(Emergency_blood.KEY);
        level1.add(emergency);
        LessonCategory body = new LessonCategory();
        body.setKey("BODY");
        body.setTitleJP("体");
        body.addLessonKey(Body_height.KEY);
        body.addLessonKey(Body_weight.KEY);
        level1.add(body);
        LessonCategory entertainment = new LessonCategory();
        entertainment.setKey("ENTERTAINMENT");
        entertainment.setTitleJP("エンターテインメント");
        entertainment.addLessonKey(Entertainment_movie.KEY);
        entertainment.addLessonKey(Entertainment_music.KEY);
        entertainment.addLessonKey(Entertainment_actors.KEY);
        level1.add(entertainment);
        lessonLevels.add(level1);
    }
}
