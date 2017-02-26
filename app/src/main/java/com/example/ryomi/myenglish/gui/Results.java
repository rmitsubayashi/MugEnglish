package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.example.ryomi.myenglish.questionmanager.ResultsManager;

import java.util.ArrayList;
import java.util.List;

public class Results extends AppCompatActivity {
    private LinearLayout questionRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        questionRecord = (LinearLayout)findViewById(R.id.results_question_record);

        ResultsManager resultsManager = QuestionManager.getInstance().getResultsManager();
        resultsManager.displayResults(this);
    }

    public void populateNewStars(AchievementStars newStars){
        ImageView star1 = (ImageView)findViewById(R.id.results_star1);
        ImageView star2 = (ImageView)findViewById(R.id.results_star2);
        ImageView star3 = (ImageView)findViewById(R.id.results_star3);
        List<ImageView> stars = new ArrayList<>();
        stars.add(star1);
        stars.add(star2);
        stars.add(star3);
        GUIUtils.populateStarsImageView(stars,newStars);
    }

    public void addQuestion(String question){
        TextView view = (TextView)getLayoutInflater().inflate(R.layout.inflatable_results_question, questionRecord, false);
        view.setText(question);

        questionRecord.addView(view);
    }

    public void addResponse(String response, boolean correct){
        TextView view = (TextView)getLayoutInflater().inflate(R.layout.inflatable_results_response, questionRecord, false);
        view.setText(response);

        questionRecord.addView(view);
    }
}
