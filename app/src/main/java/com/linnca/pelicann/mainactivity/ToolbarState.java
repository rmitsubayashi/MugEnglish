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

    public void setTitle(String title) {
        this.title = title;
    }

    boolean searchIconVisible() {
        return searchIcon;
    }

    public void setSearchIcon(boolean visible) {
        this.searchIcon = visible;
    }
}
