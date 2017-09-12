package com.linnca.pelicann.mainactivity.widgets;

public class ToolbarSpinnerItem {
    private final String text;
    private final int imageID;

    public ToolbarSpinnerItem(String text, int imageID) {
        this.text = text;
        this.imageID = imageID;
    }

    public String getText() {
        return text;
    }

    public int getImageID() {
        return imageID;
    }
}
