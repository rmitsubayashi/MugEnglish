package com.linnca.pelicann.lessonlist;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.FirebaseDatabase;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnResultListener;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LessonListAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_NORMAL_ROW = 1;
    private final int VIEW_TYPE_REVIEW_ROW = 2;
    //gold?
    static final int STATUS_CLEARED = 0;
    //with color
    static final int STATUS_ACTIVE = 1;
    //gray color
    static final int STATUS_NEXT_ACTIVE = 2;
    //lock
    static final int STATUS_LOCKED = 3;
    //no view
    static final int STATUS_NONE = 4;
    private final List<LessonListRow> data;
    private final int lessonLevel;
    private UserLessonList userLessonList;
    private final LessonList.LessonListListener listener;

    LessonListAdapter(int lessonLevel, List<LessonListRow> lessonRows, LessonList.LessonListListener listener, Set<String> clearedLessonKeys){
        this.lessonLevel = lessonLevel;
        this.data = lessonRows;
        this.listener = listener;
        this.userLessonList = new UserLessonList(clearedLessonKeys);
    }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public int getItemCount(){return data.size();}

    @Override
    public int getItemViewType(int position){
        LessonListRow row = data.get(position);
        if (row.isReview()){
            return VIEW_TYPE_REVIEW_ROW;
        } else {
            return VIEW_TYPE_NORMAL_ROW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView;
        switch (viewType){
            case VIEW_TYPE_NORMAL_ROW:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_list_list_item, parent, false);
                return new LessonListRowViewHolder(itemView);
            case VIEW_TYPE_REVIEW_ROW:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_list_review_item, parent, false);
                return new LessonListReviewRowViewHolder(itemView);
            default:
                return null;
        }
        
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final LessonListRow lessonRow = data.get(position);
        if (holder instanceof LessonListRowViewHolder) {
            int[] rowStatus = new int[3];
            int nextToClearReviewPosition = userLessonList.getNextToClearReviewPosition();
            if (position <= nextToClearReviewPosition) {
                for (int i = 0; i < 3; i++) {
                    LessonData data = lessonRow.getLessons()[i];
                    int status = getItemStatus(data);
                    rowStatus[i] = status;
                }
            } else {
                //we can keep everything locked
                //because the user hasn't finished the review before this
                for (int i = 0; i < 3; i++) {
                    LessonData data = lessonRow.getLessons()[i];
                    if (data != null)
                        rowStatus[i] = STATUS_LOCKED;
                    else
                        rowStatus[i] = STATUS_NONE;
                }
            }

            LessonList.LessonListListener debugListener = new LessonList.LessonListListener() {
                @Override
                public void lessonListToLessonDetails(LessonData lessonData) {
                    addClearedLessonKey(lessonData.getKey());
                }

                @Override
                public void lessonListToReview(int lessonLevel, String key) {

                }

                @Override
                public void setToolbarState(ToolbarState state) {

                }
            };
            ((LessonListRowViewHolder)holder).populateRow(lessonRow, listener, rowStatus);

            LessonListRow rowBefore = position == 0 ? null : data.get(position - 1);
            LessonListRow rowAfter = position == data.size() - 1 ? null : data.get(position + 1);
            //don't connect reviews and the preceding/following item
            if (rowBefore != null && rowBefore.isReview())
                rowBefore = null;
            if (rowAfter != null && rowAfter.isReview())
                rowAfter = null;
            ((LessonListRowViewHolder)holder).connectRows(lessonRow, rowBefore, rowAfter);
        } else if (holder instanceof LessonListReviewRowViewHolder){
            LessonData reviewData = lessonRow.getLessons()[1];
            int status = getItemStatus(reviewData);
            ((LessonListReviewRowViewHolder)holder).populateRow(reviewData, listener, status, lessonLevel);
        }
    }

    //debugging only
    private void addClearedLessonKey(String lessonKey){
        FirebaseDB db = new FirebaseDB();
        db.addClearedLesson(userLessonList.getLessonLevel(lessonKey), lessonKey, new OnResultListener() {
            @Override
            public void onClearedLessonAdded(boolean firstTimeCleared) {
                super.onClearedLessonAdded(firstTimeCleared);
            }
        });
    }

    void setClearedLessonKeys(Set<String> clearedLessonKeys){
        userLessonList = new UserLessonList(clearedLessonKeys);
        notifyDataSetChanged();
    }

    private int getItemStatus(LessonData data){
        if (data == null){
            return STATUS_NONE;
        }

        if (userLessonList.isCleared(data.getKey())){
            return STATUS_CLEARED;
        }
        boolean active = true;
        List<String> prerequisites = data.getPrerequisiteKeys();
        if (prerequisites == null){
            return STATUS_ACTIVE;
        }
        //check if we've cleared all prerequisites for this lesson
        for (String prerequisiteKey : prerequisites){
            if (!userLessonList.isCleared(prerequisiteKey)){
                active = false;
                break;
            }
        }
        if (active){
            return STATUS_ACTIVE;
        }

        for (String prerequisiteKey : prerequisites){
            LessonData prerequisite = userLessonList.getLesson(prerequisiteKey);
            List<String> prerequisitesOfPrerequisites = prerequisite.getPrerequisiteKeys();
            if (prerequisitesOfPrerequisites == null){
                return STATUS_NEXT_ACTIVE;
            }
            for (String key : prerequisitesOfPrerequisites){
                if (userLessonList.isCleared(key)){
                    return STATUS_NEXT_ACTIVE;
                }
            }
        }

        return STATUS_LOCKED;
    }
}
