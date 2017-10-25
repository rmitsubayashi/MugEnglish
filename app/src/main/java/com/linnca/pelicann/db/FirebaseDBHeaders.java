package com.linnca.pelicann.db;

public class FirebaseDBHeaders {
    //top most headers
    public static final String USER_INTERESTS = "userInterests";
    public static final String CLEARED_LESSONS = "clearedLessons";
    public static final String LESSON_INSTANCES = "lessonInstances";
    public static final String LESSON_INSTANCE_VOCABULARY = "lessonInstanceVocabulary";
    public static final String QUESTIONS = "questions";
    public static final String QUESTION_SET_IDS_PER_LESSON = "questionSetIDsPerLesson";
    public static final String QUESTION_SETS = "questionSets";
    public static final String QUESTION_SETS_QUESTION_IDS = "questions";
    public static final String QUESTION_SETS_VOCABULARY = "vocabulary";
    public static final String QUESTION_SETS_LABEL ="label";
    public static final String RANDOM_QUESTION_SET_IDS = "randomQuestionSets";
    public static final String RANDOM_QUESTION_SET_ID = "setID";
    public static final String RANDOM_QUESTION_SET_DATE = "date";
    public static final String INSTANCE_RECORDS = "instanceRecords";
    public static final String UTILS = "utils";
    public static final String RECOMMENDATION_MAP = "recommendationMap";
    public static final String RECOMMENDATION_MAP_EDGE_COUNT = "count";
    public static final String RECOMMENDATION_MAP_EDGE_DATA = "data";
    public static final String RECOMMENDATION_MAP_FOR_LESSON_GENERATION = "recommendationMapForLessonGeneration";
    public static final String APP_USAGE = "appUsage";
    public static final String VOCABULARY = "vocabulary";
    public static final String VOCABULARY_LIST = "vocabularyList";
    //make sure this matches the field in the class VocabularyListWord
    public static final String VOCABULARY_LIST_WORD_WORD = "word";
    public static final String VOCABULARY_DETAILS = "vocabularyDetails";
    public static final String REPORT_CARD = "reportCard";
    public static final String REPORT_CARD_CORRECT = "correct";
    public static final String REPORT_CARD_TOTAL = "total";

    private FirebaseDBHeaders(){}
}
