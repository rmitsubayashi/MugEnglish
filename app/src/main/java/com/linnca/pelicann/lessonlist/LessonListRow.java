package com.linnca.pelicann.lessonlist;

import com.linnca.pelicann.lessondetails.LessonData;

public class LessonListRow {
    private final int lessonsPerRow = 3;
    private LessonData[] lessons = new LessonData[lessonsPerRow];
    private boolean isReview = false;

    public LessonListRow(){}

    void setCol1(LessonData data){
        lessons[0] = data;
    }

    void setCol2(LessonData data){
        lessons[1] = data;
    }

    void setCol3(LessonData data){
        lessons[2] = data;
    }

    void setReview(boolean isReview){
        this.isReview = isReview;
    }

    public boolean isReview(){
        return isReview;
    }

    public LessonData[] getLessons(){
        return lessons;
    }

}
