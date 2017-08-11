package com.example.ryomi.mugenglish.gui.widgets;


import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.datawrappers.LessonInstanceData;
import com.example.ryomi.mugenglish.questionmanager.QuestionManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LessonDetailsAdapter
        extends FirebaseRecyclerAdapter<LessonInstanceData, LessonDetailsViewHolder> {

    //context menu doesn't work for recyclerviews
    private LessonInstanceData longClickData;
    //hide loading view once data is initially loaded
    private ProgressBar loading;
    //update GUI if the list is empty
    private TextView noItems;

    public LessonDetailsAdapter(DatabaseReference ref, TextView noItems, ProgressBar loading){
        super(LessonInstanceData.class, R.layout.inflatable_lesson_details_instance_list_item,
                LessonDetailsViewHolder.class, ref);
        this.noItems = noItems;
        this.loading = loading;
    }

    public LessonInstanceData getLongClickPosition(){
        return longClickData;
    }

    @Override
    public void populateViewHolder(LessonDetailsViewHolder holder, final LessonInstanceData data, int position) {
        String topics = "";
        for (String topic : data.getTopics()){
            topics += topic + " + ";
        }
        topics = topics.substring(0,topics.length()-3);
        holder.setTopics(topics);

        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
        String dateString = dateFormat.format(new Date(data.getCreatedTimestamp()));
        String createdLabel = holder.itemView.getContext().
                getResources().getString(R.string.lesson_details_created);
        holder.setCreated(createdLabel + ": " + dateString);

        //set action listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionManager manager = QuestionManager.getInstance();
                manager.startQuestions(data, (Activity)view.getContext());
            }
        });

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
