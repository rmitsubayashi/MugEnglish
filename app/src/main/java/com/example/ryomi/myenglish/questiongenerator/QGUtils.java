package com.example.ryomi.myenglish.questiongenerator;

/**
 * Created by ryomi on 1/20/2017.
 */

public class QGUtils {
    public static String stripWikidataID(String str){
        int lastIndexID = str.lastIndexOf('/');
        String result = str.substring(lastIndexID+1);
        return result;
    }
}
