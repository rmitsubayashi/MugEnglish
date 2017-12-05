package com.linnca.pelicann.lessondetails;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.GUIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class LessonDetailsAdapter
        extends RecyclerView.Adapter<LessonDetailsViewHolder> {
    private final List<LessonInstanceData> allInstances = new ArrayList<>();
    //context menu doesn't work for recyclerviews
    private LessonInstanceData longClickData;
    //so we can click on an item and start questions
    private final LessonDetails.LessonDetailsListener lessonDetailsListener;
    //need it for the listener
    private final String lessonKey;
    //helps change the UI when items are loaded
    private LessonDetailsAdapterListener uiListener;

    interface LessonDetailsAdapterListener {
        //we are showing/hiding UI components on the main layout
        // instead of creating an extra view type with an empty state
        // because we want to position the empty state components relative
        // to components on the main layout
        void onItems();
        void onNoItems();
    }

    LessonDetailsAdapter(LessonDetailsAdapterListener uiListener,
                         LessonDetails.LessonDetailsListener lessonDetailsListener, String lessonKey){
        super();
        this.uiListener = uiListener;
        this.lessonDetailsListener = lessonDetailsListener;
        this.lessonKey = lessonKey;
    }

    LessonInstanceData getLongClickPositionData(){
        return longClickData;
    }

    @Override
    public int getItemCount(){
        return allInstances.size();
    }

    void setLessonInstances(List<LessonInstanceData> updatedInstances){
        List<LessonInstanceData> oldInstances = new ArrayList<>(allInstances);
        allInstances.clear();
        allInstances.addAll(updatedInstances);

        //animate changes
        if (oldInstances.size() > updatedInstances.size()){
            List<Integer> toRemove = GUIUtils.getItemIndexesToRemove(oldInstances, updatedInstances);
            for (Integer index : toRemove){
                notifyItemRemoved(index);
            }
        } else if (oldInstances.size() < updatedInstances.size()){
            List<Integer> toAdd = GUIUtils.getItemIndexesToAdd(oldInstances, updatedInstances);
            for (Integer index : toAdd){
                notifyItemInserted(index);
            }
        }

        //we should handle the empty state by displaying instructions on what to do
        if (this.getItemCount() == 0){
            uiListener.onNoItems();
        } else {
            uiListener.onItems();
        }
    }

    @Override
    public LessonDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inflatable_lesson_details_instance_list_item, parent, false);
        final LessonDetailsViewHolder holder = new LessonDetailsViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lessonDetailsListener.lessonDetailsToQuestions(
                        allInstances.get(holder.getAdapterPosition()),
                        lessonKey
                );
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickData = allInstances.get(holder.getAdapterPosition());
                //returning false so we can catch the onlongclicklistener of the parent
                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final LessonDetailsViewHolder holder, int position) {
        LessonInstanceData data = allInstances.get(position);
        StringBuilder allInterestsLabelBuilder = new StringBuilder();
        for (String interestLabel : data.uniqueInterestLabels()) {
                allInterestsLabelBuilder.append(interestLabel);
                allInterestsLabelBuilder.append(" + ");
        }
        String allInterestsLabel = allInterestsLabelBuilder.toString();
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
    }


}
