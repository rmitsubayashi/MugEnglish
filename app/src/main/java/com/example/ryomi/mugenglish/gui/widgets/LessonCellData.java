package com.example.ryomi.mugenglish.gui.widgets;

import com.example.ryomi.mugenglish.db.datawrappers.LessonData;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps info needed for each cell in gui
 * has the theme info + color to apply + stars (if user is logged in)
 */

public class LessonCellData {
    private LessonData lessonData;
    //background color for the theme cell
    private String colorString;
    //stars for the current user for the theme
    private List<Boolean> starList;

    public LessonCellData(LessonData lessonData, String colorString,
                          List<Boolean> starList){
        this.lessonData = lessonData;
        this.colorString = colorString;
        //copy
        this.starList = new ArrayList<>(starList);
    }

    public LessonData getLessonData(){ return this.lessonData; }

    public String getColorString(){
        return this.colorString;
    }

    public List<Boolean> getStarList(){
        return this.starList;
    }

}
