package com.example.ryomi.myenglish.gui;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryomi.myenglish.R;
import com.example.ryomi.myenglish.db.datawrappers.ThemeData;
import com.example.ryomi.myenglish.db.datawrappers.ThemeInstanceData;
import com.example.ryomi.myenglish.gui.widgets.GUIUtils;
import com.example.ryomi.myenglish.questiongenerator.Theme;
import com.example.ryomi.myenglish.questiongenerator.ThemeFactory;
import com.example.ryomi.myenglish.questionmanager.QuestionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import info.hoang8f.android.segmented.SegmentedGroup;

public class ThemeDetails extends AppCompatActivity {
    private ThemeData themeData;
    private View selectedInstanceLayout = null;
    private int themeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_details);

        Intent intent = getIntent();
        if (intent.hasExtra("id") && intent.hasExtra("backgroundColor")){
            //get data
            String id = intent.getStringExtra("id");
            populateData(id);
            int color = intent.getIntExtra("backgroundColor",0);
            setThemeColor(color);
            adjustLayout();
            addActionListeners();

        }
    }

    private void populateData(String themeID){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("themes/"+themeID);
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


        //list of instances
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final LinearLayout instanceList = (LinearLayout) findViewById(R.id.theme_details_instanceList);
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("themeInstances/"+userID+"/"+themeID);
            ValueEventListener getThemeInstanceData = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        ThemeInstanceData data = child.getValue(ThemeInstanceData.class);
                        String date = new Date(data.getCreatedTimestamp()).toString();
                        RelativeLayout row = (RelativeLayout) ThemeDetails.this.getLayoutInflater().inflate(
                                R.layout.inflatable_theme_details_instance_list_item, null
                        );

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0,0,0,30);
                        row.setLayoutParams(params);

                        TextView topicsTextView = (TextView)row.findViewById(R.id.theme_details_item_topics);
                        String topics = "";
                        for (String topic : data.getTopics()){
                            topics += topic + " + ";
                        }
                        topics = topics.substring(0,topics.length()-3);
                        topicsTextView.setText(topics);

                        TextView dateCreatedTextView = (TextView)row.findViewById(R.id.theme_details_item_date_created);
                        dateCreatedTextView.setText(date);
                        dateCreatedTextView.setTextColor(themeColor);
                        //save data in the row so we can retrieve it later
                        row.setTag(data);
                        //the row acts like a button group

                        row.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                activateRow(view);
                            }
                        });
                        instanceList.addView(row);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            ref2.addListenerForSingleValueEvent(getThemeInstanceData);

        }
    }

    private void setThemeColor(int color){
        themeColor = color;
        //background for whole activity
        ScrollView activity = (ScrollView) findViewById(R.id.activity_theme_details);
        activity.setBackgroundColor(color);

        //text colors for white background
        // (excluding description which will be black)

        int white = ContextCompat.getColor(this, R.color.white);
        SegmentedGroup segmentedControl = (SegmentedGroup)
                findViewById(R.id.theme_details_chooseInstanceControl);
        segmentedControl.setTintColor(white, themeColor);
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
    }

    private void addActionListeners(){
        Button playButton = (Button) findViewById(R.id.theme_details_playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQuestions();
            }
        });

        RadioButton newInstanceButton = (RadioButton) findViewById(R.id.theme_details_newInstance);
        newInstanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout hideLayout = (LinearLayout) findViewById(R.id.theme_details_instanceList);
                hideLayout.setVisibility(View.GONE);
            }
        });

        RadioButton oldInstanceButton = (RadioButton) findViewById(R.id.theme_details_oldInstance);
        oldInstanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout showLayout = (LinearLayout) findViewById(R.id.theme_details_instanceList);
                showLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    private void activateRow(View activateView){
        //the row is already activated
        //then deactivate that row
        if(activateView.equals(selectedInstanceLayout)) {
            deactivateSelectedRow();
            return;
        }

        deactivateSelectedRow();
        activateView.setBackgroundColor(themeColor);
        selectedInstanceLayout = activateView;
    }

    private void deactivateSelectedRow(){
        //there is no active row
        if (selectedInstanceLayout == null)
            return;

        int white = ContextCompat.getColor(this, R.color.white);
        selectedInstanceLayout.setBackgroundColor(white);

        selectedInstanceLayout = null;
    }

    private void startQuestions(){
        RadioButton newInstanceButton = (RadioButton) findViewById(R.id.theme_details_newInstance);
        if (newInstanceButton.isChecked()){
            createNewQuestions();
        } else {
            //if it's not checked the old instance button has to be checked
            if (selectedInstanceLayout != null)
                startOldQuestions();
            else
                Toast.makeText(this,"Please select an instance",Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewQuestions(){
        Theme theme = ThemeFactory.createTheme(themeData);
        //this also calls QuestionManager and starts the questions.
        //we should ideally return a instance and then pass that in to the question manager
        //but I gave up after a few hours working with stupid asynchronous data
        theme.initiateQuestions(this);
    }

    private void startOldQuestions(){
        //the user hasn't selected an instance
        if (selectedInstanceLayout == null){
            return;
        }

        ThemeInstanceData data = (ThemeInstanceData)selectedInstanceLayout.getTag();
        QuestionManager manager = QuestionManager.getInstance();
        manager.startQuestions(data, this);
    }
}
