package com.linnca.pelicann.db.datawrappers;

import java.io.Serializable;
import java.util.List;

public class QuestionData implements Serializable{
    private String id;
    private String lessonId;
    private String topic;
    private Integer questionType;
    private String question;
    private List<String> choices;
    private String answer;
    private List<String> acceptableAnswers;
    private List<String> vocabulary;

    public QuestionData(){}

    public QuestionData(String id, String themeId,
                        String topic, Integer questionType,
                        String question, List<String> choices,
                        String answer, List<String> acceptableAnswers,
                        List<String> vocabulary) {
        this.id = id;
        this.lessonId = themeId;
        this.topic = topic;
        this.questionType = questionType;
        this.question = question;
        this.choices = choices;
        this.answer = answer;
        this.acceptableAnswers = acceptableAnswers;
        this.vocabulary = vocabulary;
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

    public List<String> getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(List<String> vocabulary) {
        this.vocabulary = vocabulary;
    }

    public List<String> getAcceptableAnswers() {return acceptableAnswers;}

    public void setAcceptableAnswers(List<String> acceptableAnswers){this.acceptableAnswers = acceptableAnswers; }
}
