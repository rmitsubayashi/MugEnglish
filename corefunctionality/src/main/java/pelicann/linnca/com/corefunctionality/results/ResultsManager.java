package pelicann.linnca.com.corefunctionality.results;

import org.joda.time.DateTime;

import java.util.List;

import pelicann.linnca.com.corefunctionality.db.DBDailyLessonResultListener;
import pelicann.linnca.com.corefunctionality.db.DBInstanceRecordResultListener;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.lessonquestions.InstanceAttemptRecord;
import pelicann.linnca.com.corefunctionality.lessonquestions.QuestionAttempt;

//this manages the results displayed to the user
public class ResultsManager {
    private final Database db;
    private final InstanceAttemptRecord instanceAttemptRecord;
    private final ResultsManagerListener resultsManagerListener;

    public interface ResultsManagerListener {
        void onIncrementDailyLessonCt(int oldCt, int newCt);
    }

    public ResultsManager(InstanceAttemptRecord instanceAttemptRecord, Database db, ResultsManagerListener listener){
        this.instanceAttemptRecord = instanceAttemptRecord;
        this.resultsManagerListener = listener;
        this.db = db;
    }

    public void saveInstanceRecord(){
        //save the whole instance record
        final DBInstanceRecordResultListener instanceRecordResultListener = new DBInstanceRecordResultListener() {
            @Override
            public void onInstanceRecordAdded(String generatedRecordKey) {
            }
        };
        db.addInstanceAttemptRecord(instanceAttemptRecord, instanceRecordResultListener);
    }

    public static int[] calculateCorrectCount(List<QuestionAttempt> attempts){
        String tempQuestionID = "";
        int correctCt = 0;
        int totalCt = 0;
        for (QuestionAttempt attempt : attempts){
            String questionID = attempt.getQuestionID();
            //there can only be one correct answer per question.
            // (there can be multiple incorrect answers)
            if (attempt.getCorrect())
                correctCt++;
            if (!tempQuestionID.equals(questionID)){
                totalCt++;
                tempQuestionID = questionID;
            }
        }

        return new int[]{correctCt, totalCt};
    }

    public void incrementDailyLessonCt(){
        DBDailyLessonResultListener dailyLessonResultListener = new DBDailyLessonResultListener() {
            @Override
            public void onDailyLessonAdded(int newCt) {
                int oldCt = newCt - 1;
                resultsManagerListener.onIncrementDailyLessonCt(oldCt, newCt);
            }
        };
        db.incrementDailyLesson(formatDailyLessonCtDate(), dailyLessonResultListener);
    }

    private String formatDailyLessonCtDate(){
        int month = DateTime.now().monthOfYear().get();
        int day = DateTime.now().dayOfMonth().get();
        return Integer.toString(month) + "-" + Integer.toString(day);
    }
}
