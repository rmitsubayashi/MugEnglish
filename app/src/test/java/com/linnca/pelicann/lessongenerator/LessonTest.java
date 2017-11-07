package com.linnca.pelicann.lessongenerator;

import com.linnca.pelicann.db.TestDB;
import com.linnca.pelicann.lessongenerator.lessons.Goodbye_bye;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LessonTest {
    private TestDB db;

    @Before
    public void setUp(){
        db = new TestDB();
    }

    @Test
    public void lessonFactory_parseLesson_shouldCreateLessonClass(){
        Lesson lesson = LessonFactory.parseLesson(Goodbye_bye.KEY, db,null);
        assertTrue(lesson instanceof Goodbye_bye);
    }

    @Test
    public void lessonFactory_parseLesson_shouldReturnNullOnInvalidLessonKey(){
        Lesson lesson = LessonFactory.parseLesson("invalid key", db, null);
        assertNull(lesson);
    }

    @Test
    public void lessonWithOnlyGenericQuestions_saveQuestions_shouldSaveQuestionsIntoDB() throws Exception{
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, db, null);
        lessonWithOnlyGenericQuestions.saveGenericQuestions();
        List<List<String>> questionIDSets = lessonWithOnlyGenericQuestions.getGenericQuestionIDSets();
        boolean noMatch = false;
        for (List<String> questionVariations : questionIDSets){
            for (String questionID : questionVariations){
                if (!db.questions.containsKey(questionID)){
                    noMatch = true;
                    break;
                }
            }
        }
        assertFalse(noMatch);
    }

    @Test
    public void lessonWithOnlyGenericQuestions_saveQuestions_shouldOnlySaveGenericQuestionsIntoDB() throws Exception{
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, db, null);
        lessonWithOnlyGenericQuestions.saveGenericQuestions();
        List<List<String>> questionIDSets = lessonWithOnlyGenericQuestions.getGenericQuestionIDSets();
        int questionCt = 0;
        for (List<String> questionVariations : questionIDSets){
            questionCt += questionVariations.size();
        }
        assertEquals(questionCt, db.questions.size());
    }

    /*
    @Test
    public void lessonWithOnlyGenericQuestions_createInstance_shouldOnlyHaveGenericQuestions() throws Exception{
        Lesson.LessonListener lessonListener = new Lesson.LessonListener() {
            @Override
            public void onLessonCreated() {
                //assertEquals();
            }
        };
        Lesson lessonWithOnlyGenericQuestions = LessonFactory.parseLesson(Goodbye_bye.KEY, db, lessonListener);
        lessonWithOnlyGenericQuestions.createInstance();
    }*/
}
