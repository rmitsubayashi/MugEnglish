package com.linnca.pelicann.lessondetails;


import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
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
    //hide loading view once data is initially loaded
    private final ProgressBar loading;
    //update GUI if the list is empty
    private final TextView noItems;
    //so we can click on an item and start questions
    private final LessonDetails.LessonDetailsListener lessonDetailsListener;
    //need it for the listener
    private final String lessonKey;

    public LessonDetailsAdapter(DatabaseReference ref, TextView noItems, ProgressBar loading,
                                LessonDetails.LessonDetailsListener lessonDetailsListener, String lessonKey){
        super(LessonInstanceData.class, R.layout.inflatable_lesson_details_instance_list_item,
                LessonDetailsViewHolder.class, ref);
        this.noItems = noItems;
        this.loading = loading;
        this.lessonDetailsListener = lessonDetailsListener;
        this.lessonKey = lessonKey;
    }

    public LessonInstanceData getLongClickPositionData(){
        return longClickData;
    }

    @Override
    public void populateViewHolder(final LessonDetailsViewHolder holder, final LessonInstanceData data, int position) {
        String allInterestsLabel = "";
        Set<String> duplicates = new HashSet<>(data.getInterestLabels().size());
        for (String interestLabel : data.getInterestLabels()) {
            if (!duplicates.contains(interestLabel)) {
                allInterestsLabel += interestLabel + " + ";
            }
            duplicates.add(interestLabel);
        }
        allInterestsLabel = allInterestsLabel.substring(0,allInterestsLabel.length()-3);
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
        noItems.setVisibility(this.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
        loading.setVisibility(View.INVISIBLE);
    }


}
