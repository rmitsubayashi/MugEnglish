package com.linnca.pelicann.lessongenerator;

//create an instance of the required class

import android.util.Log;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.lessons.COMPANY_makes_PRODUCT;
import com.linnca.pelicann.lessongenerator.lessons.Hello_my_name_is_NAME;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_AGE_years_old;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_DEMONYM;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_a_OCCUPATION;
import com.linnca.pelicann.lessongenerator.lessons.NAME_is_at_work_He_is_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.NAME_plays_SPORT;
import com.linnca.pelicann.lessongenerator.lessons.NAME_works_at_EMPLOYER;
import com.linnca.pelicann.lessongenerator.lessons.PLACE_is_a_COUNTRY_CITY;
import com.linnca.pelicann.lessongenerator.lessons.The_DEMONYM_flag_is_COLORS;
import com.linnca.pelicann.lessongenerator.lessons.The_emergency_phone_number_of_COUNTRY_is_NUMBER;
import com.linnca.pelicann.lessongenerator.lessons.good_morning_afternoon_evening;

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
            case Hello_my_name_is_NAME.KEY :{
                return new Hello_my_name_is_NAME(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case good_morning_afternoon_evening.KEY :{
                return new good_morning_afternoon_evening(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case NAME_is_AGE_years_old.KEY :{
                return new NAME_is_AGE_years_old(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case NAME_plays_SPORT.KEY :{
                return new NAME_plays_SPORT(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case COMPANY_makes_PRODUCT.KEY :{
                return new COMPANY_makes_PRODUCT(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case NAME_works_at_EMPLOYER.KEY :{
                return new NAME_works_at_EMPLOYER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case NAME_is_at_work_He_is_at_EMPLOYER.KEY :{
                return new NAME_is_at_work_He_is_at_EMPLOYER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }
            case PLACE_is_a_COUNTRY_CITY.KEY :{
                return new PLACE_is_a_COUNTRY_CITY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        listener
                );
            }


            default:
                Log.d(TAG, "Could not parse lesson");
                return null;
        }
    }

    public static void saveGenericQuestions(){
        /*Lesson lesson = new good_morning_afternoon_evening(null, null);
        lesson.saveGenericQuestions();
        lesson = new Hello_my_name_is_NAME(null, null);
        lesson.saveGenericQuestions();
        lesson = new COMPANY_makes_PRODUCT(null, null);
        lesson.saveGenericQuestions();*/
        Lesson lesson = new PLACE_is_a_COUNTRY_CITY(null, null);
        lesson.saveGenericQuestions();
    }
}
