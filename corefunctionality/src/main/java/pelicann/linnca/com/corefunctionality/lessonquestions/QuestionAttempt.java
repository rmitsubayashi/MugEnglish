package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.io.Serializable;

public class QuestionAttempt implements Serializable {
    private int attemptNumber;
    private String questionID;
    private String response;
    private Boolean correct;
    private long startTime;
    private long endTime;

    QuestionAttempt(int attemptNumber, String questionID, String response, Boolean correct, long startTime, long endTime) {
        this.attemptNumber = attemptNumber;
        this.questionID = questionID;
        this.response = response;
        this.correct = correct;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public String getQuestionID() {
        return questionID;
    }

    public Boolean getCorrect() {
        return correct;
    }
}
