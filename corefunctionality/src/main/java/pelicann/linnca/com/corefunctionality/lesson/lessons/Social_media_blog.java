package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_social_media_blog;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_social_media_blog;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_social_media_blog;

public class Social_media_blog extends Lesson {
    public static final String KEY = "socialMediaBlog";

    public Social_media_blog(){
        this.instanceGenerator = new Instance_social_media_blog();
        this.scriptGenerator = new Script_social_media_blog();
        this.questionGenerator = new Questions_social_media_blog();
    }
}
