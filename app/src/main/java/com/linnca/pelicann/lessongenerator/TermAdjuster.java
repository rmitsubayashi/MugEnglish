package com.linnca.pelicann.lessongenerator;

//adjust the terminology WikiData uses to make it something more relevant
//ie association football player -> soccer player
public final class TermAdjuster {
    private TermAdjuster(){}
    public static String adjustOccupationEN(String occupation){
        //covers soccer player, manager, etc.
        occupation = occupation.replace("association football", "soccer");

        return occupation;
    }

    public static String adjustSportsEN(String sportsName){
        if (sportsName == null){
            return "";
        }
        //remove the "women's" part (feminist?)
        sportsName = sportsName.replace("women\'s ", "");
        //~sport ie water sport
        //should always be pluralized??
        //not dancesport
        if(sportsName.length() > 5 && (sportsName.substring(sportsName.length()-6)).equals(" sport") )
            sportsName += "s";
        //other exceptions
        switch(sportsName){
            case "association football":
                return "soccer";
            default:
                return sportsName;
        }
    }
}
