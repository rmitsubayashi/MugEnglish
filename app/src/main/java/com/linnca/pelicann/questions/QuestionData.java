package com.linnca.pelicann.questions;

import com.linnca.pelicann.lessongenerator.FeedbackPair;

import java.io.Serializable;
import java.util.List;

//Used to store questions
public class QuestionData implements Serializable{
    private String id;
    private String lessonId;
    private String topic;
    private Integer questionType;
    private String question;
    private List<String> choices;
    private String answer;
    private List<String> acceptableAnswers;
    private List<FeedbackPair> feedback;

    public QuestionData(){}

    public QuestionData(String id, String lessonId,
                        String topic, Integer questionType,
                        String question, List<String> choices,
                        String answer, List<String> acceptableAnswers,
                        List<FeedbackPair> feedback) {
        this.id = id;
        this.lessonId = lessonId;
        this.topic = topic;
        this.questionType = questionType;
        this.question = question;
        this.choices = choices;
        this.answer = answer;
        this.acceptableAnswers = acceptableAnswers;
        this.feedback = feedback;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getAcceptableAnswers() {return acceptableAnswers;}

    public void setAcceptableAnswers(List<String> acceptableAnswers){this.acceptableAnswers = acceptableAnswers; }

    public List<FeedbackPair> getFeedback() {
        return feedback;
    }

    public void setFeedback(List<FeedbackPair> feedback) {
        this.feedback = feedback;
    }

    //used in question manager when we want to add question data to a list
    // of review questions
    @Override
    public boolean equals(Object object){
        if (object == null)
            return false;

        if (!(object instanceof QuestionData))
            return false;

        QuestionData data = (QuestionData)object;
        //we only check the ID because the label and description might change
        //if a user adds the entity data after it has been modified
        return  (data.getId().equals(this.id));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
}
