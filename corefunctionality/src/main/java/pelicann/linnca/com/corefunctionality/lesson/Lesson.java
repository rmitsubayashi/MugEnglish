package pelicann.linnca.com.corefunctionality.lesson;

import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.db.NetworkConnectionChecker;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.InstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionData;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionGenerator;
import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptGenerator;

public abstract class Lesson {
    protected String key;
    protected InstanceGenerator instanceGenerator;
    protected ScriptGenerator scriptGenerator;
    protected QuestionGenerator questionGenerator;

    public void createLessonInstance(EndpointConnectorReturnsXML connector, Database db,
                                     InstanceGenerator.LessonInstanceGeneratorListener lessonListener,
                                     NetworkConnectionChecker networkConnectionChecker){
        instanceGenerator.createInstance(connector, db, lessonListener,
                networkConnectionChecker);
    }

    public Script createScript(List<EntityPropertyData> data){
        return scriptGenerator.makeScript(data);
    }

    public List<QuestionData> createQuestions(List<EntityPropertyData> data){
        List<QuestionData> questions = questionGenerator.makeQuestions(data);
        int questionNum = 1;
        for (QuestionData question : questions){
            question.setId(key + questionNum);
            questionNum++;
        }
        return questions;
    }
}
