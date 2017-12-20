package com.linnca.pelicann.lessondetails;

import com.linnca.pelicann.questions.QuestionSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//used in lessonDetails to display each instance and start the questions
public class LessonInstanceData implements Serializable{
    private String id;
    private String lessonKey;
    //for displaying to the user
    private long createdTimeStamp;
    //generic questions to put before the main question sets
    private List<String> preGenericQuestionIds;
    //generic questions to put after the main question sets
    private List<String> postGenericQuestionIds;
    private List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
    private int questionOrder;
    public static final int QUESTION_ORDER_ORDER_BY_QUESTION = 1;
    public static final int QUESTION_ORDER_ORDER_BY_SET = 2;

    public LessonInstanceData(){
    }

    public LessonInstanceData(String id, String lessonKey, long createdTimeStamp,
                              List<String> preGenericQuestionIds, List<String> postGenericQuestionIds,
                              List<LessonInstanceDataQuestionSet> questionSets, int questionOrder) {
        this.id = id;
        this.lessonKey = lessonKey;
        this.createdTimeStamp = createdTimeStamp;
        this.preGenericQuestionIds = preGenericQuestionIds;
        this.postGenericQuestionIds = postGenericQuestionIds;
        this.questionSets = new ArrayList<>(questionSets);
        this.questionOrder =questionOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonKey() {
        return lessonKey;
    }

    public void setLessonKey(String lessonKey) {
        this.lessonKey = lessonKey;
    }

    public List<LessonInstanceDataQuestionSet> getQuestionSets() {
        return questionSets;
    }

    public void setQuestionSets(List<LessonInstanceDataQuestionSet> questionSets) {
        this.questionSets = questionSets;
    }

    public List<String> getPreGenericQuestionIds() {
        return preGenericQuestionIds;
    }

    public void setPreGenericQuestionIds(List<String> preGenericQuestionIds) {
        this.preGenericQuestionIds = preGenericQuestionIds;
    }

    public List<String> getPostGenericQuestionIds() {
        return postGenericQuestionIds;
    }

    public void setPostGenericQuestionIds(List<String> postGenericQuestionIds) {
        this.postGenericQuestionIds = postGenericQuestionIds;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    public List<String> questionSetIds() {
        List<String> questionSetIds = new ArrayList<>(questionSets.size());
        for (LessonInstanceDataQuestionSet set : questionSets){
            questionSetIds.add(set.getId());
        }
        return questionSetIds;
    }

    //don't add 'get' because that tells FireBase that it's a variable
    public int questionSetCount(){
        return questionSets.size();
    }

    public List<String> allQuestionIds() {
        List<String> questionIDs = new ArrayList<>();
        if (preGenericQuestionIds != null)
            questionIDs.addAll(preGenericQuestionIds);
        if (questionOrder == QUESTION_ORDER_ORDER_BY_SET) {
            for (LessonInstanceDataQuestionSet set : questionSets) {
                questionIDs.addAll(set.getQuestionIDs());
            }
        } else if (questionOrder == QUESTION_ORDER_ORDER_BY_QUESTION) {
            int questionCtPerSet = questionSets.get(0).getQuestionIDs().size();
            for (int i=0; i<questionCtPerSet; i++){
                for (LessonInstanceDataQuestionSet set : questionSets){
                    questionIDs.add(set.getQuestionIDs().get(i));
                }
            }
        }
        if (postGenericQuestionIds != null)
            questionIDs.addAll(postGenericQuestionIds);

        return questionIDs;
    }

    //don't add 'get' because that tells FireBase that it's a variable
    public int questionCount(){
        int questionCt = 0;
        if (preGenericQuestionIds != null)
            questionCt += preGenericQuestionIds.size();

        for (LessonInstanceDataQuestionSet set : questionSets) {
            questionCt += set.getQuestionIDs().size();
        }

        if (postGenericQuestionIds != null)
            questionCt += postGenericQuestionIds.size();

        return questionCt;
    }

    public String questionIdAt(int index){
        List<String> questionIDs = allQuestionIds();
        if (index >= questionIDs.size()){
            return "";
        } else {
            return questionIDs.get(index);
        }
    }

    public Set<String> uniqueInterestLabels(){
        Set<String> interestLabels = new HashSet<>(questionSetCount());
        for (LessonInstanceDataQuestionSet set : questionSets){
            interestLabels.add(set.getInterestLabel());
        }
        return interestLabels;
    }

    //this is synchronized so when we fetch FireBase multiple times
    //to grab questions, we can get the results asynchronously and
    //not have to worry about concurrency issues
    public synchronized void addQuestionSet(QuestionSet questionSet, boolean partOfPopularityRating){
        LessonInstanceDataQuestionSet set = new LessonInstanceDataQuestionSet(questionSet, partOfPopularityRating);
        questionSets.add(set);
    }

    @Override
    public boolean equals(Object object){

        if (object == null)
            return false;

        if (!(object instanceof LessonInstanceData))
            return false;
        LessonInstanceData data = (LessonInstanceData) object;
        return  (data.getId().equals(this.id));
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
}
