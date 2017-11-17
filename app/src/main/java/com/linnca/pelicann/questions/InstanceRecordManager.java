package com.linnca.pelicann.questions;

import java.util.ArrayList;
import java.util.List;

class InstanceRecordManager {
    //handles writing into the instance record while the user is answering questions
    private InstanceRecord instanceRecord;
    private long questionAttemptStartTimestamp;
    private long questionAttemptEndTimestamp;

    InstanceRecordManager(String instanceID, String lessonID){
        instanceRecord = new InstanceRecord();
        instanceRecord.setCompleted(false);
        instanceRecord.setInstanceId(instanceID);
        instanceRecord.setLessonId(lessonID);
        instanceRecord.setAttempts(new ArrayList<QuestionAttempt>());
    }

    InstanceRecordManager(InstanceRecord instanceRecord){
        this.instanceRecord = instanceRecord;
    }

    InstanceRecord getInstanceRecord(){
        return instanceRecord;
    }

    void addQuestionAttempt(String questionID, String response, boolean correct){
        List<QuestionAttempt> attempts = instanceRecord.getAttempts();
        int attemptNumber;
        //first attempt at first question
        if (attempts.size() == 0) {
            //prevents array out of bounds exception
            attemptNumber = 1;
        } else {
            QuestionAttempt lastAttempt = attempts.get(attempts.size() - 1);
            if (questionID.equals(lastAttempt.getQuestionID())){
                //same question so this is an attempt at the same question
                attemptNumber = lastAttempt.getAttemptNumber() + 1;
            } else {
                //new question so first attempt number
                attemptNumber = 1;
            }
        }
        setQuestionAttemptEndTimestamp();
        QuestionAttempt attempt = new QuestionAttempt(
                attemptNumber, questionID, response, correct,
                questionAttemptStartTimestamp, questionAttemptEndTimestamp);

        attempts.add(attempt);
    }

    void setQuestionAttemptStartTimestamp(){
        questionAttemptStartTimestamp = System.currentTimeMillis();
    }

    void setQuestionAttemptEndTimestamp(){
        questionAttemptEndTimestamp = System.currentTimeMillis();
    }

    void markInstanceCompleted(){
        instanceRecord.setCompleted(true);
    }
}
