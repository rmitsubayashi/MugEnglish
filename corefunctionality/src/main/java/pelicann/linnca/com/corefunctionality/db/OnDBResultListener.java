package pelicann.linnca.com.corefunctionality.db;

import pelicann.linnca.com.corefunctionality.lessondetails.LessonInstanceData;
import pelicann.linnca.com.corefunctionality.questions.InstanceRecord;
import pelicann.linnca.com.corefunctionality.questions.QuestionData;
import pelicann.linnca.com.corefunctionality.questions.QuestionSet;
import pelicann.linnca.com.corefunctionality.results.ResultsVocabularyWord;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;
import pelicann.linnca.com.corefunctionality.userprofile.AppUsageLog;
import pelicann.linnca.com.corefunctionality.userprofile.UserProfile_ReportCardDataWrapper;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyListWord;
import pelicann.linnca.com.corefunctionality.vocabulary.VocabularyWord;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public abstract class OnDBResultListener {
    public void onNoConnection(){}
    public void onSlowConnection(){}

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

    public void onSimilarUserInterestsQueried(List<WikiDataEntity> userInterests){}

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

    public void onDailyLessonAdded(int newCt){}

    public void onSportQueried(String wikiDataID, String verb, String object){}
    public void onSportsQueried(){}
}
