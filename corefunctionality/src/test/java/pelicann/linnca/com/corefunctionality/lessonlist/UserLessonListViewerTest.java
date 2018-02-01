package pelicann.linnca.com.corefunctionality.lessonlist;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserLessonListViewerTest {
    private LessonListViewer lessonListViewer;

    @Before
    public void init(){
        lessonListViewer = new LessonListViewer() {
            @Override
            protected void populateLessons() {
                //the test lesson list looks like this:
                // 1  2  3
                // |  |__|
                // |  |
                // 4  5
                // REVIEW
                // 6
                // |__
                // |  |
                // 7  8
                // REVIEW
                // END OF LEVEL
                // 9
                List<LessonListRow> lessonRows;
                LessonListRow row;
                LessonData col1Data;
                List<String> col1Prerequisites;
                LessonData col2Data;
                List<String> col2Prerequisites;
                LessonData col3Data;
                List<String> col3Prerequisites;

                lessonRows = new ArrayList<>(5);
                titleCount = new HashMap<>();

                row = new LessonListRow();
                col1Data = new LessonData("key1", "title", 1, null, 100, 1, 1);
                col2Data = new LessonData("key2", "title", null, null, 100, 1, 1);
                col3Data = new LessonData("key3", "title", null, null, 100, 1, 1);
                row.setCol1(col1Data);
                row.setCol2(col2Data);
                row.setCol3(col3Data);
                lessonRows.add(row);

                row = new LessonListRow();
                col1Prerequisites = new ArrayList<>(1);
                col1Prerequisites.add("key1");
                col1Data = new LessonData("key4", "title4", null, col1Prerequisites, 100, 1, 1);
                col2Prerequisites = new ArrayList<>(2);
                col2Prerequisites.add("key2");
                col2Prerequisites.add("key3");
                col2Data = new LessonData("key5", "title5", null, col2Prerequisites, 100, 1, 1);
                row.setCol1(col1Data);
                row.setCol2(col2Data);
                lessonRows.add(row);

                row = new LessonListRow();
                row.setReview(true);
                col2Prerequisites = new ArrayList<>(2);
                col2Prerequisites.add("key4");
                col2Prerequisites.add("key5");
                col2Data = new LessonData(LessonData.formatReviewID(1,1), review, null, col2Prerequisites, 100, 0, 0);
                row.setCol2(col2Data);
                lessonRows.add(row);

                row = new LessonListRow();
                col1Data = new LessonData("key6", "title6", null, null, 100, 1, 1);
                row.setCol1(col1Data);
                lessonRows.add(row);

                row = new LessonListRow();
                col1Prerequisites = new ArrayList<>(1);
                col1Prerequisites.add("key6");
                col1Data = new LessonData("key7", "title7", null, col1Prerequisites, 100, 1, 1);
                col2Prerequisites = new ArrayList<>(1);
                col2Prerequisites.add("key6");
                col2Data = new LessonData("key8", "title8", null, col2Prerequisites, 100, 1, 1);
                row.setCol1(col1Data);
                row.setCol2(col2Data);
                lessonRows.add(row);

                row = new LessonListRow();
                row.setReview(true);
                col2Prerequisites = new ArrayList<>(2);
                col2Prerequisites.add("key7");
                col2Prerequisites.add("key8");
                col2Data = new LessonData(LessonData.formatReviewID(1,2), review, null, col2Prerequisites, 100, 0, 0);
                row.setCol2(col2Data);
                lessonRows.add(row);

                adjustRowTitles(lessonRows);
                lessonLevels.add(lessonRows);

                lessonRows = new ArrayList<>(1);
                row = new LessonListRow();
                col1Data = new LessonData("key9", "title9", null, null, 100, 1, 1);
                row.setCol1(col1Data);
                lessonRows.add(row);

                adjustRowTitles(lessonRows);
                lessonLevels.add(lessonRows);

            }
        };
    }

    @Test
    public void getNextReviewPosition_noLessonsCleared_shouldReturnFirstReviewPosition(){
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                new HashSet<String>());
        assertEquals(2, userLessonListViewer.getNextToClearReviewPosition());
    }

    @Test
    public void getNextReviewPosition_firstReviewCleared_shouldReturnSecondReviewPosition(){
        Set<String> clearedLessonKeys = new HashSet<>();
        clearedLessonKeys.add(LessonData.formatReviewID(1,1));
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                clearedLessonKeys);
        assertEquals(5, userLessonListViewer.getNextToClearReviewPosition());
    }

    @Test
    public void getNextReviewPosition_lastReviewCleared_shouldReturnLessonCleared(){
        Set<String> clearedLessonKeys = new HashSet<>();
        clearedLessonKeys.add(LessonData.formatReviewID(1,1));
        clearedLessonKeys.add(LessonData.formatReviewID(1,2));
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                clearedLessonKeys);
        //only eight rows total (level1 + level2) so if greater than 8,
        // we should be fine
        assertTrue(userLessonListViewer.getNextToClearReviewPosition() > 8);
    }

    @Test
    public void shouldSaveForReview_clearedFirstReviewAndLessonInBetweenFirstAndSecondReviews_shouldReturnTrue(){
        Set<String> clearedLessonKeys = new HashSet<>();
        clearedLessonKeys.add(LessonData.formatReviewID(1,1));
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                clearedLessonKeys);
        assertTrue(userLessonListViewer.shouldSaveForReview("key6"));
    }

    @Test
    public void shouldSaveForReview_clearedFirstReviewAndLessonBeforeFirstReview_shouldReturnFalse(){
        Set<String> clearedLessonKeys = new HashSet<>();
        clearedLessonKeys.add(LessonData.formatReviewID(1,1));
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                clearedLessonKeys);
        assertFalse(userLessonListViewer.shouldSaveForReview("key1"));
    }

    @Test
    public void getLessonsUnlockedByClearingLesson_lessonWithOneUnlockableLesson_shouldReturnThatLesson(){
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                new HashSet<String>());
        List<LessonData> lessonsUnlocked = userLessonListViewer.getLessonsUnlockedByClearing("key1");
        assertEquals(1, lessonsUnlocked.size());
        assertEquals("key4", lessonsUnlocked.get(0).getKey());
    }
    @Test
    public void getLessonsUnlockedByClearingLesson_lessonWithTwoUnlockableLessons_shouldReturnThoseLesson(){
        Set<String> clearedLessons = new HashSet<>(1);
        clearedLessons.add("key1");
        clearedLessons.add("key2");
        clearedLessons.add("key3");
        clearedLessons.add("key4");
        clearedLessons.add("key5");
        clearedLessons.add(LessonData.formatReviewID(1,1));
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                clearedLessons);
        List<LessonData> lessonsUnlocked = userLessonListViewer.getLessonsUnlockedByClearing("key6");
        assertEquals(2, lessonsUnlocked.size());
        Set<String> keys = new HashSet<>(2);
        for (LessonData lessonUnlocked : lessonsUnlocked){
            keys.add(lessonUnlocked.getKey());
        }
        assertTrue(keys.contains("key7"));
        assertTrue(keys.contains("key8"));
    }

    @Test
    public void getLessonsUnlockedByClearingLesson_lessonWithTwoLessonsToUnlockButNoneCleared_shouldNotReturnThatLesson(){
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                new HashSet<String>());
        List<LessonData> lessonsUnlocked = userLessonListViewer.getLessonsUnlockedByClearing("key2");
        assertEquals(0, lessonsUnlocked.size());
    }

    @Test
    public void getLessonsUnlockedByClearingLesson_lessonWithTwoLessonsToUnlockAndOneCleared_shouldReturnThatLesson(){
        Set<String> clearedLessons = new HashSet<>(1);
        clearedLessons.add("key2");
        UserLessonListViewer userLessonListViewer = new UserLessonListViewer(lessonListViewer,
                clearedLessons);
        List<LessonData> lessonsUnlocked = userLessonListViewer.getLessonsUnlockedByClearing("key3");
        assertEquals(1, lessonsUnlocked.size());
        assertEquals("key5", lessonsUnlocked.get(0).getKey());
    }
}
