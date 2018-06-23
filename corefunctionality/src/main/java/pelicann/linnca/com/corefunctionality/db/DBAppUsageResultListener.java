package pelicann.linnca.com.corefunctionality.db;

import org.joda.time.DateTime;

import java.util.List;

import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

public abstract class DBAppUsageResultListener {
    public abstract void onFirstAppUsageDateQueried(DateTime date);
    public abstract void onAppUsageForMonthsQueried(List<AppUsageLog> logs);
}
