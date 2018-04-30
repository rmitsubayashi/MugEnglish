package pelicann.linnca.com.corefunctionality.lessoninstance;

public class Translation {
    private String english;
    private String japanese;
    //if we ever want to reference this in another lesson
    private String wikidataID;

    public static final int MALE = 1;
    public static final int FEMALE = 2;

    public static final String NONE = "@none";

    public Translation(){}

    public Translation(String english, String japanese) {
        this.english = english;
        this.japanese = japanese;
    }

    public Translation(String wikidataID, String english, String japanese) {
        this.wikidataID = wikidataID;
        this.english = english;
        this.japanese = japanese;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getJapanese() {
        return japanese;
    }

    public void setJapanese(String japanese) {
        this.japanese = japanese;
    }

    public String getWikidataID() {
        return wikidataID;
    }

    public void setWikidataID(String wikidataID) {
        this.wikidataID = wikidataID;
    }

    //util for gender (we will be using a lot of gender)
    public void setGenderPronoun(int gender){
        switch (gender){
            case MALE :
                this.english = "he";
                this.japanese = "彼";
                break;
            case FEMALE :
                this.english = "she";
                this.japanese = "彼女";
                break;
            default :
                this.english = "he";
                this.japanese = "彼";
        }
    }

    //some of the exact translations may change over time.
    //if either the japanese or english translations are the same,
    // it's more likely that the translations belong to the same entity
    public boolean mostLikelyEquals(Translation translation){
        return translation.getJapanese().equals(japanese) ||
                translation.getEnglish().equals(english);
    }

    @Override
    public boolean equals(Object object){

        if (object == null)
            return false;

        if (!(object instanceof Translation))
            return false;
        Translation data = (Translation) object;
        //we only check the ID because the label and description might change
        //if a user adds the entity data after it has been modified
        return  (   data.getEnglish().equals(this.english) &&
                    data.getJapanese().equals(this.japanese));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + english.hashCode();
        result = 31 * result + japanese.hashCode();
        return result;
    }
}
