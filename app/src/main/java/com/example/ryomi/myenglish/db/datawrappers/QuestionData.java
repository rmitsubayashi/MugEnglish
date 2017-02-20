package com.example.ryomi.myenglish.db.datawrappers;

import java.util.List;

public class QuestionData {
    private String id;
    private String themeId;
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
        this.themeId = themeId;
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

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
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
