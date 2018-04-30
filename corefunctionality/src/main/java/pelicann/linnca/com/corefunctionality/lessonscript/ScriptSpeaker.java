package pelicann.linnca.com.corefunctionality.lessonscript;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;

public class ScriptSpeaker {
    private Translation name;
    //usually the first name.
    // for casual conversation, you would normally say
    // something like 'Hey Yuto' rather than
    // 'Hey Yuto Nagatomo'
    private Translation nickname;

    public static String SPEAKER_USER = "@user";
    public static String SPEAKER_NONE = "@noSpeaker";

    public static String IMAGE_NONE = "@none";

    public ScriptSpeaker() {
    }

    public ScriptSpeaker(Translation name, Translation nickname) {
        this.name = name;
        this.nickname = nickname;

    }

    public static ScriptSpeaker getGuestSpeaker(int number){
        return new ScriptSpeaker(
                new Translation(SPEAKER_USER + number, SPEAKER_USER + number),
                new Translation(SPEAKER_USER + number, SPEAKER_USER + number)
        );

    }

    public static boolean isGuestSpeaker(String speaker){
        return speaker.contains(SPEAKER_USER);
    }

    public static int getGuestSpeakerNumber(String speaker){
        String numString = speaker.replace(SPEAKER_USER, "");
        return Integer.parseInt(numString);
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
