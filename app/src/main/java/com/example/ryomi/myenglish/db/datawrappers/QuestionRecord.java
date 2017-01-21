package com.example.ryomi.myenglish.db.datawrappers;

import java.util.Map;

public class QuestionRecord {
    private String id;
    private String questionId;
    private String instanceId;
    private String startTimestamp;
    private String endTimestamp;
    private String response;
    private Boolean correct;
    private Map<String, Integer> vocabularyTouchedCt;

    public QuestionRecord(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Map<String, Integer> getVocabularyTouchedCt() {
        return vocabularyTouchedCt;
    }

    public void setVocabularyTouchedCt(Map<String, Integer> vocabularyTouchedCt) {
        this.vocabularyTouchedCt = vocabularyTouchedCt;
    }
}
