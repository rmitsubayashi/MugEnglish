package com.example.ryomi.mugenglish.gui.widgets;

public class UserProfileReportCardData {
    private String themeID;
    private String themeName = "";
    private int correctCt = 0;
    private int totalCt = 0;
    private int instanceCt = 0;
    private int recordCt = 0;

    public UserProfileReportCardData() {
    }

    public String getThemeID(){
        return themeID;
    }

    public void setThemeID(String themeID){
        this.themeID = themeID;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public void incrementCorrectCt(){
        this.correctCt++;
    }

    public void incrementTotalCt(){
        this.totalCt ++;
    }

    public String getAccuracy(){
        double percentageDecimals = ((double)correctCt) / ((double)totalCt);
        int percentage = (int)(percentageDecimals * 100);
        return Integer.toString(percentage) + "%";
    }

    public String getInstanceCt() {
        return Integer.toString(instanceCt);
    }

    public void incrementInstanceCt() {
        this.instanceCt++;
    }

    public String getRecordCt() {
        return Integer.toString(recordCt);
    }

    public void incrementRecordCt() {
        this.recordCt++;
    }
}
