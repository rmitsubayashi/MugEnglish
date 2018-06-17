package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_social_media_twitter;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_social_media_twitter;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_social_media_twitter;

public class Social_media_twitter extends Lesson {
    public static final String KEY = "socialMediaTwitter";

    public Social_media_twitter(){
        this.instanceGenerator = new Instance_social_media_twitter();
        this.scriptGenerator = new Script_social_media_twitter();
        this.questionGenerator = new Questions_social_media_twitter();
    }
}
