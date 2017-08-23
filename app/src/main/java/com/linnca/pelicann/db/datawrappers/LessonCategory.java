package com.linnca.pelicann.db.datawrappers;

import java.util.ArrayList;
import java.util.List;

public class LessonCategory {
    private int id;
    private String title;
    private List<LessonData> lessons = new ArrayList<>();

    public LessonCategory(){}

    public LessonCategory(int id, String title){
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addLesson(LessonData lessonData){
        lessons.add(lessonData);
    }

    public List<LessonData> getLessons(){
        //don't need a deep copy
        return lessons;
    }
}
