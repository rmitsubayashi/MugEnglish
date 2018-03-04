package com.linnca.pelicann.lessondetails;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

class LessonScriptSpeakerImagesViewHolder extends RecyclerView.ViewHolder {
    private final LinearLayout list;
    //this should also behave like a recycler view item

    LessonScriptSpeakerImagesViewHolder(View itemView){
        super(itemView);
        list = itemView.findViewById(R.id.lesson_script_speaker_images_layout);
    }

    void setSpeakerCards(List<ScriptSpeaker> speakers, List<Integer> colorAttrIDs){
        int currentIndex = 0;
        int speakerSize = speakers.size();
        for (int i=0; i<speakerSize; i++) {
            ScriptSpeaker speaker = speakers.get(i);
            //just in case we have less colors than speakers
            int colorAttrID = colorAttrIDs.size() <= i ?
                    colorAttrIDs.get(colorAttrIDs.size()-1) :
                    colorAttrIDs.get(i);
            View card;
            int numberOfCards = list.getChildCount();
            if (currentIndex >= numberOfCards) {
                card = LayoutInflater.from(itemView.getContext()).inflate(
                        R.layout.inflatable_lesson_script_speaker_card, list, false
                );
                list.addView(card);
            } else {
                card = list.getChildAt(currentIndex);
            }

            populateCard(speaker, colorAttrID, card);

            currentIndex++;
        }

        //remove extra views
        int lastViewIndex = list.getChildCount()-1;
        for (int i=lastViewIndex; i >= currentIndex; i--){
            list.removeViewAt(i);
        }
    }

    private void populateCard(ScriptSpeaker speaker, int colorAttrID, View card){
        ImageView speakerImageView = card.findViewById(R.id.lesson_script_speaker_card_image);
        final ProgressBar speakerImageLoading = card.findViewById(R.id.lesson_script_speaker_card_image_loading);
        TextView nameTextView = card.findViewById(R.id.lesson_script_speaker_card_name);
        TextView nameTranslationTextView = card.findViewById(R.id.lesson_script_speaker_card_name_translation);

        int color = ThemeColorChanger.getColorFromAttribute(colorAttrID, itemView.getContext());
        if (speaker.getImageURL() == null || speaker.getImageURL().equals("") ||
                speaker.getImageURL().equals(ScriptSpeaker.IMAGE_NONE)){
            speakerImageView.setImageResource(R.drawable.ic_person);
            speakerImageView.setColorFilter(color);
            speakerImageLoading.setVisibility(View.GONE);
        } else {
            speakerImageLoading.setVisibility(View.VISIBLE);
            GlideApp.with(itemView.getContext())
                    .load(speaker.getImageURL())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            speakerImageLoading.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(speakerImageView)
            ;
        }
        Translation name = speaker.getName();
        nameTextView.setText(name.getJapanese());
        nameTranslationTextView.setText(name.getEnglish());
        nameTextView.setTextColor(color);
        nameTranslationTextView.setTextColor(color);
    }
}
