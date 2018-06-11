package pelicann.linnca.com.corefunctionality.lessoninstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeedbackPair implements Serializable{
    private List<String> response;
    private String feedback;
    private int responseCheckType;
    //use explicit when you want to check without re-formatting the response.
    //an example would be capitalization feedback.
    //if we format the response (which lower cases the string),
    // we can't determine if the user capitalized correctly or not
    public static final int EXPLICIT = 1;
    //use implicit for cases where it doesn't matter whether to format the string or not
    public static final int IMPLICIT = 2;
    //for when we give feedback no matter the answer
    public static final String ALL = "@all";

    public FeedbackPair(List<String> response, String feedback, int responseCheckType) {
        this.response = response;
        this.feedback = feedback;
        this.responseCheckType = responseCheckType == EXPLICIT || responseCheckType == IMPLICIT ?
                responseCheckType : IMPLICIT;
    }

    public FeedbackPair(String response, String feedback, int responseCheckType) {
        this.response = new ArrayList<>();
        this.response.add(response);
        this.feedback = feedback;
        this.responseCheckType = responseCheckType == EXPLICIT || responseCheckType == IMPLICIT ?
                responseCheckType : IMPLICIT;
    }

    public List<String> getResponse() {
        return response;
    }

    public String getFeedback() {
        return feedback;
    }

    public int getResponseCheckType() {
        return responseCheckType;
    }
}
