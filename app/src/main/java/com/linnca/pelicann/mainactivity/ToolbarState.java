package com.linnca.pelicann.mainactivity;

public class ToolbarState {
    private String title;
    private boolean searchIcon;

    public ToolbarState(String title, boolean searchIcon) {
        this.title = title;
        this.searchIcon = searchIcon;
    }

    public String getTitle() {
        return title;
    }

    boolean searchIconVisible() {
        return searchIcon;
    }
}
