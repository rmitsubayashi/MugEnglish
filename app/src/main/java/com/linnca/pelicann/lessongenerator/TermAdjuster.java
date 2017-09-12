package com.linnca.pelicann.lessongenerator;

//adjust the terminology WikiData uses to make it something more relevant
//ie association football player -> soccer player
public class TermAdjuster {
    private TermAdjuster(){}
    public static String adjustOccupationEN(String occupation){
        if (occupation.equals("association football player")){
            occupation = "soccer player";
        }

        return occupation;
    }
}
