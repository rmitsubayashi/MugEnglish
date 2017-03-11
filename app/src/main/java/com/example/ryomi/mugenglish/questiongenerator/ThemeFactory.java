package com.example.ryomi.mugenglish.questiongenerator;

//create an instance of the required class

import com.example.ryomi.mugenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.mugenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.mugenglish.db.database2classmappings.ThemeMappings;
import com.example.ryomi.mugenglish.db.datawrappers.ThemeData;
import com.example.ryomi.mugenglish.questiongenerator.themes.AUTHOR_wrote_BOOK;
import com.example.ryomi.mugenglish.questiongenerator.themes.BOOK_was_published_by_PUBLISHER;
import com.example.ryomi.mugenglish.questiongenerator.themes.CITY_is_in_TERRITORY;
import com.example.ryomi.mugenglish.questiongenerator.themes.I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY;
import com.example.ryomi.mugenglish.questiongenerator.themes.MOVIE_has_won_AWARD;
import com.example.ryomi.mugenglish.questiongenerator.themes.MOVIE_is_a_GENRE;
import com.example.ryomi.mugenglish.questiongenerator.themes.MOVIE_won_AWARD_in_YEAR;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_is_DEMONYM;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_is_a_GENDER;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_is_a_OCCUPATION;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_is_playing_SPORT;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_participated_in_WAR;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_plays_SPORT;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_possessive_blood_type_is_BLOODTYPE;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_went_to_SCHOOL_So_did_NAME2;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_went_to_SCHOOL_from_START_to_END;
import com.example.ryomi.mugenglish.questiongenerator.themes.NAME_will_be_AGE_in_X_months;
import com.example.ryomi.mugenglish.questiongenerator.themes.PREFECTURE_is_next_to_PREFECTURE2;
import com.example.ryomi.mugenglish.questiongenerator.themes.TAXON_is_named_after_ENTITY;
import com.example.ryomi.mugenglish.questiongenerator.themes.The_JIS_area_code_of_PLACE_is_NUMBER;

public class ThemeFactory {
    public static Theme createTheme(ThemeData themeData){
        String themeID = themeData.getId();
        switch (themeID) {
            case ThemeMappings.NAME_went_to_SCHOOL_from_START_to_END :
                return new NAME_went_to_SCHOOL_from_START_to_END(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_possessive_blood_type_is_BLOODTYPE :
                return new NAME_possessive_blood_type_is_BLOODTYPE(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_plays_SPORT :
                return new NAME_plays_SPORT(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_went_to_SCHOOL_So_did_NAME2 :
                return new NAME_went_to_SCHOOL_So_did_NAME2(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_is_DEMONYM :
                return new NAME_is_DEMONYM(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_is_a_OCCUPATION :
                return new NAME_is_a_OCCUPATION(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_is_playing_SPORT :
                return new NAME_is_playing_SPORT(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.CITY_is_in_TERRITORY :
                return new CITY_is_in_TERRITORY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_is_a_GENDER :
                return new NAME_is_a_GENDER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_will_be_AGE_in_X_months :
                return new NAME_will_be_AGE_in_X_months(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.I_should_call_EMERGENCY_PHONE_NUMBER :
                return new I_should_call_EMERGENCY_PHONE_NUMBER_in_an_emergency_in_COUNTRY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.The_JIS_area_code_of_PLACE_is_NUMBER :
                return new The_JIS_area_code_of_PLACE_is_NUMBER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.BOOK_was_published_by_PUBLISHER :
                return new BOOK_was_published_by_PUBLISHER(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.AUTHOR_wrote_BOOK :
                return new AUTHOR_wrote_BOOK(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.TAXON_is_named_after_ENTITY :
                return new TAXON_is_named_after_ENTITY(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.NAME_participated_in_WAR :
                return new NAME_participated_in_WAR(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.MOVIE_has_won_AWARD :
                return new MOVIE_has_won_AWARD(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.MOVIE_won_AWARD_in_YEAR :
                return new MOVIE_won_AWARD_in_YEAR(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.MOVIE_is_a_GENRE :
                return new MOVIE_is_a_GENRE(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            case ThemeMappings.PREFECTURE_is_next_to_PREFECTUE2 :
                return new PREFECTURE_is_next_to_PREFECTURE2(
                        new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
                        themeData);
            default:
                return null;
        }
    }
}
