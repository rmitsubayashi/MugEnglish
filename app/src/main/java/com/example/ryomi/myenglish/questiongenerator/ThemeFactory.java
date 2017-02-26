package com.example.ryomi.myenglish.questiongenerator;

//create an instance of the required class

import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.database2classmappings.ThemeMappings;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.questiongenerator.themes.CITY_is_in_TERRITORY;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_is_DEMONYM;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_is_a_GENDER;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_is_a_OCCUPATION;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_is_playing_SPORT;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_plays_SPORT;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_possessive_blood_type_is_BLOODTYPE;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_went_to_SCHOOL_So_did_NAME2;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_went_to_SCHOOL_from_START_to_END;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_will_be_AGE_in_X_months;

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
            default:
                return null;
        }
    }
}
