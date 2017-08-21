package com.linnca.pelicann.gui.widgets;


import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.LessonInstanceData;
import com.linnca.pelicann.gui.LessonDetails;
import com.linnca.pelicann.questionmanager.QuestionManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonDetailsAdapter
        extends FirebaseRecyclerAdapter<LessonInstanceData, LessonDetailsViewHolder> {

    //context menu doesn't work for recyclerviews
    private DatabaseReference longClickRef;
    //hide loading view once data is initially loaded
    private ProgressBar loading;
    //update GUI if the list is empty
    private TextView noItems;
    //so we can click on an item and start questions
    private LessonDetails.LessonDetailsListener lessonDetailsListener;

    public LessonDetailsAdapter(DatabaseReference ref, TextView noItems, ProgressBar loading, LessonDetails.LessonDetailsListener lessonDetailsListener){
        super(LessonInstanceData.class, R.layout.inflatable_lesson_details_instance_list_item,
                LessonDetailsViewHolder.class, ref);
        this.noItems = noItems;
        this.loading = loading;
        this.lessonDetailsListener = lessonDetailsListener;
    }

    public DatabaseReference getLongClickPosition(){
        return longClickRef;
    }

    @Override
    public void populateViewHolder(final LessonDetailsViewHolder holder, final LessonInstanceData data, final int position) {
        String allInterestsLabel = "";
        for (String interestLabel : data.getInterestLabels()) {
            allInterestsLabel += interestLabel + " + ";
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
                lessonDetailsListener.lessonDetailsToQuestions(data);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickRef = getRef(position);
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
