package com.linnca.pelicann.lessondetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//serializable so we can pass it from the lesson list to
// the lesson details
public class LessonData implements Serializable{
    private String key;
    private String title;
    private Integer descriptionLayout;
    private List<String> prerequisiteKeys;
    private int toClearScore;
    private int colorID;
    private int iconID;

    public LessonData(){}

    public LessonData(String key, String title,
                      Integer descriptionLayout,
                      List<String> prerequisiteKeys,
                      int toClearScore,
                      int colorID,
                      int iconID){
        this.key = key;
        this.title = title;
        this.descriptionLayout = descriptionLayout;
        this.prerequisiteKeys = prerequisiteKeys;
        this.toClearScore = toClearScore;
        this.colorID = colorID;
        this.iconID = iconID;
    }

    public LessonData(LessonData copy){
        this.key = copy.key;
        this.title = copy.title;
        this.descriptionLayout = copy.descriptionLayout;
        this.prerequisiteKeys = new ArrayList<>(prerequisiteKeys);
        this.toClearScore = copy.toClearScore;
        this.colorID = copy.colorID;
        this.iconID = copy.iconID;
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

    public int getToClearScore() {
        return toClearScore;
    }

    public void setToClearScore(int toClearScore) {
        this.toClearScore = toClearScore;
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
}
