package com.example.ryomi.myenglish.db.datawrappers;

import java.util.List;

public class InstanceRecord {
    private String id;
    private String instanceId;
    private List<QuestionAttempt> attempts;
    private Boolean completed;

    public InstanceRecord(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public List<QuestionAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<QuestionAttempt> attempts) {
        this.attempts = attempts;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
