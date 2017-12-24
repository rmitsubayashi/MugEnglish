package com.linnca.pelicann.lessondetails;

import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//holder for user interest list cells
class LessonDetailsViewHolder extends RecyclerView.ViewHolder{
    private final TextView interestsLabel;
    private final TextView created;

    LessonDetailsViewHolder(View itemView) {
        super(itemView);
        interestsLabel = itemView.findViewById(R.id.lesson_details_item_topics);
        created = itemView.findViewById(R.id.lesson_details_item_date_created);

    }

    void setInterestsLabel(Collection<String> interestLabels, int position) {
        StringBuilder allInterestsLabelBuilder = new StringBuilder();
        List<Integer> plusIndexes = new ArrayList<>(interestLabels.size());
        int allInterestLength = 0;
        for (String interestLabel : interestLabels) {
            allInterestsLabelBuilder.append(interestLabel);
            allInterestLength+= interestLabel.length();
            allInterestsLabelBuilder.append(" ・ ");
            plusIndexes.add(allInterestLength + 1);
            allInterestLength += 3;
        }
        String allInterestsLabel = allInterestsLabelBuilder.toString();
        if (allInterestsLabel.equals("")) {
            allInterestsLabel = Integer.toString(position + 1);
        } else {
            allInterestsLabel = allInterestsLabel.substring(0, allInterestsLabel.length() - 3);
            plusIndexes.remove(plusIndexes.size()-1);
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(allInterestsLabel);
        for (Integer plusIndex : plusIndexes){
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                    ThemeColorChanger.getColorFromAttribute(R.attr.color500, itemView.getContext())
            );
            spannableStringBuilder.setSpan(colorSpan, plusIndex, plusIndex+1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        interestsLabel.setText(spannableStringBuilder);

    }

    void setCreated(String text){
        created.setText(text);
    }


}