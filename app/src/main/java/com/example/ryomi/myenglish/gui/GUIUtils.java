package com.example.ryomi.myenglish.gui;

import android.content.Context;
import android.widget.ImageView;

import com.example.ryomi.myenglish.R;

import java.util.List;

public class GUIUtils {
    private GUIUtils(){}
    public static int stringToDrawableID(String imageString, Context context){
        int imageID = context.getResources().getIdentifier(imageString, "drawable",
                context.getApplicationInfo().packageName);
        return imageID;
    }

    public static void populateStars(List<ImageView> imageViews, List<Boolean> starsEnabled){
        int starCt = imageViews.size();
        for(int i=0; i<starCt; i++){
            ImageView imageView = imageViews.get(i);
            Boolean starEnabled = starsEnabled.get(i);
            //should not happen but just in case
            if (starEnabled == null){
                imageView.setImageResource(R.drawable.star_disabled);
            } else if (starEnabled){
                imageView.setImageResource(R.drawable.star);
            } else {
                imageView.setImageResource(R.drawable.star_disabled);
            }
        }
    }
}
