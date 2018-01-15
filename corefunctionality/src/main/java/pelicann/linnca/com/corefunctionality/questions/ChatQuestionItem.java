package pelicann.linnca.com.corefunctionality.questions;

public class ChatQuestionItem {
    public static String USER_INPUT = "@userInput@";
    private boolean isUser;
    private String text;

    public ChatQuestionItem(boolean isUser, String text){
        this.isUser = isUser;
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
