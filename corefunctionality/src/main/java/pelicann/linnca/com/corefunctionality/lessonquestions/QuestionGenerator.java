package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;

public abstract class QuestionGenerator {
    //5 or 10 questions regarding the script
    public abstract List<QuestionData> makeQuestions(List<EntityPropertyData> data);
}
