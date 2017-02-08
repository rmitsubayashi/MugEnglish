package com.example.ryomi.myenglish.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;

//holder for user interest list cells
public class ThemeDetailsViewHolder  extends RecyclerView.ViewHolder{
    private final TextView topics;
    private final TextView created;

    public ThemeDetailsViewHolder(View itemView) {
        super(itemView);
        topics = (TextView) itemView.findViewById(R.id.theme_details_item_topics);
        created = (TextView) itemView.findViewById(R.id.theme_details_item_date_created);

    }

    public void setTopics(String text){
        topics.setText(text);
    }

    public void setCreated(String text){
        created.setText(text);
    }


}