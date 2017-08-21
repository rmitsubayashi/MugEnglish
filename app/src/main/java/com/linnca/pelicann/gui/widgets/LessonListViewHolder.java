package com.linnca.pelicann.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.List;

//holder for user interest list cells
public class LessonListViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;
    private final List<ImageView> stars = new ArrayList<>();

    public LessonListViewHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.lesson_list_item_text);
        stars.add((ImageView) itemView.findViewById(R.id.lesson_list_item_star1));
        stars.add((ImageView) itemView.findViewById(R.id.lesson_list_item_star2));
        stars.add((ImageView) itemView.findViewById(R.id.lesson_list_item_star3));

    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public List<ImageView> getStars(){
        return this.stars;
    }
}