package com.linnca.pelicann.lessonlist;


import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class LessonListViewerTest {
    private LessonListViewer lessonListViewer;

    @Before
    public void init(){
        lessonListViewer = new LessonListViewer() {
            @Override
            protected void populateLessons() {
                //the test lesson list looks like this:
                // 1  2  3
                // END OF LESSON
                // 4
                // 5
                List<LessonListRow> lessonRows;
                LessonListRow row;
                LessonData col1Data;
                LessonData col2Data;
                LessonData col3Data;

                lessonRows = new ArrayList<>(1);
                titleCount = new HashMap<>();

                row = new LessonListRow();
                col1Data = new LessonData("key1", "title", 1, null, 100, R.color.lblue300, 1);
                col2Data = new LessonData("key2", "title", null, null, 100, R.color.lblue700, 1);
                col3Data = new LessonData("key3", "title", null, null, 100, R.color.lblue700, 1);
                row.setCol1(col1Data);
                row.setCol2(col2Data);
                row.setCol3(col3Data);
                lessonRows.add(row);

                adjustRowTitles(lessonRows);
                lessonLevels.add(lessonRows);

                titleCount.clear();
                nextLevelResetReviewIDCt();

                lessonRows = new ArrayList<>(1);
                row = new LessonListRow();
                col1Data = new LessonData("key4", "title4", null, null, 100, R.color.lblue300, 1);
                row.setCol1(col1Data);
                lessonRows.add(row);

                lessonRows = new ArrayList<>(1);
                row = new LessonListRow();
                col1Data = new LessonData("key5", "title5", null, null, 100, R.color.lblue300, 1);
                row.setCol1(col1Data);
                lessonRows.add(row);

                adjustRowTitles(lessonRows);
                lessonLevels.add(lessonRows);

            }
        };
    }

    @Test
    public void adjustRowTitles_sameTitlesShouldBeIncrementedInCenterLeftRightOrder(){
        LessonListRow firstRow = lessonListViewer.lessonLevels.get(0).get(0);
        LessonData[] lessons = firstRow.getLessons();
        assertEquals("title2", lessons[0].getTitle());
        assertEquals("title1", lessons[1].getTitle());
        assertEquals("title3", lessons[2].getTitle());
    }

    @Test
    public void getLessonDataByKey_shouldGetRightData(){
        LessonData data = lessonListViewer.getLessonData("key2");
        assertEquals("key2", data.getKey());
    }

    @Test
    public void getLessonsAtLevel_shouldGetRightLevel(){
        List<LessonListRow> level1Rows = lessonListViewer.getLessonsAtLevel(1);
        assertEquals(1, level1Rows.size());
    }

    @Test
    public void getLessonLevel_shouldGetRightLevel(){
        int level = lessonListViewer.getLessonLevel("key1");
        assertEquals(1, level);
    }

    @Test
    public void getLessonRowIndex_shouldGetRightRowIndex(){
        int rowIndex = lessonListViewer.getLessonRowIndex("key1");
        assertEquals(0, rowIndex);
    }

    @Test
    public void checkLayoutExistsOnLessonWithLayout_shouldReturnTrue(){
        boolean layoutExists = lessonListViewer.layoutExists("key1");
        assertTrue(layoutExists);
    }

    @Test
    public void checkLayoutExistsOnLessonWithOutLayout_shouldReturnFalse(){
        boolean layoutExists = lessonListViewer.layoutExists("key2");
        assertFalse(layoutExists);
    }

    @Test
    public void createTwoReviewIDs_shouldBeUnique(){
        String firstID = lessonListViewer.getNextReviewID();
        String secondID = lessonListViewer.getNextReviewID();
        assertFalse(firstID.equals(secondID));
    }

    @Test
    public void createTwoReviewIDsInDifferentLevels_shouldBeUnique(){
        String firstID = lessonListViewer.getNextReviewID();
        lessonListViewer.nextLevelResetReviewIDCt();
        String secondID = lessonListViewer.getNextReviewID();
        assertFalse(firstID.equals(secondID));
    }

}
