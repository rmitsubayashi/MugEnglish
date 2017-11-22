package com.linnca.pelicann.lessonlist;

import com.linnca.pelicann.lessondetails.LessonData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// uses the user's cleared lessons to create a better picture
// of the lesson list state
public class UserLessonListViewer {
    private LessonListViewer lessonListViewer;
    private final Set<String> clearedLessonKeys;
    private int lastClearedReviewPosition = -1;
    private int nextToClearReviewPosition = -1;

    private final int LEVEL_CLEARED = 10000;

    public UserLessonListViewer(LessonListViewer lessonListViewer, Set<String> clearedLessonKeys){
        this.lessonListViewer = lessonListViewer;
        //the cleared lessons are all the cleared lessons for a level
        this.clearedLessonKeys = new HashSet<>(clearedLessonKeys);
        parseData();
    }

    public int getLastClearedReviewPosition(){
        return lastClearedReviewPosition;
    }

    public int getNextToClearReviewPosition(){
        return nextToClearReviewPosition;
    }

    //some of the methods just call on the underlying lesson list viewer
    // since the visibility is private
    public LessonData getLesson(String lessonKey){
        return lessonListViewer.getLessonData(lessonKey);
    }

    public int getLessonLevel(String lessonKey){
        return lessonListViewer.getLessonLevel(lessonKey);
    }

    public void addClearedLesson(String clearedLessonKey){
        clearedLessonKeys.add(clearedLessonKey);
        parseData();
    }

    public boolean isCleared(String lessonKey){
        return clearedLessonKeys.contains(lessonKey);
    }

    public boolean shouldSaveForReview(String lessonKey){
        //if the user cleared the level already,
        //we shouldn't save it for the review
        if (nextToClearReviewPosition == LEVEL_CLEARED){
            return false;
        }

        int rowIndex = lessonListViewer.getLessonRowIndex(lessonKey);
        //if the row is in between the last review and the user's next review,
        //add it to the review question pool
        return rowIndex > lastClearedReviewPosition && rowIndex < nextToClearReviewPosition;
    }

    public List<LessonData> getLessonsUnlockedByClearing(String lessonKey){
        List<LessonData> unlockedLessons = new ArrayList<>(5);
        //if the lesson is already cleared,
        //clearing that lesson shouldn't unlock anything
        if (clearedLessonKeys.contains(lessonKey)){
            return unlockedLessons;
        }

        int lessonLevel = lessonListViewer.getLessonLevel(lessonKey);
        List<LessonListRow> lessonRows = lessonListViewer.getLessonsAtLevel(lessonLevel);
        for (LessonListRow row : lessonRows) {
            for (LessonData lessonData : row.getLessons()) {
                if (lessonData == null)
                    continue;
                List<String> prerequisites = lessonData.getPrerequisiteKeys();
                if (prerequisites == null)
                    continue;
                if (prerequisites.contains(lessonKey)) {
                    //temporary
                    boolean unlocked = true;
                    for (String prerequisite : prerequisites){
                        //this is the lesson in question so we
                        // don't care that the user hasn't completed this
                        // prerequisite yet
                        if (prerequisite.equals(lessonKey)){
                            continue;
                        }
                        if (!clearedLessonKeys.contains(prerequisite)){
                            unlocked = false;
                            break;
                        }
                    }
                    if (unlocked){
                        unlockedLessons.add(new LessonData(lessonData));
                    }
                }
            }
        }

        return unlockedLessons;
    }

    private void parseData(){
        int maxClearedLevel = -1;
        int maxClearedReviewNumber = -1;
        for (String key : clearedLessonKeys){
            if (!lessonListViewer.isReview(key)) {
                //we don't need to worry about non-review lessons
                continue;
            }
            //first check the level.
            //we technically don't need to because
            // all the cleared lessons will be from one level,
            //but in case we change thee implementation
            int reviewLevel = lessonListViewer.getLessonLevel(key);
            if (reviewLevel > maxClearedLevel){
                maxClearedLevel = reviewLevel;
                maxClearedReviewNumber = -1;
            } else if (reviewLevel < maxClearedLevel) {
                //if the level is lower, we don't need to worry about it
                continue;
            } //do nothing if lesson level is equal
            //next get the review number
            int reviewNumber = lessonListViewer.getReviewNumber(key);
            if (reviewNumber > maxClearedReviewNumber){
                maxClearedReviewNumber = reviewNumber;
            }
        }
        if (maxClearedReviewNumber == -1){
            //we haven't cleared a review yet
            lastClearedReviewPosition = -1;
        } else {
            String lastClearedReviewKey = lessonListViewer.getReviewID(maxClearedLevel, maxClearedReviewNumber);
            lastClearedReviewPosition = lessonListViewer.getLessonRowIndex(lastClearedReviewKey);
        }
        //if the user hasn't cleared a review yet, the first review should be the next to clear
        // review key
        if (maxClearedReviewNumber == -1){
            maxClearedLevel = 1;
            maxClearedReviewNumber = 0;
        }
        String nextClearedReviewKey = lessonListViewer.getReviewID(maxClearedLevel, maxClearedReviewNumber+1);
        //handles the case where the user has completed every review for the level.
        nextToClearReviewPosition = lessonListViewer.getLessonRowIndex(nextClearedReviewKey) != -1 ?
                lessonListViewer.getLessonRowIndex(nextClearedReviewKey) : LEVEL_CLEARED ;
    }


}
