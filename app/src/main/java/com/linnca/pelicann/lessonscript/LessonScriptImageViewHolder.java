package com.linnca.pelicann.lessonscript;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.linnca.pelicann.R;

class LessonScriptImageViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final ProgressBar loading;

    LessonScriptImageViewHolder(View itemView){
        super(itemView);
        imageView = itemView.findViewById(R.id.lesson_script_image);
        loading = itemView.findViewById(R.id.lesson_script_image_loading);
    }

    void setImage(String imageURL){
        if (imageURL != null && !imageURL.equals("")){
            loading.setVisibility(View.VISIBLE);
            GlideApp.with(itemView.getContext())
                    .load(imageURL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            loading.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView)
            ;
        } else {
            imageView.setImageDrawable(null);
        }

    }
}
