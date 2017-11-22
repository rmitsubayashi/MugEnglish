package com.linnca.pelicann.userprofile;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessonlist.LessonListRow;
import com.linnca.pelicann.lessonlist.LessonListViewer;
import com.linnca.pelicann.lessonlist.LessonListViewerImplementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class UserProfile_ReportCardAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_NORMAL_ROW = 1;
    private final int VIEW_TYPE_HEADER = 2;
    //any integer other than these will be the average accuracy
    static final int NOT_CLEARED = -1;
    private final LessonListViewer lessonListViewer;
    private int lessonLevel;
    private List<LessonListRow> rows;
    private Map<String, UserProfile_ReportCardDataWrapper> data = new HashMap<>();
    private ReportCardListener listener;

    interface ReportCardListener {
        void onItemClicked();
    }

    UserProfile_ReportCardAdapter(int lessonLevel,
                                  ReportCardListener listener){
        lessonListViewer = new LessonListViewerImplementation();
        this.listener = listener;

        setLessons(lessonLevel);

    }

    int getLessonLevel(){
        return this.lessonLevel;
    }

    void setData(List<UserProfile_ReportCardDataWrapper> dataToSet){
        data.clear();
        for (UserProfile_ReportCardDataWrapper datum : dataToSet){
            data.put(datum.getLessonKey(), datum);
        }
        notifyDataSetChanged();
    }

    private void setLessons(int level){
        List<LessonListRow> lessonRows = lessonListViewer.getLessonsAtLevel(level);
        //don't include reviews
        for (Iterator<LessonListRow> iterator = lessonRows.iterator(); iterator.hasNext();){
            LessonListRow row = iterator.next();
            if (row.isReview()){
                iterator.remove();
            }
        }
        this.rows = new ArrayList<>(lessonRows.size()+1);
        //header view
        rows.add(new LessonListRow());
        //the data
        rows.addAll(lessonRows);

        notifyDataSetChanged();

        //save so we know what lesson level we are currently on
        this.lessonLevel = level;
    }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public int getItemCount(){return rows.size();}

    @Override
    public int getItemViewType(int position){
        if (position == 0){
            return VIEW_TYPE_HEADER;
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
                        .inflate(R.layout.inflatable_user_profile_report_card_list_item, parent, false);
                return new UserProfile_ReportCard_RowViewHolder(itemView);
            case VIEW_TYPE_HEADER:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_user_profile_report_card_header, parent, false);
                return new User_Profile_ReportCardHeaderViewHolder(itemView);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserProfile_ReportCard_RowViewHolder) {
            populateRow(position, (UserProfile_ReportCard_RowViewHolder)holder);
        } else if (holder instanceof User_Profile_ReportCardHeaderViewHolder){
            ((User_Profile_ReportCardHeaderViewHolder)holder).setOnSpinnerItemChangeListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long id) {
                            int newLevel = itemPosition + 1;
                            if (newLevel == lessonLevel)
                                return;
                            setLessons(newLevel);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    }
            );
        }
    }

    private void populateRow(int position, final UserProfile_ReportCard_RowViewHolder holder){
        LessonListRow lessonRow = rows.get(position);
        //do everything that doesn't require database connection first
        holder.setRowInfo(lessonRow);
        LessonListRow rowBefore = position == 0 ? null : rows.get(position - 1);
        LessonListRow rowAfter = position == rows.size() - 1 ? null : rows.get(position + 1);
        //don't connect reviews and the preceding/following item
        if (rowBefore != null && rowBefore.isReview())
            rowBefore = null;
        if (rowAfter != null && rowAfter.isReview())
            rowAfter = null;
        (holder).connectRows(lessonRow, rowBefore, rowAfter);
        //now connect to the database
        LessonData[] lessons = lessonRow.getLessons();
        for (int i=0; i<3; i++){
            LessonData lessonData = lessons[i];
            final int rowNumber = i+1;
            //don't do anything if there is not a lesson on that cell
            if (lessonData == null)
                continue;
            final int toClearScore = lessonData.getToClearScore();
            //if the user hasn't cleared it yet
            if (!data.containsKey(lessonData.getKey())) {
                holder.setRowData(rowNumber, NOT_CLEARED, toClearScore, listener);
                continue;
            }
            UserProfile_ReportCardDataWrapper reportCardData = data.get(lessonData.getKey());

            int total = reportCardData.getTotalCt();
            int correct = reportCardData.getCorrectCt();
            if (total == 0){
                //shouldn't happen but just in case
                holder.setRowData(rowNumber, NOT_CLEARED, toClearScore, listener);
                continue;
            }
            int averageCorrect = correct * 100 / total;
            holder.setRowData(rowNumber, averageCorrect, toClearScore, listener);
        }
    }
}
