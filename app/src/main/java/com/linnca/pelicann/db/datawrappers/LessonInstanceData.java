package com.linnca.pelicann.db.datawrappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LessonInstanceData implements Serializable{

    private List<String> questionSetIds;
    //for every question with more than one possible variation,
    //we check this to see which variation we have here
    private List<String> questionIds;
    //basically for displaying the instances to the users
    private List<String> interestLabels; //actual name, not ID
    //for displaying
    private long createdTimeStamp;

    public LessonInstanceData(){
        questionSetIds = new ArrayList<>();
        questionIds = new ArrayList<>();
        interestLabels = new ArrayList<>();
    }

    public LessonInstanceData(List<String> questionSetIds, List<String> questionIds, List<String> interestLabels) {
        this.questionSetIds = new ArrayList<>(questionSetIds);
        this.questionIds = new ArrayList<>(questionIds);
        this.interestLabels = new ArrayList<>(interestLabels);
    }

    public List<String> getQuestionSetIds() {
        return questionSetIds;
    }

    public void setQuestionSetIds(List<String> questionSetIds) {
        this.questionSetIds = questionSetIds;
    }

    //don't add 'get' because that tells FireBase that it's a variable
    public int questionSetCount(){
        return questionSetIds.size();
    }

    //these are synchronized so when we fetch FireBase multiple times
    //to grab questions, we can get the results asynchronously and
    //not have to worry about concurrency issues
    public synchronized void addQuestionSetId(String questionSetId){
        questionSetIds.add(questionSetId);
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    //don't add 'get' because that tells FireBase that it's a variable
    public int questionCount(){ return questionIds.size(); }

    public synchronized void addQuestionIds(List<String> questionIds){
        this.questionIds.addAll(questionIds);
    }

    public String getQuestionIdAt(int index){
        if (index >= questionIds.size()){
            return "";
        } else {
            return questionIds.get(index);
        }
    }

    public List<String> getInterestLabels() {
        return interestLabels;
    }

    public void setInterestLabels(List<String> interestLabels) {
        this.interestLabels = interestLabels;
    }

    public synchronized void addInterestLabel(String interestLabel){
        this.interestLabels.add(interestLabel);
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }
}
