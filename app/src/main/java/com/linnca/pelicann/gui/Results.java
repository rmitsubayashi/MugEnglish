package com.linnca.pelicann.gui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.AchievementStars;
import com.linnca.pelicann.questionmanager.QuestionManager;
import com.linnca.pelicann.questionmanager.ResultsManager;

public class Results extends AppCompatActivity {
    private LinearLayout questionRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Toolbar appBar = (Toolbar) findViewById(R.id.results_tool_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //questionRecord = (LinearLayout)findViewById(R.id.results_question_record);

        //ResultsManager resultsManager = QuestionManager.getInstance().getResultsManager();
        //resultsManager.displayResults(this);
    }

    public void populateNewStars(AchievementStars existingAchievements, AchievementStars newAchievements){
        boolean show = false;
        LinearLayout list = (LinearLayout)findViewById(R.id.results_new_star_earned);
        //basically check if old = false and new = true
        if (!existingAchievements.getFirstInstance() && newAchievements.getFirstInstance()) {
            show = true;
            LinearLayout listItem = (LinearLayout)getLayoutInflater()
                    .inflate(R.layout.inflatable_results_achievement, list, false);
            TextView listItemText = (TextView)listItem.findViewById(R.id.results_achievement_text);
            listItemText.setText(R.string.results_star_first_instance);
            list.addView(listItem);
        }
        if (!existingAchievements.getSecondInstance() && newAchievements.getSecondInstance()) {
            show = true;
            LinearLayout listItem = (LinearLayout)getLayoutInflater()
                    .inflate(R.layout.inflatable_results_achievement, list, false);
            TextView listItemText = (TextView)listItem.findViewById(R.id.results_achievement_text);
            listItemText.setText(R.string.results_star_second_instance);
            list.addView(listItem);
        }
        if (!existingAchievements.getRepeatInstance() && newAchievements.getRepeatInstance()) {
            show = true;
            LinearLayout listItem = (LinearLayout)getLayoutInflater()
                    .inflate(R.layout.inflatable_results_achievement, list, false);
            TextView listItemText = (TextView)listItem.findViewById(R.id.results_achievement_text);
            listItemText.setText(R.string.results_star_repeat_instance);
            list.addView(listItem);
        }

        if (show){
            TextView notification = (TextView) findViewById(R.id.results_new_star_earned_notification);
            notification.setVisibility(View.VISIBLE);
            list.setVisibility(View.VISIBLE);
        }

    }

    /*public void addQuestion(String question){
        TextView view = (TextView)getLayoutInflater().inflate(R.layout.inflatable_results_question, questionRecord, false);
        view.setText(question);

        questionRecord.addView(view);
    }

    public void addResponse(String response, boolean correct){
        TextView view = (TextView)getLayoutInflater().inflate(R.layout.inflatable_results_response, questionRecord, false);
        view.setText(response);

        questionRecord.addView(view);
    }*/

    public void populateCorrectCount(int correctCt, int totalCt){
        String displayText = Integer.toString(correctCt) + " / " + Integer.toString(totalCt);
        TextView displayTextView = (TextView)findViewById(R.id.results_questions_correct);
        displayTextView.setText(displayText);
        double correctPercentage = (double)correctCt / (double)totalCt;
        if (correctPercentage > 0.5){
            displayTextView.setTextColor(ContextCompat.getColor(this, R.color.lgreen500));
        } else {
            displayTextView.setTextColor(ContextCompat.getColor(this, R.color.red500));
        }
    }
}
