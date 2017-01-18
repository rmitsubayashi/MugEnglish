package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class DynamicPaddedCell extends LinearLayout{
    public DynamicPaddedCell(Context context) {
        super(context);
    }

    public DynamicPaddedCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicPaddedCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int paddingVertical = (int)(height * 0.1);
        int paddingHorizontal = (int) (width * 0.1);

        setPadding(paddingHorizontal,paddingVertical,paddingHorizontal,paddingVertical);
    }
}
