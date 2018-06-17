package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.io.Serializable;
import java.util.List;

public class InstanceAttemptRecord implements Serializable{
    private String id;
    private String instanceId;
    private String lessonId;
    private List<QuestionAttempt> attempts;

    InstanceAttemptRecord(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getLessonId() {
        return lessonId;
    }

    void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public List<QuestionAttempt> getAttempts() {
        return attempts;
    }

    void setAttempts(List<QuestionAttempt> attempts) {
        this.attempts = attempts;
    }

    @Override
    public boolean equals(Object object){
        if (object == null)
            return false;

        if (!(object instanceof InstanceAttemptRecord))
            return false;

        InstanceAttemptRecord record = (InstanceAttemptRecord) object;
        return  ((record.getId()).equals(this.id));
    }

    @Override
    public int hashCode(){
        return 17 * 31 + this.id.hashCode();
    }
}
