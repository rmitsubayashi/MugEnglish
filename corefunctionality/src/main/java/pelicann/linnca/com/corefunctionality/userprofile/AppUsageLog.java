package pelicann.linnca.com.corefunctionality.userprofile;

public class AppUsageLog {
    private long startTimeStamp;
    private long endTimeStamp;

    public AppUsageLog() {
    }

    public AppUsageLog(long startTimeStamp, long endTimeStamp) {
        this.startTimeStamp = startTimeStamp;
        this.endTimeStamp = endTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public static String formatKey(int month, int year){
        String key = year + "-";
        if (month < 10){
            key += "0";
        }
        key += month;
        return key;
    }
}
