package com.linnca.pelicann.db;

class FirebaseDBHeaders {
    //headers for each child in the firebase database
    static final String USER_INTERESTS = "userInterests";
    //make sure these match the class variable names in WikiDataEntryData
    static final String USER_INTERESTS_PRONUNCIATION = "pronunciation";
    static final String USER_INTERESTS_CLASSIFICATION = "classification";
    static final String USER_INTEREST_RANKINGS = "userInterestRankings";
    static final String USER_INTEREST_RANKINGS_COUNT = "count";
    static final String USER_INTEREST_RANKINGS_DATA = "data";
    static final String CLEARED_LESSONS = "clearedLessons";
    static final String REVIEW_QUESTIONS = "reviewQuestions";
    static final String LESSON_INSTANCES = "lessonInstances";
    static final String LESSON_INSTANCE_VOCABULARY = "lessonInstanceVocabulary";
    static final String QUESTIONS = "questions";
    //static final String QUESTION_SET_IDS_PER_USER_INTEREST_PER_LESSON = "questionSetIDsPerUserInterestPerLesson";
    static final String QUESTION_SETS = "questionSets";
    //make sure these headers matches the fields in QuestionSet
    static final String QUESTION_SET_COUNT = "count";
    static final String QUESTION_SET_INTEREST_ID = "interestID";
    static final String INSTANCE_RECORDS = "instanceRecords";
    static final String UTILS = "utils";
    static final String UTILS_SPORTS_VERB_MAPPINGS = "sportsVerbMapping";
    static final String UTILS_SPORT_VERB_MAPPING_OBJECT = "object";
    static final String UTILS_SPORT_VERB_MAPPING_VERB = "verb";
    static final String APP_USAGE = "appUsage";
    static final String VOCABULARY = "vocabulary";
    static final String VOCABULARY_LIST = "vocabularyList";
    //make sure this matches the field in the class VocabularyListWord
    static final String VOCABULARY_LIST_WORD_WORD = "word";
    static final String VOCABULARY_DETAILS = "vocabularyDetails";
    static final String REPORT_CARD = "reportCard";
    static final String REPORT_CARD_CORRECT = "correct";
    static final String REPORT_CARD_TOTAL = "total";

    private FirebaseDBHeaders(){}
}
