package com.linnca.pelicann.mainactivity.widgets;

public class ToolbarState {
    public static final String NO_TITLE_WITH_SPINNER = "no title with spinner";
    private String title;
    private boolean searchIcon;
    //null if we wanat the icon to be hidden
    private String descriptionLessonKey;

    public ToolbarState(String title, boolean searchIcon,
                        String descriptionLessonKey) {
        this.title = title;
        this.searchIcon = searchIcon;
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

    public String getDescriptionLessonKey() {
        return descriptionLessonKey;
    }

    public void setDescriptionLessonKey(String descriptionLessonKey) {
        this.descriptionLessonKey = descriptionLessonKey;
    }
}
