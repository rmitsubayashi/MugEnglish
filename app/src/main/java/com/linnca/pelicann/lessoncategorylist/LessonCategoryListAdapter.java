package com.linnca.pelicann.lessoncategorylist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessonlist.LessonCategory;
public class LessonCategoryListAdapter extends RecyclerView.Adapter<LessonCategoryListViewHolder>  {
    private final int lessonLevel;
    private final List<LessonCategory> lessonCategories;
    private final LessonCategoryList.LessonCategoryListListener listener;

    LessonCategoryListAdapter(int lessonLevel, List<LessonCategory> categories,
                              LessonCategoryList.LessonCategoryListListener listener){
        this.lessonLevel = lessonLevel;
        this.lessonCategories = categories;
        this.listener = listener;
    }

    @Override
    public long getItemId(int position){ return position; }

    @Override
    public int getItemCount(){return lessonCategories.size();}

    @Override
    public LessonCategoryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_category_list_item, parent, false);
        return new LessonCategoryListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final LessonCategoryListViewHolder holder, int position) {
        LessonCategory category = lessonCategories.get(position);
        holder.setTitle(category.getTitleJP());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LessonCategory category = lessonCategories.get(holder.getAdapterPosition());
                listener.lessonCategoryListToLessonScript(category);
            }
        });
    }

}
