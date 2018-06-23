package pelicann.linnca.com.corefunctionality.userprofile;

public class AppUsageLog {
    private long startTimeStamp;
    private long endTimeStamp;

    //used for Firebase
    public AppUsageLog(){}

    public AppUsageLog(long startTimeStamp, long endTimeStamp) {
        this.startTimeStamp = startTimeStamp;
        this.endTimeStamp = endTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }
}
