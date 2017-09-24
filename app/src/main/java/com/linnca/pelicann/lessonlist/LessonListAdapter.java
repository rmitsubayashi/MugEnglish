package com.linnca.pelicann.lessonlist;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.lessondetails.LessonData;

import java.util.List;

class LessonListAdapter
        extends RecyclerView.Adapter<LessonListViewHolder> {
    private final List<LessonData> data;
    private final LessonList.LessonListListener listener;

    LessonListAdapter(List<LessonData> lessons, LessonList.LessonListListener listener){
        this.data = lessons;
        this.listener = listener;
    }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public int getItemCount(){return data.size();}

    @Override
    public LessonListViewHolder onCreateViewHolder(ViewGroup parent, int viewtype){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflatable_lesson_list_list_item, parent, false);
        return new LessonListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LessonListViewHolder holder, int position) {
        final LessonData lessonData = data.get(position);
        final Context fContext = holder.itemView.getContext();
        final int[] blueShades = {
                ContextCompat.getColor(fContext, R.color.lblue300),
                ContextCompat.getColor(fContext, R.color.lblue500),
                ContextCompat.getColor(fContext, R.color.lblue700)};

        holder.setText(lessonData.getTitle());

        final int fPosition = position;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int colorPos = fPosition % (blueShades.length);
                int color = blueShades[colorPos];
                listener.lessonListToLessonDetails(lessonData, color);
            }
        });
    }


}
