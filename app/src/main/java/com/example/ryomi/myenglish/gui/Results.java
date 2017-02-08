package com.example.ryomi.myenglish.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.AchievementStars;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.example.ryomi.myenglish.questionmanager.ResultsManager;

import java.util.ArrayList;
import java.util.List;

public class Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        ResultsManager resultsManager = QuestionManager.getInstance().getResultsManager();
        resultsManager.displayResults(this);
    }

    public void populateExistingStars(AchievementStars oldStars){
        ImageView star1 = (ImageView)findViewById(R.id.test_star1);
        ImageView star2 = (ImageView)findViewById(R.id.test_star2);
        ImageView star3 = (ImageView)findViewById(R.id.test_star3);
        List<ImageView> stars = new ArrayList<>();
        stars.add(star1);
        stars.add(star2);
        stars.add(star3);
        GUIUtils.populateStarsImageView(stars,oldStars);
        
    }

    public void populateNewStars(AchievementStars newStars){
        ImageView star4 = (ImageView)findViewById(R.id.test_star4);
        ImageView star5 = (ImageView)findViewById(R.id.test_star5);
        ImageView star6 = (ImageView)findViewById(R.id.test_star6);
        List<ImageView> stars = new ArrayList<>();
        stars.add(star4);
        stars.add(star5);
        stars.add(star6);
        GUIUtils.populateStarsImageView(stars,newStars);
    }
}
