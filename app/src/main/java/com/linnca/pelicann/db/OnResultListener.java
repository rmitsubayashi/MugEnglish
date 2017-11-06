package com.linnca.pelicann.db;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
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
    public void onQuestionsQueried(List<String> questionSetIDs, List<WikiDataEntryData> userInterestsSearched){}
    //for each question set that we add
    public void onQuestionSetAdded(String questionSetKey, List<List<String>> questionIDs, String interestLabel, List<String> vocabularyWordKeys){}
    //once we are done adding all the question sets to the database
    public void onQuestionsAdded(){}
    public void onRelatedUserInterestsQueried(List<WikiDataEntryData> relatedUserInterests){}
    public void onRandomQuestionsQueried(List<String> questionSetIDs){}
    //for each question set that we search
    public void onQuestionSetQueried(String questionSetKey, List<List<String>> questionIDs, String interestLabel, List<String> vocabularyWordKeys){}
    //once we are done getting all question sets
    public void onQuestionSetsQueried(){}

    public void onQuestionQueried(QuestionData questionData){}

    public void onLessonInstanceAdded(){}
    public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances){}
    public void onLessonInstanceDetailsQueried(List<InstanceRecord> records){}
    public void onLessonInstanceRemoved(){}

    public void onVocabularyWordQueried(List<VocabularyWord> wordCluster){}
    public void onVocabularyListQueried(List<VocabularyListWord> vocabularyList){}
    public void onVocabularyListItemsRemoved(){}
    public void onVocabularyWordAdded(){}
    public void onLessonVocabularyQueried(List<NewVocabularyWrapper> words){}

    public void onUserInterestsQueried(List<WikiDataEntryData> userInterests){}
    public void onUserInterestsAdded(){}
    public void onUserInterestsRemoved(){}

    public void onRecommendationsQueried(List<WikiDataEntryData> recommendations){}

    public void onClearedLessonsQueried(Set<String> clearedLessonKeys){}
    public void onClearedLessonAdded(boolean firstTimeCleared){}

    public void onInstanceRecordAdded(String generatedRecordKey){}

    public void onReportCardQueried(List<UserProfile_ReportCardDataWrapper> reportCardInfo){}
    public void onReportCardAdded(){}

    public void onFirstAppUsageDateQueried(DateTime date){}
    public void onAppUsageForMonthsQueried(List<AppUsageLog> logs){}

    public void onSportQueried(String wikiDataID, String verb, String object){}
    public void onSportsQueried(){}
}
