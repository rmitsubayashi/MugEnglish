package com.linnca.pelicann.lessongenerator;

//create an instance of the required class

import android.util.Log;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.The_DEMONYM_flag_is_COLORS;
import com.linnca.pelicann.lessongenerator.lessons.The_emergency_phone_number_of_COUNTRY_is_NUMBER;

public class LessonFactory {
    private static final String TAG = "LessonFactory";
    private LessonFactory(){}
    public static Lesson parseLesson(String lessonKey, Lesson.LessonListener listener){
        switch (lessonKey) {
            case NAME_is_DEMONYM.KEY :
                return new NAME_is_DEMONYM(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener);
            case NAME_is_a_OCCUPATION.KEY :
                return new NAME_is_a_OCCUPATION(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            case The_DEMONYM_flag_is_COLORS.KEY :
                return new The_DEMONYM_flag_is_COLORS(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            case The_emergency_phone_number_of_COUNTRY_is_NUMBER.KEY :{
                return new The_emergency_phone_number_of_COUNTRY_is_NUMBER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            default:
                Log.d(TAG, "Could not parse lesson");
                return null;
        }
    }
}
