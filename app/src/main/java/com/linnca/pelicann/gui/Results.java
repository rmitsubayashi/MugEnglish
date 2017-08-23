package com.linnca.pelicann.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.AchievementStars;
import com.linnca.pelicann.db.datawrappers.InstanceRecord;
import com.linnca.pelicann.db.datawrappers.QuestionAttempt;
import com.linnca.pelicann.questionmanager.ResultsManager;

import java.util.List;

//after we are finished with the questions,
//we redirect to this fragment
// and save everything in the database

public class Results extends Fragment {
    public static final String BUNDLE_INSTANCE_RECORD = "bundleInstanceRecord";
    private InstanceRecord instanceRecord;
    private ResultsManager resultsManager;
    private TextView correctCtTextView;
    private LinearLayout newStarsLayout;
    private Button finishButton;
    private Button reviewButton;

    private ResultsListener resultsListener;

    interface ResultsListener {
        void resultsToLessonCategories();
        void resultsToReview(InstanceRecord instanceRecord);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceRecord = (InstanceRecord) getArguments().getSerializable(BUNDLE_INSTANCE_RECORD);
        resultsManager = new ResultsManager(instanceRecord, new ResultsManager.ResultsManagerListener() {
            @Override
            public void onAchievementsSaved(AchievementStars existingAchievements, AchievementStars newAchievements) {
                populateNewStars(existingAchievements, newAchievements);
            }
        });
        //this won't affect when we check for new achievements
        resultsManager.saveInstanceRecord();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        newStarsLayout = view.findViewById(R.id.results_new_star_layout);
        correctCtTextView = view.findViewById(R.id.results_questions_correct);
        reviewButton = view.findViewById(R.id.results_review);
        finishButton = view.findViewById(R.id.results_finish);
        //do this here because this updates the layout
        resultsManager.identifyAchievements();
        setLayout();
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
            resultsListener = (ResultsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    public void populateNewStars(AchievementStars existingAchievements, AchievementStars newAchievements){
        boolean show = false;
        //basically check if old = false and new = true
        if (!existingAchievements.getFirstInstance() && newAchievements.getFirstInstance()) {
            show = true;
            LinearLayout listItem = (LinearLayout)getLayoutInflater()
                    .inflate(R.layout.inflatable_results_achievement_bubble, newStarsLayout, false);
            TextView listItemText = listItem.findViewById(R.id.results_achievement_text);
            listItemText.setText(getString(R.string.results_star_earned_template, getString(R.string.results_star_first_instance)));
            newStarsLayout.addView(listItem);
        }
        if (!existingAchievements.getSecondInstance() && newAchievements.getSecondInstance()) {
            show = true;
            LinearLayout listItem = (LinearLayout)getLayoutInflater()
                    .inflate(R.layout.inflatable_results_achievement_bubble, newStarsLayout, false);
            TextView listItemText = listItem.findViewById(R.id.results_achievement_text);
            listItemText.setText(getString(R.string.results_star_earned_template, getString(R.string.results_star_second_instance)));
            newStarsLayout.addView(listItem);
        }
        if (!existingAchievements.getRepeatInstance() && newAchievements.getRepeatInstance()) {
            show = true;
            LinearLayout listItem = (LinearLayout)getLayoutInflater()
                    .inflate(R.layout.inflatable_results_achievement_bubble, newStarsLayout, false);
            TextView listItemText = (TextView)listItem.findViewById(R.id.results_achievement_text);
            listItemText.setText(getString(R.string.results_star_earned_template, getString(R.string.results_star_repeat_instance)));
            newStarsLayout.addView(listItem);
        }

        if (show){
            newStarsLayout.setVisibility(View.VISIBLE);
        }

    }

    private void setLayout(){
        populateCorrectCount();
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultsListener.resultsToLessonCategories();
            }
        });

        boolean needToReview = false;
        for (QuestionAttempt attempt : instanceRecord.getAttempts()){
            if (!attempt.getCorrect()){
                needToReview = true;
                break;
            }
        }
        if (needToReview){
            reviewButton.setVisibility(View.VISIBLE);
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resultsListener.resultsToReview(instanceRecord);
                }
            });
            //change the layout of the finish button to recommend review
            // (make it borderless)
            finishButton.setBackgroundResource(R.drawable.transparent_button);
            finishButton.setTextColor(ContextCompat.getColor(getContext(), R.color.lblue500));
            finishButton.setText(R.string.results_finish_review);
        }
    }

    private void populateCorrectCount(){
        String tempQuestionID = "";
        int correctCt = 0;
        int totalCt = 0;
        List<QuestionAttempt> attempts = instanceRecord.getAttempts();
        for (QuestionAttempt attempt : attempts){
            String questionID = attempt.getQuestionID();
            //there can only be one correct answer per question.
            // (there can be multiple incorrect answers)
            if (attempt.getCorrect())
                correctCt++;
            if (!tempQuestionID.equals(questionID)){
                totalCt++;
                tempQuestionID = questionID;
            }
        }
        String displayText = Integer.toString(correctCt) + " / " + Integer.toString(totalCt);
        correctCtTextView.setText(displayText);
        //change text color based on accuracy (the user can edit border line??)
        double correctPercentage = (double)correctCt / (double)totalCt;
        if (correctPercentage > 0.7){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lgreen500));
        } else if (correctPercentage > 0.5){
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange500));
        } else {
            correctCtTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red500));
        }
    }
}
