package pelicann.linnca.com.corefunctionality.userprofile;

public class AppUsageLog {
    private long startTimeStamp;
    private long endTimeStamp;

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

    public static String formatKey(int month, int year){
        String key = year + "-";
        if (month < 10){
            key += "0";
        }
        key += month;
        return key;
    }
}
