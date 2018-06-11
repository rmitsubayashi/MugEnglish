package pelicann.linnca.com.corefunctionality.db;

import org.joda.time.DateTime;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;

public abstract class OnDBResultListener {
    public void onNoConnection(){}
    public void onSlowConnection(){}

    public void onEntityPropertyDataSearched(List<EntityPropertyData> found, List<WikiDataEntity> searched){}
    //for each
    public void onEntityPropertyDataAdded(EntityPropertyData added){}
    //all
    public void onAllEntityPropertyDataAdded(){}
    public void onRandomEntityPropertyDataQueried(List<EntityPropertyData> result){}

    public void onLessonInstanceAdded(){}
    public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances){}

    public void onUserInterestsQueried(List<WikiDataEntity> userInterests){}
    public void onUserInterestsAdded(){}
    public void onUserInterestsRemoved(){}

    public void onSimilarUserInterestsQueried(List<WikiDataEntity> userInterests){}

    public void onUserInterestRankingsQueried(List<WikiDataEntity> userInterests){}

    public void onInstanceRecordAdded(String generatedRecordKey){}

    public void onReportCardAdded(){}

    public void onFirstAppUsageDateQueried(DateTime date){}
    public void onAppUsageForMonthsQueried(List<AppUsageLog> logs){}

    public void onDailyLessonAdded(int newCt){}

    public void onSportQueried(String wikiDataID, String verb, String object){}
    public void onSportsQueried(){}
}
