package com.linnca.pelicann.questions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

//adjusts the layout so it's a square based on width
public class GridContainer extends RelativeLayout {
    public GridContainer(Context context) {
        super(context);
    }

    public GridContainer(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public GridContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }
}
