package pelicann.linnca.com.corefunctionality.db;

public class FirebaseDBHeaders {
    //headers for each child in the firebase database
    public static final String USER_INTERESTS = "userInterests";
    //make sure these match the class variable names in WikiDataEntity
    public static final String USER_INTERESTS_PRONUNCIATION = "pronunciation";
    public static final String USER_INTERESTS_CLASSIFICATION = "classification";
    public static final String USER_INTEREST_RANKINGS = "userInterestRankings";
    public static final String USER_INTEREST_RANKINGS_COUNT = "count";
    public static final String USER_INTEREST_RANKINGS_DATA = "data";
    public static final String SIMILAR_USER_INTERESTS = "similarUserInterests";
    public static final String CLEARED_LESSONS = "clearedLessons";
    public static final String REVIEW_QUESTIONS = "reviewQuestions";
    public static final String LESSON_INSTANCES = "lessonInstances";
    public static final String LESSON_INSTANCE_VOCABULARY = "lessonInstanceVocabulary";
    public static final String QUESTIONS = "questions";
    //public static final String QUESTION_SET_IDS_PER_USER_INTEREST_PER_LESSON = "questionSetIDsPerUserInterestPerLesson";
    public static final String QUESTION_SETS = "questionSets";
    //make sure these headers matches the fields in QuestionSet
    public static final String QUESTION_SET_COUNT = "count";
    public static final String QUESTION_SET_INTEREST_ID = "interestID";
    public static final String INSTANCE_RECORDS = "instanceRecords";
    public static final String UTILS = "utils";
    public static final String UTILS_SPORTS_VERB_MAPPINGS = "sportsVerbMapping";
    public static final String UTILS_SPORT_VERB_MAPPING_OBJECT = "object";
    public static final String UTILS_SPORT_VERB_MAPPING_VERB = "verb";
    public static final String APP_USAGE = "appUsage";
    public static final String VOCABULARY = "vocabulary";
    public static final String VOCABULARY_LIST = "vocabularyList";
    //make sure this matches the field in the class VocabularyListWord
    public static final String VOCABULARY_LIST_WORD_WORD = "word";
    public static final String VOCABULARY_DETAILS = "vocabularyDetails";
    public static final String REPORT_CARD = "reportCard";
    public static final String REPORT_CARD_CORRECT = "correct";
    public static final String REPORT_CARD_TOTAL = "total";
    public static final String DAILY_LESSON_CT = "dailyLessonCount";

    private FirebaseDBHeaders(){}
}
