package com.linnca.pelicann.lessondetails;

import java.io.Serializable;
import java.util.List;

//serializable so we can pass it from the lesson list to
// the lesson details
public class LessonData implements Serializable{
    private String key;
    private String title;
    private Integer descriptionLayout;
    private List<String> prerequisiteKeys;
    //i.e. 4/5 of the prerequisites to unlock
    private int prerequisiteLeeway = 0;
    private int colorID;
    private int iconID;

    public LessonData(){}

    public LessonData(String key, String title,
                      Integer descriptionLayout,
                      List<String> prerequisiteKeys,
                      int colorID,
                      int iconID){
        this.key = key;
        this.title = title;
        this.descriptionLayout = descriptionLayout;
        this.prerequisiteKeys = prerequisiteKeys;
        this.colorID = colorID;
        this.iconID = iconID;
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

    public List<String> getPrerequisiteKeys() {
        return prerequisiteKeys;
    }

    public void setPrerequisiteKeys(List<String> prerequisiteKeys) {
        this.prerequisiteKeys = prerequisiteKeys;
    }

    public int getColorID() {
        return colorID;
    }

    public void setColorID(int colorID) {
        this.colorID = colorID;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public int getPrerequisiteLeeway() {
        return prerequisiteLeeway;
    }

    public void setPrerequisiteLeeway(int prerequisiteLeeway) {
        this.prerequisiteLeeway = prerequisiteLeeway;
    }
}
