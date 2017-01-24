package com.example.ryomi.myenglish.gui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.QuestionData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.questiongenerator.Theme;
import com.example.ryomi.myenglish.questiongenerator.ThemeFactory;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ThemeDetails extends AppCompatActivity {
    private ThemeData themeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_details);

        Intent intent = getIntent();
        if (intent.hasExtra("id") && intent.hasExtra("backgroundColor")){
            //get data
            String id = intent.getStringExtra("id");
            populateData(id);
            int bgColor = intent.getIntExtra("backgroundColor",0);
            setBackgroundColor(bgColor);
            adjustLayout();
            addActionListeners();

        }
    }

    private void populateData(String id){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("themes/"+id);
        ValueEventListener getThemeData = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                themeData = dataSnapshot.getValue(ThemeData.class);
                ImageView imageView = (ImageView) ThemeDetails.this.findViewById(R.id.theme_details_mainImage);
                int imageID = GUIUtils.stringToDrawableID(themeData.getImage(), ThemeDetails.this);
                imageView.setImageResource(imageID);

                TextView titleTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_title);
                titleTextView.setText(themeData.getCategory());

                TextView lastPlayedTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_lastPlayed);
                lastPlayedTextView.setText("1/14/16");

                TextView bestScoreTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_bestScore);
                bestScoreTextView.setText("10/10");

                TextView descriptionTextView = (TextView) ThemeDetails.this.findViewById(R.id.theme_details_description);
                if (Build.VERSION.SDK_INT >= 24) {
                    descriptionTextView.setText(Html.fromHtml(themeData.getDescription(), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    descriptionTextView.setText((Html.fromHtml(themeData.getDescription())));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        ref.addListenerForSingleValueEvent(getThemeData);
    }

    private void setBackgroundColor(int color){
        ScrollView activity = (ScrollView) findViewById(R.id.activity_theme_details);
        activity.setBackgroundColor(color);
    }

    private void adjustLayout(){
        //this is the padding for the 'whole activity'
        //if we set the padding for the activity
        //then the main image will be covered up by the padding
        //so set padding for individual layouts
        double paddingPercent = 0.1;

        //top horizontal layout that has info and the image next to it.
        //we have to extend the width so we can make a cool
        //'part-of-the-image-is-cut' effect
        LinearLayout outOfScreenLayout = (LinearLayout) findViewById(R.id.theme_details_outOfScreenLayout);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int newWidth = (int)(width * 1.1);
        LinearLayout.LayoutParams ooslParams = new LinearLayout.LayoutParams(
                newWidth, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        outOfScreenLayout.setLayoutParams(ooslParams);
        int ooslPaddingLeft = (int)(width * paddingPercent);
        int ooslPaddingTop = (int)(height * paddingPercent);
        int ooslPaddingBottom =ooslPaddingTop;
        int ooslPaddingRight = 0;

        outOfScreenLayout.setPadding(ooslPaddingLeft, ooslPaddingTop,
                ooslPaddingRight, ooslPaddingBottom);

        //spinner
        Spinner selectInstanceSpinner = (Spinner) findViewById(R.id.theme_details_selectInstanceSpinner);
        int sisMarginLeft = (int)(width * paddingPercent);
        int sisMarginRight = sisMarginLeft;
        int sisMarginTop = (int)(height * 0.05);
        int sisMarginBottom = (int)(height * 0.05);
        LinearLayout.LayoutParams sisParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sisParams.setMargins(sisMarginLeft,sisMarginTop,sisMarginRight,sisMarginBottom);
        selectInstanceSpinner.setLayoutParams(sisParams);

        //description
        TextView descriptionTextView = (TextView) findViewById(R.id.theme_details_description);
        int dtvMarginLeft = (int)(width * paddingPercent);
        int dtvMarginRight = dtvMarginLeft;
        int dtvMarginTop = (int)(height * 0.05);
        int dtvMarginBottom = (int)(height * 0.1);
        LinearLayout.LayoutParams dtvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dtvParams.setMargins (dtvMarginLeft,dtvMarginTop,
                dtvMarginRight,dtvMarginBottom);
        descriptionTextView.setLayoutParams(dtvParams);

        int dtvPaddingLeft = (int)(width * 0.1);
        int dtvPaddingRight = dtvPaddingLeft;
        int dtvPaddingTop = (int)(height * 0.1);
        int dtvPaddingBottom = (int)(height * 0.1);
        descriptionTextView.setPadding(dtvPaddingLeft,dtvPaddingTop, dtvPaddingRight, dtvPaddingBottom);

    }

    private void addActionListeners(){
        Button playButton = (Button) findViewById(R.id.theme_details_playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewQuestions();
            }
        });
        System.out.println("Hello new world");
    }

    private void createNewQuestions(){
        Theme theme = ThemeFactory.createTheme(themeData);
        //this also calls QuestionManager and starts the questions.
        //we should ideally return a instance and then pass that in to the question manager
        //but I gave up after a few hours working with stupid asynchronous data
        theme.initiateQuestions(this);

    }
}
