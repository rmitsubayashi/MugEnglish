package com.linnca.pelicann.questions;

import java.io.Serializable;
import java.util.List;

public class InstanceRecord implements Serializable{
    private String id;
    private String instanceId;
    private String lessonId;
    private List<QuestionAttempt> attempts;
    private Boolean completed;

    InstanceRecord(){}

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

    public Boolean getCompleted() {
        return completed;
    }

    void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object object){
        if (object == null)
            return false;

        if (!(object instanceof InstanceRecord))
            return false;

        InstanceRecord record = (InstanceRecord) object;
        return  ((record.getId()).equals(this.id));
    }

    @Override
    public int hashCode(){
        return 17 * 31 + this.id.hashCode();
    }
}
