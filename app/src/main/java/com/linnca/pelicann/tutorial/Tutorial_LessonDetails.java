package com.linnca.pelicann.tutorial;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.widgets.ToolbarState;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Tutorial_LessonDetails extends Fragment {
    public static String BUNDLE_SELECTED_PERSON = "lessonDetailsSelectedPerson";
    public static String BUNDLE_DESCRIPTION_FEATURE_COVERED = "lessonDetailsDescriptionFeatureCovered";
    private OnboardingPersonBundle selectedPerson;
    private boolean descriptionFeatureCovered = false;

    private ViewGroup mainLayout;

    Tutorial_LessonDetailsListener listener;

    interface Tutorial_LessonDetailsListener {
        void lessonDetailsToQuestion1();
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onStart(){
        super.onStart();
        listener.setToolbarState(new ToolbarState(getString(R.string.fragment_tutorial_lesson_details_title), false, null));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedPerson = (OnboardingPersonBundle)getArguments().getSerializable(BUNDLE_SELECTED_PERSON);
        descriptionFeatureCovered = getArguments().getBoolean(BUNDLE_DESCRIPTION_FEATURE_COVERED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_tutorial_lesson_details, container, false);
        mainLayout = view.findViewById(R.id.fragment_tutorial_lesson_details);

        mainLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lblue300));

        if (descriptionFeatureCovered) {
            showLesson();
        } else {
            showDescription();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    private void implementListeners(Context context){
        try {
            listener = (Tutorial_LessonDetailsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void showLesson(){
        addLesson();
    }

    private void addLesson(){
        View lessonView = getLayoutInflater().inflate(R.layout.inflatable_lesson_details_instance_list_item, mainLayout, false);
        TextView topicsTextView = lessonView.findViewById(R.id.lesson_details_item_topics);
        TextView dateCreatedTextView = lessonView.findViewById(R.id.lesson_details_item_date_created);
        topicsTextView.setText(selectedPerson.getJapaneseName());
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
        String dateString = dateFormat.format(new Date());
        String createdLabel = getResources().getString(R.string.lesson_details_created);
        dateCreatedTextView.setText(createdLabel + ":" + dateString);

        lessonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.lessonDetailsToQuestion1();
            }
        });

        //don't need to worry about adding this to a list
        mainLayout.addView(lessonView);

        final View view = getLayoutInflater().inflate(R.layout.inflatable_tutorial_guide_end_align, mainLayout, false);
        //reusing the chat item
        TextView guideTextView = view.findViewById(R.id.guide_message);
        guideTextView.setText(R.string.tutorial_lesson_details_go_to_question);
        mainLayout.addView(view);
        view.post(new Runnable() {
            @Override
            public void run() {
                Animation hoverAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.hover);
                view.startAnimation(hoverAnimation);
            }
        });
    }

    private void showDescription(){
        //icon handled in main activity.
        //just show guide
        final View view = getLayoutInflater().inflate(R.layout.inflatable_tutorial_guide_end_align, mainLayout, false);
        //reusing the chat item
        TextView guideTextView = view.findViewById(R.id.guide_message);
        guideTextView.setText(R.string.tutorial_lesson_details_go_to_lesson_description);
        mainLayout.addView(view);
        view.post(new Runnable() {
            @Override
            public void run() {
                Animation hoverAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.hover);
                view.startAnimation(hoverAnimation);
            }
        });
    }
}
