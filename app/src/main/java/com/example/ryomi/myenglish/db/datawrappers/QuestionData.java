package com.example.ryomi.myenglish.db.datawrappers;

import java.util.List;

public class QuestionData {
    private String id;
    private String themeId;
    private String topicId;
    private Integer questionType;
    private String question;
    private List<String> choices;
    private String answer;
    private List<String> vocabulary;

    public QuestionData(){}

    public QuestionData(String id, String themeId,
                        String topicId, Integer questionType,
                        String question, List<String> choices,
                        String answer, List<String> vocabulary) {
        this.id = id;
        this.themeId = themeId;
        this.topicId = topicId;
        this.questionType = questionType;
        this.question = question;
        this.choices = choices;
        this.answer = answer;
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

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
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
}
