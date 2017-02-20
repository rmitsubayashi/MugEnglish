package com.example.ryomi.myenglish.questiongenerator;

public class QGUtils {
    public static String stripWikidataID(String str){
        int lastIndexID = str.lastIndexOf('/');
        String result = str.substring(lastIndexID+1);
        return result;
    }
}
