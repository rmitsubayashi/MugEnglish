package com.example.ryomi.myenglish.questiongenerator;

//適切なテーマクラスを作る

import com.example.ryomi.myenglish.connectors.WikiBaseEndpointConnector;
import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.db.database2classmappings.ThemeMappings;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_plays_SPORT;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_possessive_blood_type_is_BLOODTYPE;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_went_to_SCHOOL_So_did_NAME2;
import com.example.ryomi.myenglish.questiongenerator.themes.NAME_went_to_SCHOOL_from_START_to_END;

public class ThemeFactory {
    public static Theme createTheme(String themeID){
        if (themeID == ThemeMappings.NAME_went_to_SCHOOL_from_START_to_END){
            return new NAME_went_to_SCHOOL_from_START_to_END(new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE));
        } else if (themeID == ThemeMappings.NAME_possessive_blood_type_is_BLOODTYPE){
            return new NAME_possessive_blood_type_is_BLOODTYPE(new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE));
        } else if (themeID == ThemeMappings.NAME_plays_SPORT){
            return new NAME_plays_SPORT(new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE));
        } else if (themeID == ThemeMappings.NAME_went_to_SCHOOL_So_did_NAME2){
            return new NAME_went_to_SCHOOL_So_did_NAME2(new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE));
        } else {
            return null;
        }
    }
}
