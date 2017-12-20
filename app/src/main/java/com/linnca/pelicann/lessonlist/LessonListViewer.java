package com.linnca.pelicann.lessonlist;

import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.lessondetails.LessonData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//stores the lesson list information
// and formats it for viewing
public abstract class LessonListViewer {
    final List<List<LessonListRow>> lessonLevels = new ArrayList<>();
    //so we can go people1 -> people2 -> people3
    Map<String, Integer> titleCount;
    protected final String review = "ふくしゅう";
    //so we can set the review IDs in order
    private int reviewIDNumber = 1;
    private int reviewIDLevel = 1;

    public LessonListViewer(){
        populateLessons();
    }

    abstract protected void populateLessons();

    public void debugUnlockAllLessons(){
        Database db = new FirebaseDB();
        db.clearAllLessons(lessonLevels);
    }

    String getNextReviewID(){
        String id = LessonData.formatReviewID(reviewIDLevel, reviewIDNumber);
        reviewIDNumber++;
        return id;
    }

    void nextLevelResetReviewIDCt(){
        reviewIDNumber = 0;
        reviewIDLevel++;
    }

    void adjustRowTitles(List<LessonListRow> rows){
        for (LessonListRow row : rows){
            LessonData[] lessons = row.getLessons();
            //order is center -> left -> right
            if (lessons[1] != null){
                lessons[1].setTitle(formatTitle(lessons[1].getTitle()));
            }
            if (lessons[0] != null){
                lessons[0].setTitle(formatTitle(lessons[0].getTitle()));
            }
            if (lessons[2] != null){
                lessons[2].setTitle(formatTitle(lessons[2].getTitle()));
            }
        }
    }

    private String formatTitle(String title){
        //don't enumerate review titles
        if (title.equals(review)){
            return title;
        }
        if (titleCount.containsKey(title)){
            int newCt = titleCount.get(title) + 1;
            titleCount.put(title, newCt);
            return title + Integer.toString(newCt);
        } else {
            titleCount.put(title, 1);
            return title + "1";
        }
    }

    public List<LessonListRow> getLessonsAtLevel(int level){
        level = level - 1;
        return lessonLevels.get(level);
    }

    public LessonData getLessonData(String lessonKey){
        for (List<LessonListRow> lessonRows : lessonLevels) {
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData == null)
                        continue;
                    if (lessonData.getKey().equals(lessonKey)) {
                        return lessonData;
                    }
                }
            }
        }
        return null;
    }

    public int getLessonLevel(String lessonKey){
        int levelCt = lessonLevels.size();
        for (int i=0; i<levelCt; i++) {
            List<LessonListRow> lessonRows = lessonLevels.get(i);
            for (LessonListRow row : lessonRows) {
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData == null)
                        continue;
                    if (lessonData.getKey().equals(lessonKey)) {
                        return i+1;
                    }
                }
            }
        }
        return -1;
    }

    int getLessonRowIndex(String lessonKey){
        int levelCt = lessonLevels.size();
        for (int i=0; i<levelCt; i++) {
            List<LessonListRow> lessonRows = lessonLevels.get(i);
            int rowCt = lessonRows.size();
            for (int j=0; j<rowCt; j++) {
                LessonListRow row = lessonRows.get(j);
                for (LessonData lessonData : row.getLessons()) {
                    if (lessonData == null)
                        continue;
                    if (lessonData.getKey().equals(lessonKey)) {
                        return j;
                    }
                }
            }
        }
        return -1;
    }

    //for description page
    public boolean layoutExists(String lessonKey){
        LessonData lessonData = getLessonData(lessonKey);
        return lessonData != null && lessonData.getDescriptionLayout() != null;
    }
}
