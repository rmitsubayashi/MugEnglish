package pelicann.linnca.com.corefunctionality.lessondetails;

import pelicann.linnca.com.corefunctionality.questions.QuestionSet;

import java.io.Serializable;
import java.util.List;

public class LessonInstanceDataQuestionSet implements Serializable{
    private String id;
    private boolean partOfPopularityRating;
    //for displaying the instance information to the users
    private String interestLabel;
    private List<String> questionIDs;

    public LessonInstanceDataQuestionSet() {
    }

    public LessonInstanceDataQuestionSet(String id, boolean partOfPopularityRating, String interestLabel, List<String> questionIDs) {
        this.id = id;
        this.partOfPopularityRating = partOfPopularityRating;
        this.interestLabel = interestLabel;
        this.questionIDs = questionIDs;
    }

    public LessonInstanceDataQuestionSet(QuestionSet questionSet, boolean partOfPopularityRating){
        this.id = questionSet.getKey();
        this.partOfPopularityRating = partOfPopularityRating;
        this.interestLabel = questionSet.getInterestLabel();
        this.questionIDs = questionSet.pickQuestions();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPartOfPopularityRating() {
        return partOfPopularityRating;
    }

    public void setPartOfPopularityRating(boolean partOfPopularityRating) {
        this.partOfPopularityRating = partOfPopularityRating;
    }

    public String getInterestLabel() {
        return interestLabel;
    }

    public void setInterestLabel(String interestLabel) {
        this.interestLabel = interestLabel;
    }

    public List<String> getQuestionIDs() {
        return questionIDs;
    }

    public void setQuestionIDs(List<String> questionIDs) {
        this.questionIDs = questionIDs;
    }
}
