package pelicann.linnca.com.corefunctionality.lessonquestions;

import java.util.ArrayList;
import java.util.List;

class InstanceAttemptRecordManager {
    //handles writing into the instance record while the user is answering questions
    private final InstanceAttemptRecord instanceAttemptRecord;
    private long questionAttemptStartTimestamp;
    private long questionAttemptEndTimestamp;

    InstanceAttemptRecordManager(String instanceID, String lessonID){
        instanceAttemptRecord = new InstanceAttemptRecord();
        instanceAttemptRecord.setInstanceId(instanceID);
        instanceAttemptRecord.setLessonId(lessonID);
        instanceAttemptRecord.setAttempts(new ArrayList<QuestionAttempt>());
    }

    InstanceAttemptRecord getInstanceAttemptRecord(){
        return instanceAttemptRecord;
    }

    void addQuestionAttempt(String questionID, String response, boolean correct){
        List<QuestionAttempt> attempts = instanceAttemptRecord.getAttempts();
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

    private void setQuestionAttemptEndTimestamp(){
        questionAttemptEndTimestamp = System.currentTimeMillis();
    }
}
