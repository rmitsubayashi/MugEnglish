package com.example.ryomi.mugenglish.db.datawrappers;

import java.io.Serializable;

//serializable so we can pass it from the theme list to
// the theme details
public class ThemeData implements Serializable{
    private String id;
    private String image;
    private String title;
    private String category;
    private String description;

    public ThemeData(){}

    public ThemeData(String id, String image, String title, String category,
                         String description){
        this.id = id;
        this.image = image;
        this.title = title;
        this.category = category;
        this.description = description;
    }

    public String getId(){
        return this.id;
    }

    public String getImage(){
        return this.image;
    }

    public String getTitle(){
        return this.title;
    }

    public String getCategory(){
        return this.category;
    }

    public String getDescription(){
        return this.description;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setImage(String image){
        this.image = image;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setCategory(String category) {this.category = category; }

    public void setDescription(String description){
        this.description = description;
    }

}
