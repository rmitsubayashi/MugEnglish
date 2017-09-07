package com.linnca.pelicann.db.datawrappers;

import java.io.Serializable;

//serializable so we can pass it from the lesson list to
// the lesson details
public class LessonData implements Serializable{
    private String key;
    private String title;
    private Integer descriptionLayout;

    public LessonData(){}

    public LessonData(String key, String title,
                      Integer descriptionLayout){
        this.key = key;
        this.title = title;
        this.descriptionLayout = descriptionLayout;
    }

    public String getKey(){
        return this.key;
    }

    public String getTitle(){
        return this.title;
    }

    public Integer getDescriptionLayout(){
        return this.descriptionLayout;
    }

    public void setKey(String key){
        this.key = key;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescriptionLayout(Integer descriptionLayout){
        this.descriptionLayout = descriptionLayout;
    }

}
