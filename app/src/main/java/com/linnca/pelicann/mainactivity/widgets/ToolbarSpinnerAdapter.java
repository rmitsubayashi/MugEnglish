package com.linnca.pelicann.mainactivity.widgets;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.List;

public class ToolbarSpinnerAdapter extends ArrayAdapter<ToolbarSpinnerItem> {
    public static final int FILTER_ALL = 0;
    public static final int FILTER_PERSON = 1;
    public static final int FILTER_PLACE = 2;
    public static final int FILTER_OTHER = 3;

    private final List<ToolbarSpinnerItem> list;
    private final LayoutInflater layoutInflater;
    public ToolbarSpinnerAdapter(Activity context, List<ToolbarSpinnerItem> list){
        super(context, R.layout.inflatable_toolbar_spinner_top_item, list);
        layoutInflater = context.getLayoutInflater();
        this.list = list;

    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.inflatable_toolbar_spinner_top_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.tool_bar_spinner_item_text);
        ImageView imageView = convertView.findViewById(R.id.tool_bar_spinner_item_icon);
        ToolbarSpinnerItem item = list.get(position);
        textView.setText(item.getText());
        imageView.setImageResource(item.getImageID());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.inflatable_toolbar_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.tool_bar_spinner_item_text);
        ImageView imageView = convertView.findViewById(R.id.tool_bar_spinner_item_icon);
        ToolbarSpinnerItem item = list.get(position);
        textView.setText(item.getText());
        imageView.setImageResource(item.getImageID());

        return convertView;
    }

    public static boolean isSpinnerState(int state){
        return  state == FILTER_ALL ||
                state == FILTER_PERSON ||
                state == FILTER_PLACE ||
                state == FILTER_OTHER
                ;
    }

    public static String getSpinnerStateIdentifier(int state){
        switch (state){
            case FILTER_ALL:
                return "all";
            case FILTER_PERSON:
                return "person";
            case FILTER_PLACE:
                return "place";
            case FILTER_OTHER:
                return "other";
            default:
                return "null";
        }
    }
}
