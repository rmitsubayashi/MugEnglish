package com.linnca.pelicann.mainactivity;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public final class GUIUtils {
    /*
    //request code for firebase sign in
    public static final int REQUEST_CODE_SIGN_IN = 190;
    //which sign in methods to display to the user
    public static final int SIGN_IN_PROVIDER_ALL = 0;
    //these are for searching via facebook or twitter
    public static final int SIGN_IN_PROVIDER_FACEBOOK = 1;
    public static final int SIGN_IN_PROVIDER_TWITTER = 2;*/

    private GUIUtils(){}
    public static int stringToDrawableID(String imageString, Context context){
        return context.getResources().getIdentifier(imageString, "drawable",
                context.getApplicationInfo().packageName);
    }

    public static int getDp(int num, Context context){
        return (int)(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, num, context.getResources().getDisplayMetrics()));

    }

    public static boolean hideKeyboard(View targetViewOfKeyboard){
        if (targetViewOfKeyboard == null){
            return false;
        }
        targetViewOfKeyboard.clearFocus();
        InputMethodManager imm = (InputMethodManager) targetViewOfKeyboard.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        return  imm != null &&
                imm.hideSoftInputFromWindow(targetViewOfKeyboard.getWindowToken(), 0);

    }
}
