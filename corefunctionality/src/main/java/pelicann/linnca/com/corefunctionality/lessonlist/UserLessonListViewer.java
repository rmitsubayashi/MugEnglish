package pelicann.linnca.com.corefunctionality.lessonlist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;

// uses the user's cleared lessons to create a better picture
// of the lesson list state
public class UserLessonListViewer {
    private LessonListViewer lessonListViewer;
    private final Set<String> clearedLessonKeys;
    private int lastClearedReviewPosition = -1;
    private int nextToClearReviewPosition = -1;

    private final int LEVEL_CLEARED = 10000;

    //gold?
    public static final int STATUS_CLEARED = 0;
    //with color
    public static final int STATUS_ACTIVE = 1;
    //gray color
    public static final int STATUS_NEXT_ACTIVE = 2;
    //lock
    public static final int STATUS_LOCKED = 3;
    //no view
    public static final int STATUS_NONE = 4;

    public UserLessonListViewer(LessonListViewer lessonListViewer, Set<String> clearedLessonKeys){
        this.lessonListViewer = lessonListViewer;
        //the cleared lessons are all the cleared lessons for a level
        if (clearedLessonKeys != null)
            this.clearedLessonKeys = new HashSet<>(clearedLessonKeys);
        else
            this.clearedLessonKeys = null;
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
        if (clearedLessonKeys != null) {
            clearedLessonKeys.add(clearedLessonKey);
            parseData();
        }
    }

    boolean isCleared(String lessonKey){
        if (clearedLessonKeys == null)
            return false;
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
        if (clearedLessonKeys == null){
            return unlockedLessons;
        }
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
        if (clearedLessonKeys == null)
            return;
        int maxClearedLevel = -1;
        int maxClearedReviewNumber = -1;
        for (String key : clearedLessonKeys){
            if (!LessonData.isReview(key)) {
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
            int reviewNumber = LessonData.extractReviewNumber(key);
            if (reviewNumber > maxClearedReviewNumber){
                maxClearedReviewNumber = reviewNumber;
            }
        }
        if (maxClearedReviewNumber == -1){
            //we haven't cleared a review yet
            lastClearedReviewPosition = -1;
        } else {
            String lastClearedReviewKey = LessonData.formatReviewID(maxClearedLevel, maxClearedReviewNumber);
            lastClearedReviewPosition = lessonListViewer.getLessonRowIndex(lastClearedReviewKey);
        }
        //if the user hasn't cleared a review yet, the first review should be the next to clear
        // review key
        if (maxClearedReviewNumber == -1){
            maxClearedLevel = 1;
            maxClearedReviewNumber = 0;
        }
        String nextClearedReviewKey = LessonData.formatReviewID(maxClearedLevel, maxClearedReviewNumber+1);
        //handles the case where the user has completed every review for the level.
        nextToClearReviewPosition = lessonListViewer.getLessonRowIndex(nextClearedReviewKey) != -1 ?
                lessonListViewer.getLessonRowIndex(nextClearedReviewKey) : LEVEL_CLEARED ;
    }

    public int getItemStatus(LessonData data){
        if (clearedLessonKeys == null){
            return STATUS_LOCKED;
        }
        if (data == null){
            return STATUS_NONE;
        }

        if (isCleared(data.getKey())){
            return STATUS_CLEARED;
        }
        boolean active = true;
        List<String> prerequisites = data.getPrerequisiteKeys();
        if (prerequisites == null){
            return STATUS_ACTIVE;
        }
        //check if we've cleared all prerequisites for this lesson
        for (String prerequisiteKey : prerequisites){
            if (!isCleared(prerequisiteKey)){
                active = false;
                break;
            }
        }
        if (active){
            return STATUS_ACTIVE;
        }

        for (String prerequisiteKey : prerequisites){
            LessonData prerequisite = getLesson(prerequisiteKey);
            List<String> prerequisitesOfPrerequisites = prerequisite.getPrerequisiteKeys();
            if (prerequisitesOfPrerequisites == null){
                return STATUS_NEXT_ACTIVE;
            }
            for (String key : prerequisitesOfPrerequisites){
                if (isCleared(key)){
                    return STATUS_NEXT_ACTIVE;
                }
            }
        }

        return STATUS_LOCKED;
    }

}
