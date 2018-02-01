package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.io.Serializable;

public class QuestionAttempt implements Serializable {
    private int attemptNumber;
    private String questionID;
    private String response;
    private Boolean correct;
    private long startTime;
    private long endTime;

    public QuestionAttempt() {
    }

    public QuestionAttempt(int attemptNumber, String questionID, String response, Boolean correct, long startTime, long endTime) {
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

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
