package pelicann.linnca.com.corefunctionality.lessonscript;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public class ScriptSpeaker {
    private Translation name;
    //usually the first name.
    // for casual conversation, you would normally say
    // something like 'Hey Yuto' rather than
    // 'Hey Yuto Nagatomo'
    private Translation nickname;
    public static String SPEAKER_NONE = "@noSpeaker";

    public static String IMAGE_NONE = "@none";

    public ScriptSpeaker() {
    }

    public ScriptSpeaker(Translation name, Translation nickname) {
        this.name = name;
        this.nickname = nickname;

    }

    public ScriptSpeaker(Translation name){
        this.name = name;
        this.nickname = name;
    }

    public static ScriptSpeaker getGuestSpeaker(int number){
        switch (number){
            case 1 :
                return new ScriptSpeaker(new Translation("Susie", "スージー"),
                        new Translation("Susie", "スージー"));
            case 2 :
                return new ScriptSpeaker(new Translation("Joe", "ジョー"),
                        new Translation("Joe", "ジョー"));
            case 3 :
                return new ScriptSpeaker(new Translation("Ken", "ケン"),
                        new Translation("Ken", "ケン"));
            default:
                return new ScriptSpeaker(new Translation("Lindsay", "リンジー"),
                        new Translation("Lindsay", "リンジー"));
        }

    }

    public static ScriptSpeaker getNoSpeaker(){
        return new ScriptSpeaker(
                new Translation(SPEAKER_NONE, SPEAKER_NONE),
                new Translation(SPEAKER_NONE, SPEAKER_NONE)
        );

    }

    public Translation getName() {
        return name;
    }

    public void setName(Translation name) {
        this.name = name;
    }

    public Translation getNickname(){ return nickname; }

    public void setNickName(Translation nickname){ this.nickname = nickname; }

    @Override
    public boolean equals(Object object){
        if (object == null)
            return false;

        if (!(object instanceof ScriptSpeaker))
            return false;
        ScriptSpeaker data = (ScriptSpeaker) object;
        //we only check the ID because the label and description might change
        //if a user adds the entity data after it has been modified
        return  (   data.getName().equals(this.name) &&
                    data.getNickname().equals(this.nickname)
        );
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + nickname.hashCode();
        return result;
    }
}
