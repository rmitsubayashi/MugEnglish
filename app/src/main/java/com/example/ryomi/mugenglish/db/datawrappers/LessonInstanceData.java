package com.example.ryomi.mugenglish.db.datawrappers;

import java.io.Serializable;
import java.util.List;

public class LessonInstanceData implements Serializable{
    private String id;
    private String themeId;
    private String userId; //can be blank?
    //list of the question ids of a set
    private List<List<String>> questionSets;
    //basically for displaying the instances to the users
    private List<String> topics; //actual name, not ID
    private long createdTimestamp;
    //we can fetch it by making another request
    //but this is much simpler
    private long lastPlayed;

    public LessonInstanceData(){}

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public LessonInstanceData(String id, String themeId,
                              String userId, List<List<String>> questionSets, List<String> topics,
                              long createdTimestamp, long lastPlayed) {
        this.id = id;
        this.themeId = themeId;
        this.userId = userId;
        this.questionSets = questionSets;
        this.topics = topics;
        this.createdTimestamp = createdTimestamp;
        this.lastPlayed = lastPlayed;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setLessonId(String themeId) {
        this.themeId = themeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<List<String>> getQuestionSets() {
        return questionSets;
    }

    public void setQuestionSets(List<List<String>> questionSets) {
        this.questionSets = questionSets;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //the id is the only value we need to check
    @Override
    public boolean equals(Object object){
        if (object == null)
            return false;

        if (!(object instanceof LessonInstanceData))
            return false;

        LessonInstanceData data = (LessonInstanceData) object;
        return  (data.getId().equals(this.id));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
}
