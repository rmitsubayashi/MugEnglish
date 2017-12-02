package com.linnca.pelicann.mainactivity;

public class ToolbarSpinnerItem {
    private final String text;
    private final int imageID;

    ToolbarSpinnerItem(String text, int imageID) {
        this.text = text;
        this.imageID = imageID;
    }

    public String getText() {
        return text;
    }

    int getImageID() {
        return imageID;
    }
}
