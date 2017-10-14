package com.linnca.pelicann.tutorial;

import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.io.Serializable;

public class OnboardingPersonBundle implements Serializable {
    private final WikiDataEntryData data;
    private int gender;
    private String englishName = "";
    private String japaneseName = "";
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    public OnboardingPersonBundle(WikiDataEntryData data, int gender, String englishName, String japaneseName){
        this.data = data;
        this.englishName = englishName;
        this.japaneseName = japaneseName;
        this.gender = gender;
    }

    public WikiDataEntryData getData() {
        return data;
    }

    public int getGender(){return this.gender;}

    public String getEnglishName(){
        return this.englishName;
    }
    public String getJapaneseName(){ return this.japaneseName; }

    static int getGender(String genderWikiDataID){
        if (genderWikiDataID.equals("Q6581097")){
            return GENDER_MALE;
        } else if (genderWikiDataID.equals("Q6581072")){
            return GENDER_FEMALE;
        } else {
            //handle gays/lesbians later
            return GENDER_MALE;
        }
    }
}
