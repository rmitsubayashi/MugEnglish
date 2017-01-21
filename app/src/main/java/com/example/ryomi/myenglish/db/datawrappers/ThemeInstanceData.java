package com.example.ryomi.myenglish.db.datawrappers;

import java.util.List;

public class ThemeInstanceData {
    private String id;
    private String themeId;
    private String userId; //can be blank
    private List<String> questionIds;
    private long createdTimestamp;

    public ThemeInstanceData(){}

    public ThemeInstanceData(String id, String themeId,
                     String userId, List<String> questionIds, long createdTimestamp) {
        this.id = id;
        this.themeId = themeId;
        this.userId = userId;
        this.questionIds = questionIds;
        this.createdTimestamp = createdTimestamp;
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
}
