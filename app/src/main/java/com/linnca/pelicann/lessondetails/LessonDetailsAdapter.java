package com.linnca.pelicann.lessondetails;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.linnca.pelicann.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class LessonDetailsAdapter
        extends FirebaseRecyclerAdapter<LessonInstanceData, LessonDetailsViewHolder> {

    //context menu doesn't work for recyclerviews
    private LessonInstanceData longClickData;
    //so we can click on an item and start questions
    private final LessonDetails.LessonDetailsListener lessonDetailsListener;
    //need it for the listener
    private final String lessonKey;
    //helps change the UI when items are loaded
    private LessonDetailsAdapterListener uiListener;

    interface LessonDetailsAdapterListener {
        void onLoad();
        void onItems();
        void onNoItems();
    }

    LessonDetailsAdapter(FirebaseRecyclerOptions<LessonInstanceData> options, LessonDetailsAdapterListener uiListener,
                         LessonDetails.LessonDetailsListener lessonDetailsListener, String lessonKey){
        super(options);
        this.uiListener = uiListener;
        this.lessonDetailsListener = lessonDetailsListener;
        this.lessonKey = lessonKey;
    }

    LessonInstanceData getLongClickPositionData(){
        return longClickData;
    }

    @Override
    public LessonDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inflatable_lesson_details_instance_list_item, parent, false);
        return new LessonDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LessonDetailsViewHolder holder, int position, final LessonInstanceData data) {
        String allInterestsLabel = "";
        Set<String> duplicates = new HashSet<>(data.getInterestLabels().size());
        for (String interestLabel : data.getInterestLabels()) {
            if (!duplicates.contains(interestLabel)) {
                allInterestsLabel += interestLabel + " + ";
            }
            duplicates.add(interestLabel);
        }
        if (allInterestsLabel.equals("")){
            allInterestsLabel = Integer.toString(position + 1);
        } else {
            allInterestsLabel = allInterestsLabel.substring(0, allInterestsLabel.length() - 3);
        }
        holder.setInterestsLabel(allInterestsLabel);

        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
        String dateString = dateFormat.format(new Date(data.getCreatedTimeStamp()));
        String createdLabel = holder.itemView.getContext().
                getResources().getString(R.string.lesson_details_created);
        holder.setCreated(createdLabel + ": " + dateString);

        //set action listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lessonDetailsListener.lessonDetailsToQuestions(data, lessonKey);
            }
        });

        //final DatabaseReference ref = getItem(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickData = data;
                //returning false so we can catch the onlongclicklistener of the parent
                return false;
            }
        });
    }

    @Override
    public void onDataChanged(){
        if (this.getItemCount() == 0){
            uiListener.onNoItems();
        } else {
            uiListener.onItems();
        }
        uiListener.onLoad();
    }


}
