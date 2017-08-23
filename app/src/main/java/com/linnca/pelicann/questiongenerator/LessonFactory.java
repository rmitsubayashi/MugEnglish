package com.linnca.pelicann.questiongenerator;

//create an instance of the required class

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.questiongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.questiongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.questiongenerator.lessons.The_DEMONYM_flag_is_COLORS;

public class LessonFactory {
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
            default:
                return null;
        }
    }
}
