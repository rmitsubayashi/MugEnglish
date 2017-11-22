package com.linnca.pelicann.lessonlist;

import com.linnca.pelicann.lessondetails.LessonData;

import java.util.HashSet;
import java.util.Set;

//handles all data after getting user data
// ie the user's cleared lessons
public class UserLessonList {
    private LessonListViewer lessonListViewer = new LessonListViewer();
    private final Set<String> clearedLessonKeys;
    private int lastClearedReviewPosition = -1;
    private int nextToClearReviewPosition = -1;

    private final int LEVEL_CLEARED = 10000;

    public UserLessonList(Set<String> clearedLessonKeys){
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

    private void parseData(){
        int maxClearedReview = -1;
        for (String key : clearedLessonKeys){
            //all the review ids are "id_review#"
            if (key.startsWith("id_review")){
                //get the review number
                String numberString = key.replace("id_review","");
                int reviewNumber = Integer.parseInt(numberString);
                if (reviewNumber > maxClearedReview){
                    maxClearedReview = reviewNumber;
                }
            }
        }
        String lastClearedReviewKey = "id_review" + Integer.toString(maxClearedReview);
        if (maxClearedReview == -1){
            //we haven't cleared a review yet
            lastClearedReviewPosition = -1;
        } else {
            lastClearedReviewPosition = lessonListViewer.getLessonRowIndex(lastClearedReviewKey);
        }
        //if the user hasn't cleared a review yet, we should find the first available review
        String nextClearedReviewKey = "id_review" + Integer.toString(maxClearedReview == -1 ?
                1 : maxClearedReview+1);
        //handles the case where the user has completed every review.
        nextToClearReviewPosition = lessonListViewer.getLessonRowIndex(nextClearedReviewKey) != -1 ?
                lessonListViewer.getLessonRowIndex(nextClearedReviewKey) : LEVEL_CLEARED ;
    }


}
