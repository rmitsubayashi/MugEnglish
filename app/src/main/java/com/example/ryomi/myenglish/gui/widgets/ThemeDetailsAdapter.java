package com.example.ryomi.myenglish.gui.widgets;


import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ThemeDetailsAdapter
        extends FirebaseRecyclerAdapter<ThemeInstanceData, ThemeDetailsViewHolder> {

    //context menu doesn't work for recyclerviews
    ThemeInstanceData longClickData;
    //hide loading view once data is initially loaded
    ProgressBar loading;
    //update GUI if the list is empty
    TextView noItems;

    public ThemeDetailsAdapter(DatabaseReference ref, TextView noItems, ProgressBar loading){
        super(ThemeInstanceData.class, R.layout.inflatable_theme_details_instance_list_item,
                ThemeDetailsViewHolder.class, ref);
        this.noItems = noItems;
        this.loading = loading;
    }

    public ThemeInstanceData getLongClickPosition(){
        return longClickData;
    }

    @Override
    public void populateViewHolder(ThemeDetailsViewHolder holder, final ThemeInstanceData data, int position) {
        String topics = "";
        for (String topic : data.getTopics()){
            topics += topic + " + ";
        }
        topics = topics.substring(0,topics.length()-3);
        holder.setTopics(topics);

        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
        String dateString = dateFormat.format(new Date(data.getCreatedTimestamp()));
        String createdLabel = holder.itemView.getContext().
                getResources().getString(R.string.theme_details_created);
        holder.setCreated(createdLabel + ": " + dateString);

        //set action listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionManager manager = QuestionManager.getInstance();
                manager.startQuestions(data);
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
