package com.linnca.pelicann.mainactivity;



import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import com.linnca.pelicann.R;

public class ThemeColorChanger {
    public static final int BLUE = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;

    static void setTheme(ContextThemeWrapper contextThemeWrapper, int theme){
        int themeID;
        switch (theme){
            case BLUE:
                themeID = R.style.AppTheme_Blue;
                break;
            case GREEN:
                themeID = R.style.AppTheme_Green;
                break;
            case YELLOW:
                themeID = R.style.AppTheme_Yellow;
                break;
            default:
                themeID = R.style.AppTheme_Blue;
        }

        contextThemeWrapper.setTheme(themeID);
    }

    @ColorInt
    public static int getColorFromAttribute(int attrID, Context context){
        //since the attribute IDs reference another color ID instead of a color,
        // we can't get the color with getColor()
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrID, typedValue, true);
        return typedValue.data;
    }
}
