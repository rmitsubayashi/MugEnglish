package pelicann.linnca.com.corefunctionality.lessonscript;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public class ScriptSpeaker {
    private Translation name;
    public static final String SPEAKER_NONE = "@noSpeaker";

    public ScriptSpeaker(Translation name){
        this.name = name;
    }

    public static ScriptSpeaker getGuestSpeaker(int number){
        switch (number){
            case 1 :
                return new ScriptSpeaker(new Translation("Susie", "スージー"));
            case 2 :
                return new ScriptSpeaker(new Translation("Joe", "ジョー"));
            case 3 :
                return new ScriptSpeaker(new Translation("Ken", "ケン"));
            default:
                return new ScriptSpeaker(new Translation("Lindsay", "リンジー"));
        }

    }

    public static ScriptSpeaker getNoSpeaker(){
        return new ScriptSpeaker(
                new Translation(SPEAKER_NONE, SPEAKER_NONE)
        );

    }

    public Translation getName() {
        return name;
    }

    public void setName(Translation name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object){
        if (object == null)
            return false;

        if (!(object instanceof ScriptSpeaker))
            return false;
        ScriptSpeaker data = (ScriptSpeaker) object;
        //we only check the ID because the label and description might change
        //if a user adds the entity data after it has been modified
        return  (   data.getName().equals(this.name)
        );
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + name.hashCode();
        return result;
    }
}
