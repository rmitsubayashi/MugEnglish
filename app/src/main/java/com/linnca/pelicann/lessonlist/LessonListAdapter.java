package com.linnca.pelicann.lessonlist;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;

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
    private final Map<String, LessonData> allLessons = new HashMap<>();
    private final Map<String, Integer> allLessonsRowPosition = new HashMap<>();
    private final Set<String> clearedLessonKeys = new HashSet<>();
    private int lastClearedReviewPosition = -1;
    private int nextToClearReviewPosition = -1;
    private final LessonList.LessonListListener listener;

    LessonListAdapter(List<LessonListRow> lessonRows, LessonList.LessonListListener listener){
        this.data = lessonRows;
        this.listener = listener;
        populateMap();
        //debugging
        setReviewMargins();
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
            ((LessonListReviewRowViewHolder)holder).populateRow(reviewData, listener, status);
        }
    }

    private View.OnClickListener debugListener(final String key){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addClearedLessonKey(key);
            }
        };
    }

    private void populateMap(){
        if (data == null)
            return;
        int rowCt = 0;
        for (LessonListRow row : data){
            LessonData[] dataList = row.getLessons();
            for (LessonData data : dataList){
                if (data != null) {
                    allLessons.put(data.getKey(), data);
                    allLessonsRowPosition.put(data.getKey(), rowCt);
                }
            }
            rowCt++;
        }
    }

    public void addClearedLessonKey(String lessonKey){
        clearedLessonKeys.add(lessonKey);
        setReviewMargins();
        notifyDataSetChanged();
    }

    public void setClearedLessonKeys(Set<String> clearedLessonKeys){
        this.clearedLessonKeys.clear();
        for (String key : clearedLessonKeys){
            this.clearedLessonKeys.add(key);
        }
        setReviewMargins();
        notifyDataSetChanged();
    }

    private void setReviewMargins(){
        //all the review ids are "id_review#"
        int maxClearedReview = -1;
        for (String key : clearedLessonKeys){
            if (key.startsWith("id_review")){
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
            lastClearedReviewPosition = allLessonsRowPosition.get(lastClearedReviewKey);
        }
        //if the user hasn't cleared a review yet, we should find the first available review
        String nextClearedReviewKey = "id_review" + Integer.toString(maxClearedReview == -1 ?
                1 : maxClearedReview+1);
        //handles the case where the user has completed every review.
        nextToClearReviewPosition = allLessonsRowPosition.get(nextClearedReviewKey) != null ?
                allLessonsRowPosition.get(nextClearedReviewKey) : 2147483647 ;
    }

    private int getItemStatus(LessonData data){
        if (data == null){
            return STATUS_NONE;
        }

        Log.d("lessonListAdapter", data.getTitle());
        if (clearedLessonKeys.contains(data.getKey())){
            return STATUS_CLEARED;
        }
        boolean active = true;
        List<String> prerequisites = data.getPrerequisiteKeys();
        if (prerequisites == null){
            return STATUS_ACTIVE;
        }
        int activeLeeway = data.getPrerequisiteLeeway();
        int currentLeeway = 0;
        for (String prerequisiteKey : prerequisites){
            if (!clearedLessonKeys.contains(prerequisiteKey)){
                if (currentLeeway >= activeLeeway) {
                    active = false;
                    break;
                }
                currentLeeway++;
            }
        }
        if (active){
            return STATUS_ACTIVE;
        }

        for (String prerequisiteKey : prerequisites){
            LessonData prerequisite = allLessons.get(prerequisiteKey);
            List<String> prerequisitesOfPrerequisites = prerequisite.getPrerequisiteKeys();
            if (prerequisitesOfPrerequisites == null){
                return STATUS_NEXT_ACTIVE;
            }
            for (String key : prerequisitesOfPrerequisites){
                if (clearedLessonKeys.contains(key)){
                    return STATUS_NEXT_ACTIVE;
                }
            }
        }

        return STATUS_LOCKED;
    }
}
