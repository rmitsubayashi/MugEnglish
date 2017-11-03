package com.linnca.pelicann.userprofile;

public class UserProfile_ReportCardDataWrapper {
    private String lessonKey;
    private int correctCt;
    private int totalCt;

    public UserProfile_ReportCardDataWrapper(String lessonKey, int correctCt, int totalCt) {
        this.lessonKey = lessonKey;
        this.correctCt = correctCt;
        this.totalCt = totalCt;
    }

    public String getLessonKey() {
        return lessonKey;
    }

    public void setLessonKey(String lessonKey) {
        this.lessonKey = lessonKey;
    }

    public int getCorrectCt() {
        return correctCt;
    }

    public void setCorrectCt(int correctCt) {
        this.correctCt = correctCt;
    }

    public int getTotalCt() {
        return totalCt;
    }

    public void setTotalCt(int totalCt) {
        this.totalCt = totalCt;
    }
}
