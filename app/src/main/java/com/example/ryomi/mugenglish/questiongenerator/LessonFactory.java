package com.example.ryomi.mugenglish.questiongenerator;

//create an instance of the required class

import android.util.Log;

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.datawrappers.LessonData;
import com.example.ryomi.mugenglish.questiongenerator.lessons.AUTHOR_wrote_BOOK;
import com.example.ryomi.mugenglish.questiongenerator.lessons.BOOK_was_published_by_PUBLISHER;
import com.example.ryomi.mugenglish.questiongenerator.lessons.CITY_is_in_TERRITORY;
import com.example.ryomi.mugenglish.questiongenerator.lessons.I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY;
import com.example.ryomi.mugenglish.questiongenerator.lessons.MOVIE_has_won_AWARD;
import com.example.ryomi.mugenglish.questiongenerator.lessons.MOVIE_is_a_GENRE;
import com.example.ryomi.mugenglish.questiongenerator.lessons.MOVIE_won_AWARD_in_YEAR;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_is_DEMONYM;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_is_a_GENDER;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_is_a_OCCUPATION;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_is_around_HEIGHT_tall;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_is_playing_SPORT;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_participated_in_WAR;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_plays_SPORT;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_possessive_blood_type_is_BLOODTYPE;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_was_POSITION_from_START_to_END;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_went_to_SCHOOL_So_did_NAME2;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_went_to_SCHOOL_from_START_to_END;
import com.example.ryomi.mugenglish.questiongenerator.lessons.NAME_will_be_AGE_in_X_months;
import com.example.ryomi.mugenglish.questiongenerator.lessons.PREFECTURE_is_next_to_PREFECTURE2;
import com.example.ryomi.mugenglish.questiongenerator.lessons.TAXON_is_named_after_ENTITY;
import com.example.ryomi.mugenglish.questiongenerator.lessons.The_DEMONYM_flag_is_COLORS;
import com.example.ryomi.mugenglish.questiongenerator.lessons.The_JIS_area_code_of_PLACE_is_NUMBER;

public class LessonFactory {
    public static Lesson parseLesson(LessonData lessonData){
        String lessonID = lessonData.getId();
        Log.d("Lesson Factory", lessonID);
        switch (lessonID) {
            case NAME_went_to_SCHOOL_from_START_to_END.KEY :
                return new NAME_went_to_SCHOOL_from_START_to_END(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_possessive_blood_type_is_BLOODTYPE.KEY :
                return new NAME_possessive_blood_type_is_BLOODTYPE(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_plays_SPORT.KEY :
                return new NAME_plays_SPORT(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_went_to_SCHOOL_So_did_NAME2.KEY :
                return new NAME_went_to_SCHOOL_So_did_NAME2(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_is_DEMONYM.KEY :
                return new NAME_is_DEMONYM(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_is_a_OCCUPATION.KEY :
                return new NAME_is_a_OCCUPATION(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_is_playing_SPORT.KEY :
                return new NAME_is_playing_SPORT(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case CITY_is_in_TERRITORY.KEY :
                return new CITY_is_in_TERRITORY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_is_a_GENDER.KEY :
                return new NAME_is_a_GENDER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_will_be_AGE_in_X_months.KEY :
                return new NAME_will_be_AGE_in_X_months(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY.KEY :
                return new I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case The_JIS_area_code_of_PLACE_is_NUMBER.KEY :
                return new The_JIS_area_code_of_PLACE_is_NUMBER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case BOOK_was_published_by_PUBLISHER.KEY :
                return new BOOK_was_published_by_PUBLISHER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case AUTHOR_wrote_BOOK.KEY :
                return new AUTHOR_wrote_BOOK(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case TAXON_is_named_after_ENTITY.KEY :
                return new TAXON_is_named_after_ENTITY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_participated_in_WAR.KEY :
                return new NAME_participated_in_WAR(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case MOVIE_has_won_AWARD.KEY :
                return new MOVIE_has_won_AWARD(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case MOVIE_won_AWARD_in_YEAR.KEY :
                return new MOVIE_won_AWARD_in_YEAR(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case MOVIE_is_a_GENRE.KEY :
                return new MOVIE_is_a_GENRE(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case PREFECTURE_is_next_to_PREFECTURE2.KEY :
                return new PREFECTURE_is_next_to_PREFECTURE2(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_is_around_HEIGHT_tall.KEY :
                return new NAME_is_around_HEIGHT_tall(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case NAME_was_POSITION_from_START_to_END.KEY :
                return new NAME_was_POSITION_from_START_to_END(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            case The_DEMONYM_flag_is_COLORS.KEY :
                return new The_DEMONYM_flag_is_COLORS(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        lessonData);
            default:
                return null;
        }
    }
}
