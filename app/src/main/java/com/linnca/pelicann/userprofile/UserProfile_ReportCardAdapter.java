package com.linnca.pelicann.userprofile;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.FirebaseDBHeaders;
import com.linnca.pelicann.lessondetails.LessonData;
import com.linnca.pelicann.lessonlist.LessonHierarchyViewer;
import com.linnca.pelicann.lessonlist.LessonListRow;

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
    private final LessonHierarchyViewer lessonHierarchyViewer;
    private int lessonLevel;
    private List<LessonListRow> data;
    private final Map<String, LessonData> allLessons = new HashMap<>();
    private final Map<String, Integer> allLessonsRowPosition = new HashMap<>();

    private class DataReferencePair {
        DatabaseReference ref;
        ValueEventListener listener;

        DataReferencePair(DatabaseReference ref, ValueEventListener listener) {
            this.ref = ref;
            this.listener = listener;
        }
    }
    private List<DataReferencePair> allDatabaseListeners = new ArrayList<>();

    private FirebaseDatabase db;
    private String userID;

    private ReportCardListener listener;

    interface ReportCardListener {
        void onItemClicked();
    }

    UserProfile_ReportCardAdapter(int lessonLevel,
                                  String userID, ReportCardListener listener){
        lessonHierarchyViewer = new LessonHierarchyViewer();
        db = FirebaseDatabase.getInstance();
        this.userID = userID;
        this.listener = listener;

        setLessons(lessonLevel);

    }

    private void setLessons(int level){
        //remove any listeners currently listening
        removeValueEventListeners();

        List<LessonListRow> lessonRows = lessonHierarchyViewer.getLessonsAtLevel(level);
        //don't include reviews
        for (Iterator<LessonListRow> iterator = lessonRows.iterator(); iterator.hasNext();){
            LessonListRow row = iterator.next();
            if (row.isReview()){
                iterator.remove();
            }
        }
        this.data = new ArrayList<>(lessonRows.size()+1);
        //header view
        data.add(new LessonListRow());
        //the data
        data.addAll(lessonRows);

        populateMap();

        notifyDataSetChanged();

        //save so we know what lesson level we are currently on
        this.lessonLevel = level;
    }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public int getItemCount(){return data.size();}

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
        LessonListRow lessonRow = data.get(position);
        //do everything that doesn't require database connection first
        holder.setRowInfo(lessonRow);
        LessonListRow rowBefore = position == 0 ? null : data.get(position - 1);
        LessonListRow rowAfter = position == data.size() - 1 ? null : data.get(position + 1);
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
            DatabaseReference scoreRef = db.getReference(
                    FirebaseDBHeaders.REPORT_CARD + "/" +
                            userID + "/" +
                            lessonData.getKey()
            );
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long total = dataSnapshot.child(FirebaseDBHeaders.REPORT_CARD_TOTAL)
                            .getValue(Long.class);
                    Long correct = dataSnapshot.child(FirebaseDBHeaders.REPORT_CARD_CORRECT)
                            .getValue(Long.class);
                    if (total == null || correct == null){
                        holder.setRowData(rowNumber, NOT_CLEARED, toClearScore, listener);
                        return;
                    }

                    int averageCorrect = (int)(correct * 100 / total);
                    holder.setRowData(rowNumber, averageCorrect, toClearScore, listener);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            scoreRef.addListenerForSingleValueEvent(valueEventListener);
            allDatabaseListeners.add(new DataReferencePair(scoreRef, valueEventListener));
        }
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

    void removeValueEventListeners(){
        for (DataReferencePair pair : allDatabaseListeners){
            pair.ref.removeEventListener(pair.listener);
        }
        allDatabaseListeners.clear();
    }
}
