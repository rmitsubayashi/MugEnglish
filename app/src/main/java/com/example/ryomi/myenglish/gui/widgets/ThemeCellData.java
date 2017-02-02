package com.example.ryomi.myenglish.gui.widgets;

import com.example.ryomi.myenglish.db.datawrappers.ThemeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps info needed for each cell in gui
 * has the theme info + color to apply + stars (if user is logged in)
 */

public class ThemeCellData {
    private ThemeData themeData;
    //background color for the theme cell
    private String colorString;
    //stars for the current user for the theme
    private List<Boolean> starList;

    public ThemeCellData(ThemeData themeData, String colorString,
                 List<Boolean> starList){
        this.themeData = themeData;
        this.colorString = colorString;
        //copy
        this.starList = new ArrayList<>(starList);
    }

    public ThemeData getThemeData(){ return this.themeData; }

    public String getColorString(){
        return this.colorString;
    }

    public List<Boolean> getStarList(){
        return this.starList;
    }

}
