package com.linnca.pelicann.db;

import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionSet;
import com.linnca.pelicann.results.ResultsVocabularyWord;
import com.linnca.pelicann.userinterests.WikiDataEntity;
import com.linnca.pelicann.userprofile.AppUsageLog;
import com.linnca.pelicann.userprofile.UserProfile_ReportCardDataWrapper;
import com.linnca.pelicann.vocabulary.VocabularyListWord;
import com.linnca.pelicann.vocabulary.VocabularyWord;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public abstract class OnDBResultListener {
    public void onQuestionsQueried(List<QuestionSet> questionSets, List<WikiDataEntity> userInterestsSearched){}
    //for each question set that we add
    public void onQuestionSetAdded(QuestionSet questionSet){}
    //once we are done adding all the question sets to the database
    public void onQuestionsAdded(){}
    //once we are done getting all question sets
    public void onQuestionSetsQueried(List<QuestionSet> questionSets){}
    public void onQuestionSetCountChanged(){}
    public void onPopularQuestionSetsQueried(List<QuestionSet> questionSets){}

    public void onQuestionQueried(QuestionData questionData){}

    public void onLessonInstanceAdded(){}
    public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances){}
    public void onLessonInstanceDetailsQueried(List<InstanceRecord> records){}
    public void onLessonInstanceRemoved(){}

    public void onVocabularyWordQueried(List<VocabularyWord> wordCluster){}
    public void onVocabularyListQueried(List<VocabularyListWord> vocabularyList){}
    public void onVocabularyListItemsRemoved(){}
    public void onVocabularyWordAdded(){}
    public void onLessonVocabularyQueried(List<ResultsVocabularyWord> words){}

    public void onUserInterestsQueried(List<WikiDataEntity> userInterests){}
    public void onUserInterestsAdded(){}
    public void onUserInterestsRemoved(){}

    public void onUserInterestRankingsQueried(List<WikiDataEntity> userInterests){}

    public void onClearedLessonsQueried(Set<String> clearedLessonKeys){}
    public void onClearedLessonAdded(boolean firstTimeCleared){}

    public void onReviewQuestionsAdded(){}
    public void onReviewQuestionsRemoved(){}
    public void onReviewQuestionsQueried(List<String> questionKeys){}

    public void onInstanceRecordAdded(String generatedRecordKey){}

    public void onReportCardQueried(List<UserProfile_ReportCardDataWrapper> reportCardInfo){}
    public void onReportCardAdded(){}

    public void onFirstAppUsageDateQueried(DateTime date){}
    public void onAppUsageForMonthsQueried(List<AppUsageLog> logs){}

    public void onSportQueried(String wikiDataID, String verb, String object){}
    public void onSportsQueried(){}
}
