package com.linnca.pelicann.gui.widgets;

public class ToolbarState {
    public static String NO_TITLE_WITH_SPINNER = "no title with spinner";
    private String title;
    private boolean searchIcon;
    private boolean descriptionIcon;
    private String descriptionLessonKey;

    public ToolbarState(String title, boolean searchIcon, boolean descriptionIcon,
                        String descriptionLessonKey) {
        this.title = title;
        this.searchIcon = searchIcon;
        this.descriptionIcon = descriptionIcon;
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

    public boolean descriptionIconVisible() {
        return descriptionIcon;
    }

    public void setDescriptionIcon(boolean visible) {
        this.descriptionIcon = visible;
    }

    public String getDescriptionLessonKey() {
        return descriptionLessonKey;
    }

    public void setDescriptionLessonKey(String descriptionLessonKey) {
        this.descriptionLessonKey = descriptionLessonKey;
    }
}
