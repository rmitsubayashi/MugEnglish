package pelicann.linnca.com.corefunctionality.lessonquestions;

public class ChatQuestionItem {
    public static final String USER_INPUT = "@userInput@";
    private boolean isUser;
    private String text;

    public ChatQuestionItem(boolean isUser, String text){
        this.isUser = isUser;
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getText() {
        return text;
    }
}
