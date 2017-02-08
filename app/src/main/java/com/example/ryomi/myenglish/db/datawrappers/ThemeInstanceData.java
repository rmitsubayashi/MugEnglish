package com.example.ryomi.myenglish.db.datawrappers;

import java.io.Serializable;
import java.util.List;

public class ThemeInstanceData implements Serializable{
    private String id;
    private String themeId;
    private String userId; //can be blank
    private List<String> questionIds;
    //basically for displaying the instances to the users
    private List<String> topics; //actual name, not ID
    private long createdTimestamp;
    //we can fetch it by making another request
    //but this is much simpler
    private long lastPlayed;

    public ThemeInstanceData(){}

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

    public ThemeInstanceData(String id, String themeId,
                             String userId, List<String> questionIds, List<String> topics,
                             long createdTimestamp, long lastPlayed) {
        this.id = id;
        this.themeId = themeId;
        this.userId = userId;
        this.questionIds = questionIds;
        this.topics = topics;
        this.createdTimestamp = createdTimestamp;
        this.lastPlayed = lastPlayed;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
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

        if (!(object instanceof ThemeInstanceData))
            return false;

        ThemeInstanceData data = (ThemeInstanceData) object;
        return  (data.getId().equals(this.id));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
}
