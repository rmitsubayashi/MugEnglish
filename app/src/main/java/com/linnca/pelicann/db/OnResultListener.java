package com.linnca.pelicann.db;

import com.linnca.pelicann.results.NewVocabularyWrapper;
import com.linnca.pelicann.userinterests.WikiDataEntryData;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.userprofile.UserProfile_ReportCardDataWrapper;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public abstract class OnResultListener {
    public void onVocabularyWordQueried(List<VocabularyWord> wordCluster){}
    public void onVocabularyListQueried(List<VocabularyListWord> vocabularyList){}
    public void onVocabularyListItemsRemoved(){}
    public void onVocabularyWordAdded(){}
    public void onLessonVocabularyQueried(List<NewVocabularyWrapper> words){}

    public void onUserInterestsQueried(List<WikiDataEntryData> userInterests){}
    public void onUserInterestsAdded(){}
    public void onUserInterestsRemoved(){}

    public void onClearedLessonsQueried(Set<String> clearedLessonKeys){}
    public void onClearedLessonAdded(boolean firstTimeCleared){}

    public void onInstanceRecordAdded(String generatedRecordKey){}

    public void onReportCardQueried(List<UserProfile_ReportCardDataWrapper> reportCardInfo){}
    public void onReportCardAdded(){}

    public void onFirstAppUsageDateQueried(DateTime date){}
    public void onAppUsageForMonthsQueried(List<AppUsageLog> logs){}
}
