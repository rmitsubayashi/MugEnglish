package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ThemeCellImageView extends ImageView {
    //percent padding
    private double paddingTop = 0.2;
    private double paddingBottom = 0.2;
    private double paddingLeft = 0.25;
    private double paddingRight = 0.25;

    public ThemeCellImageView(Context context) {
        super(context);
    }

    public ThemeCellImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeCellImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        //make it a square
        setMeasuredDimension(width, width);
        //set padding
        int pTop = (int)(width * paddingTop);
        int pBottom = (int)(width * paddingBottom);
        int pLeft = (int)(width * paddingLeft);
        int pRight = (int)(width * paddingRight);

        setPadding(pLeft,pTop,pRight,pBottom);
    }

}