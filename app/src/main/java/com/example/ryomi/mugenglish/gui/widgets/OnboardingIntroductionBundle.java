package com.example.ryomi.mugenglish.gui.widgets;

import com.example.ryomi.mugenglish.db.datawrappers.WikiDataEntryData;

import java.io.Serializable;

public class OnboardingIntroductionBundle implements Serializable {
    private WikiDataEntryData data;
    private boolean isMale = false;
    private boolean isFemale = false;
    private String englishName = "";

    public OnboardingIntroductionBundle(WikiDataEntryData data, String gender, String englishName){
        this.data = data;
        this.englishName = englishName;
        if (gender.equals("male")){
            isMale = true;
        }
        else if (gender.equals("female")){
            isFemale = true;
        }
    }

    public WikiDataEntryData getData() {
        return data;
    }

    public boolean isMale(){
        return isMale;
    }

    public boolean isFemale(){
        return isFemale;
    }

    public String getEnglishName(){
        return this.englishName;
    }
}
