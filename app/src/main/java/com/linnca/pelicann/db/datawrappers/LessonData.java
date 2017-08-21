package com.linnca.pelicann.db.datawrappers;

import java.io.Serializable;

//serializable so we can pass it from the lesson list to
// the lesson details
public class LessonData implements Serializable{
    private String key;
    private String title;
    private String description;

    public LessonData(){}

    public LessonData(String key, String title,
                      String description){
        this.key = key;
        this.title = title;
        this.description = description;
    }

    public String getKey(){
        return this.key;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDescription(){
        return this.description;
    }

    public void setKey(String key){
        this.key = key;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

}
