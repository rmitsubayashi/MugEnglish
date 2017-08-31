package com.linnca.pelicann.gui.widgets;

public class ToolbarSpinnerItem {
    private String text;
    private int imageID;

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
