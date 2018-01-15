package com.linnca.pelicann.lessonlist;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDB;

import java.util.List;
import java.util.Set;

import pelicann.linnca.com.corefunctionality.db.OnDBResultListener;
import pelicann.linnca.com.corefunctionality.lessondetails.LessonData;
import pelicann.linnca.com.corefunctionality.lessonlist.LessonListRow;
import pelicann.linnca.com.corefunctionality.lessonlist.UserLessonListViewer;

import static pelicann.linnca.com.corefunctionality.lessonlist.UserLessonListViewer.STATUS_LOCKED;
import static pelicann.linnca.com.corefunctionality.lessonlist.UserLessonListViewer.STATUS_NONE;

class LessonListAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_NORMAL_ROW = 1;
    private final int VIEW_TYPE_REVIEW_ROW = 2;
    private final List<LessonListRow> data;
    private final int lessonLevel;
    private UserLessonListViewer userLessonListViewer;
    private final LessonList.LessonListListener listener;

    LessonListAdapter(int lessonLevel, List<LessonListRow> lessonRows, LessonList.LessonListListener listener, Set<String> clearedLessonKeys){
        this.lessonLevel = lessonLevel;
        this.data = lessonRows;
        this.listener = listener;
        this.userLessonListViewer = new UserLessonListViewer(new LessonListViewerImplementation(),
                clearedLessonKeys);
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

        /*LessonList.LessonListListener debugListener = new LessonList.LessonListListener() {
            @Override
            public void lessonListToLessonDetails(LessonData lessonData) {
                addClearedLessonKey(lessonData.getKey());
            }

            @Override
            public void lessonListToReviewLesson(String key) {
                addClearedLessonKey(key);
            }

            @Override
            public void setToolbarState(ToolbarState state) {

            }
        };*/

        if (holder instanceof LessonListRowViewHolder) {
            int[] rowStatus = new int[3];
            int nextToClearReviewPosition = userLessonListViewer.getNextToClearReviewPosition();
            if (position <= nextToClearReviewPosition) {
                for (int i = 0; i < 3; i++) {
                    LessonData data = lessonRow.getLessons()[i];
                    int status = userLessonListViewer.getItemStatus(data);
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
            int status = userLessonListViewer.getItemStatus(reviewData);
            ((LessonListReviewRowViewHolder)holder).populateRow(reviewData, listener, status);
        }
    }

    //debugging only
    private void addClearedLessonKey(String lessonKey){
        FirebaseDB db = new FirebaseDB();
        db.addClearedLesson(userLessonListViewer.getLessonLevel(lessonKey), lessonKey, new OnDBResultListener() {
            @Override
            public void onClearedLessonAdded(boolean firstTimeCleared) {
                super.onClearedLessonAdded(firstTimeCleared);
            }
        });
    }

    void setClearedLessonKeys(Set<String> clearedLessonKeys){
        userLessonListViewer = new UserLessonListViewer(new LessonListViewerImplementation(),
                clearedLessonKeys);
        notifyDataSetChanged();
    }
}
