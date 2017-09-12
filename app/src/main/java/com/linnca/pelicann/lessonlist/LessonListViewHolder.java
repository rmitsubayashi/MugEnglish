package com.linnca.pelicann.lessonlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.List;

//holder for user interest list cells
class LessonListViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;
    private final List<ImageView> stars = new ArrayList<>();

    LessonListViewHolder(View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.lesson_list_item_text);
        stars.add((ImageView) itemView.findViewById(R.id.lesson_list_item_star1));
        stars.add((ImageView) itemView.findViewById(R.id.lesson_list_item_star2));
        stars.add((ImageView) itemView.findViewById(R.id.lesson_list_item_star3));

    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public int[] getTextViewLocation(){
        int[] coords = new int[2];
        text.getLocationInWindow(coords);
        return coords;
    }

    List<ImageView> getStars(){
        return this.stars;
    }
}