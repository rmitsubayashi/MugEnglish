package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_introduction_team_from;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_introduction_team_from;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_introduction_team_from;

/*
* 0. person
* 1. team
* 2. team city
* 3. city
* 4. country
* 5. pic
* 6. 1st name
* */

public class Introduction_team_from extends Lesson {
    public static final String KEY = "introductionTeamFrom";

    public Introduction_team_from(){
        this.instanceGenerator = new Instance_introduction_team_from();
        this.scriptGenerator = new Script_introduction_team_from();
        this.questionGenerator = new Questions_introduction_team_from();
        this.key = KEY;
    }
}
