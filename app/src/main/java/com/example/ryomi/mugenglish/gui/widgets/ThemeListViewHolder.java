package com.example.ryomi.mugenglish.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ryomi.mugenglish.R;

import java.util.ArrayList;
import java.util.List;

//holder for user interest list cells
public class ThemeListViewHolder  extends RecyclerView.ViewHolder {
    private final TextView text;
    private final ImageView icon;
    private final List<ImageView> stars = new ArrayList<>();

    public ThemeListViewHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.theme_list_item_text);
        icon = (ImageView) itemView.findViewById(R.id.theme_list_item_icon);
        stars.add((ImageView) itemView.findViewById(R.id.theme_list_item_star1));
        stars.add((ImageView) itemView.findViewById(R.id.theme_list_item_star2));
        stars.add((ImageView) itemView.findViewById(R.id.theme_list_item_star3));

    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setIcon(int iconID) {
        this.icon.setImageResource(iconID);
    }

    public void setIconColor(int colorID) { this.icon.setColorFilter(colorID);}

    public List<ImageView> getStars(){
        return this.stars;
    }
}