package com.example.ryomi.myenglish.gui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;

import java.util.ArrayList;
import java.util.List;

public class ThemeGridAdapter extends BaseAdapter {
    private Context context;
    private List<ThemeCellData> themeCellData;
    private LayoutInflater layoutInflator;

    static class ViewHolder{
        List<ImageView> starsImageViews = new ArrayList<>();
        ImageView mainImageView;
        TextView name;
    }

    public ThemeGridAdapter(Context c, List<ThemeCellData> themeCells) {
        context = c;
        this.themeCellData = new ArrayList<>(themeCells);
        layoutInflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return themeCellData.size();
    }

    @Override
    public ThemeCellData getItem(int position) {
        return themeCellData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflator.inflate(R.layout.inflatable_theme_list_list_item, null);
            holder.mainImageView = (ThemeCellImageView)convertView.findViewById(R.id.themeGrid_mainImage);

            holder.starsImageViews.add((ImageView)convertView.findViewById(R.id.themeGrid_star1));
            holder.starsImageViews.add((ImageView)convertView.findViewById(R.id.themeGrid_star2));
            holder.starsImageViews.add((ImageView)convertView.findViewById(R.id.themeGrid_star3));

            holder.name = (TextView)convertView.findViewById(R.id.themeGrid_text);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder)(convertView.getTag());
        }

        //cell data holds info about UI appearance
        ThemeCellData cellData = themeCellData.get(position);
        //theme data holds info about content of the theme
        ThemeData themeData = themeCellData.get(position).getThemeData();
        //stars
        List<Boolean> starList = cellData.getStarList();
        int starCt = starList.size();
        for(int i=0; i<starCt; i++) {
            ImageView starImageView = holder.starsImageViews.get(i);
            boolean starEnabled = starList.get(i);
            if (starEnabled) {
                starImageView.setImageResource(R.drawable.star);
            } else {
                starImageView.setImageResource(R.drawable.star_disabled);
            }
        }

        //main image
        String imageString = themeData.getImage();
        int imageID = context.getResources().getIdentifier(imageString, "drawable",
                context.getApplicationInfo().packageName);
        holder.mainImageView.setImageResource(imageID);

        //name of cell
        holder.name.setText(themeData.getCategory() + " " + (position+1));

        //background color
        convertView.setBackgroundColor(Color.parseColor(cellData.getColorString()));

        return convertView;
    }
}
