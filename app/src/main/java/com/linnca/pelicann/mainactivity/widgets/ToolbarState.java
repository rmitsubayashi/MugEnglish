package com.linnca.pelicann.mainactivity.widgets;

public class ToolbarState {
    private String title;
    private boolean searchIcon;
    private boolean spinner;
    //null if we want the icon to be hidden
    private String descriptionLessonKey;

    public ToolbarState(String title, boolean searchIcon,
                        boolean spinner,
                        String descriptionLessonKey) {
        this.title = title;
        this.searchIcon = searchIcon;
        this.spinner = spinner;
        this.descriptionLessonKey = descriptionLessonKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean searchIconVisible() {
        return searchIcon;
    }

    public void setSearchIcon(boolean visible) {
        this.searchIcon = visible;
    }

    public boolean spinnerVisible() {
        return spinner;
    }

    public void setSpinner(boolean spinner) {
        this.spinner = spinner;
    }

    public String getDescriptionLessonKey() {
        return descriptionLessonKey;
    }

    public void setDescriptionLessonKey(String descriptionLessonKey) {
        this.descriptionLessonKey = descriptionLessonKey;
    }
}
