package com.linnca.pelicann.onboarding;

import com.linnca.pelicann.userinterests.WikiDataEntryData;

import java.io.Serializable;

class OnboardingIntroductionBundle implements Serializable {
    private final WikiDataEntryData data;
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
