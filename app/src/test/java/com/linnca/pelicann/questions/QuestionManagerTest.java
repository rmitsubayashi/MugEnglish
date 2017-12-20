package com.linnca.pelicann.questions;

import com.linnca.pelicann.db.MockFirebaseDB;
import com.linnca.pelicann.lessondetails.LessonInstanceData;
import com.linnca.pelicann.lessondetails.LessonInstanceDataQuestionSet;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionManagerTest {
    private QuestionManager questionManager;
    private MockFirebaseDB db;

    @Before
    public void init(){
        db = new MockFirebaseDB();
    }

    @Test
    public void startQuestions_startQuestions_shouldCallOnNextQuestionMethodInListener(){
        final boolean[] called = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                called[0] = true;
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        List<List<String>> questionIDs = new ArrayList<>(2);
        List<String> question1 = new ArrayList<>();
        question1.add("questionID1");
        questionIDs.add(question1);
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                questionIDs, new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);
        questionManager.startQuestions(null, lessonInstanceData, "lessonKey1", null);
        assertTrue(called[0]);
    }

    @Test
    public void startQuestions_startQuestionsWithLessonInstance_firstQuestionShouldBeFirstQuestionInLessonInstance(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                assertTrue(questionData.getId().equals("questionID1"));
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                //not testing this
            }
            
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);

        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID1", firstQuestion);
        questionManager.startQuestions(null, lessonInstanceData, "lessonID1", null);

    }

    @Test
    public void startQuestions_startQuestions_firstQuestionShouldHaveQuestionNumberOfOne(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                assertEquals(1, questionNumber);
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);
        questionManager.startQuestions(null, lessonInstanceData, "lessonID1", null);

    }

    @Test
    public void startQuestions_startQuestionsWithOneQuestion_totalQuestionCountShouldBeOne(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                assertEquals(1, totalQuestions);
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);
        questionManager.startQuestions(null, lessonInstanceData, "lessonID1", null);

    }

    @Test
    public void startQuestions_startQuestionsWithLessonInstanceAndGoToNextQuestion_nextQuestionShouldBeSecondQuestionInLessonInstance(){
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                assertTrue(questionData.getId().equals("questionID1") ||
                questionData.getId().equals("questionID2"));
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                //not testing this
            }

            
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        List<List<String>> questionIDs = new ArrayList<>(2);
        List<String> question1 = new ArrayList<>();
        question1.add("questionID1");
        questionIDs.add(question1);
        List<String> question2 = new ArrayList<>();
        question2.add("questionID2");
        questionIDs.add(question2);
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                questionIDs, new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);

        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID1", firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question2", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID2", secondQuestion);
        questionManager.startQuestions(null, lessonInstanceData, "lessonID1", null);
        questionManager.nextQuestion(null, false, null);
    }

    @Test
    public void startQuestions_startQuestionsWithLessonInstanceAndGoToNextQuestion_nextQuestionShouldHaveQuestionNumberOfTwo(){
        final boolean[] twoCalled = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                if (questionNumber == 2){
                    twoCalled[0] = true;
                }
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                //not testing this
            }
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        List<List<String>> questionIDs = new ArrayList<>(2);
        List<String> question1 = new ArrayList<>();
        question1.add("questionID1");
        questionIDs.add(question1);
        List<String> question2 = new ArrayList<>();
        question2.add("questionID2");
        questionIDs.add(question2);
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                questionIDs, new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);

        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID1", firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question2", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID2", secondQuestion);
        questionManager.startQuestions(null, lessonInstanceData, "lessonID1", null);
        questionManager.nextQuestion(null, false, null);
        assertTrue(twoCalled[0]);
    }

    @Test
    public void endQuestions_goThroughAllQuestions_shouldCallOnQuestionsFinished(){
        final boolean[] called = new boolean[]{false};
        QuestionManager.QuestionManagerListener listener = new QuestionManager.QuestionManagerListener() {
            @Override
            public void onNextQuestion(QuestionData questionData, int questionNumber, int totalQuestions, boolean firstQuestion) {
                //not testing this
            }

            @Override
            public void onQuestionsFinished(InstanceRecord instanceRecord, ArrayList<String> questionIDs, List<QuestionData> missedQuestions) {
                called[0] = true;
            }

            
        };
        questionManager = new QuestionManager(db, listener);
        List<LessonInstanceDataQuestionSet> questionSets = new ArrayList<>();
        QuestionSet questionSet = new QuestionSet("questionSetID", "wikiDatID",
                "interestLabel",
                new ArrayList<List<String>>(), new ArrayList<String>(), 1);
        questionSets.add(new LessonInstanceDataQuestionSet(questionSet, true));
        //ids are set during adding
        LessonInstanceData lessonInstanceData = new LessonInstanceData("id", "lessonKey",
                0L, new ArrayList<String>(), new ArrayList<String>(),
                questionSets, LessonInstanceData.QUESTION_ORDER_ORDER_BY_SET);

        QuestionData firstQuestion = new QuestionData("questionID1","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question1", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID1", firstQuestion);
        QuestionData secondQuestion = new QuestionData("questionID2","lessonID1",  Question_TrueFalse.QUESTION_TYPE,
                "question2", null, Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE, null, null);
        db.questions.put("questionID2", secondQuestion);
        questionManager.startQuestions(null, lessonInstanceData, "lessonID1", null);
        questionManager.nextQuestion(null, false, null);
        //questions should be finished here
        questionManager.nextQuestion(null, false, null);
        assertTrue(called[0]);
    }


}
